package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.events.ReservationEmittedEvent;
import com.aug.flightbooking.application.ports.in.ReservationEmittedEventHandler;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.config.KafkaReceiverFactory;
import com.aug.flightbooking.infrastructure.messaging.serialization.ReactiveJsonDecoder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests para FlightReservEmittedEventListenerKafka:
 *  - Éxito: decode + handler OK ⇒ ACK y sin DLQ.
 *  - Falla decode: NO retry, envía a DLQ y ACK.
 *  - Falla handler con retry(3): tras agotar, DLQ y ACK (gracias a Mono.defer()).
 */
@ExtendWith(MockitoExtension.class)
class FlightReservEmittedEventListenerKafkaTest {

    @Mock private AppProperties props;
    @Mock private AppProperties.Kafka kafka;
    @Mock private AppProperties.Kafka.Producer producer;
    @Mock private AppProperties.Kafka.Consumer consumer;

    @Mock private ReactiveJsonDecoder decoder;
    @Mock private ReservationEmittedEventHandler handler;
    @Mock private KafkaReceiver<String, byte[]> receiver;

    @Mock private KafkaDlqPublisher dlqPublisher;

    @InjectMocks
    private FlightReservEmittedEventListenerKafka listener;

    private MockedStatic<KafkaReceiverFactory> receiverFactoryMock;

    @BeforeEach
    void setup() {
        when(props.getKafka()).thenReturn(kafka);
        when(kafka.getBootstrapServers()).thenReturn("localhost:9092");
        when(kafka.getProducer()).thenReturn(producer);
        when(kafka.getConsumer()).thenReturn(consumer);
        when(producer.getReservationEmittedTopic()).thenReturn("reservation.emitted");
        when(consumer.getReservationFlightEmittedGroupId()).thenReturn("grp-emitted");

        receiverFactoryMock = mockStatic(KafkaReceiverFactory.class);
        receiverFactoryMock
                .when(() -> KafkaReceiverFactory.createReceiver(anyString(), anyString(), anyString()))
                .thenReturn(receiver);
    }

    @AfterEach
    void tearDown() {
        if (receiverFactoryMock != null) receiverFactoryMock.close();
    }

    private ReceiverRecord<String, byte[]> mockRecord(byte[] payload) {
        ReceiverRecord<String, byte[]> record = mock(ReceiverRecord.class);
        ReceiverOffset offset = mock(ReceiverOffset.class);
        when(record.value()).thenReturn(payload);
        when(record.receiverOffset()).thenReturn(offset);
        doNothing().when(offset).acknowledge();
        return record;
    }

    @Test
    @DisplayName("Éxito: decodifica, handler OK ⇒ ACK y no DLQ")
    void onMessage_happy_path_ack_only() {
        String topic = "reservation.emitted";
        ReservationEmittedEvent evt = new ReservationEmittedEvent(11L);
        byte[] bytes = "{\"reservationId\":11}".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, ReservationEmittedEvent.class)).thenReturn(Mono.just(evt));
        when(handler.handle(evt)).thenReturn(Mono.empty());

        StepVerifier.create(listener.onMessage()).verifyComplete();

        verify(handler, times(1)).handle(evt);
        verify(record.receiverOffset(), times(1)).acknowledge();
        verifyNoInteractions(dlqPublisher);
    }

    @Test
    @DisplayName("Falla decode: NO retry, envía a DLQ y ACK")
    void onMessage_decode_failure_goes_dlq_and_ack() {
        String topic = "reservation.emitted";
        byte[] bytes = "invalid".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, ReservationEmittedEvent.class))
                .thenReturn(Mono.error(new RuntimeException("bad json")));
        when(dlqPublisher.sendToDlq(topic, bytes)).thenReturn(Mono.empty());

        StepVerifier.create(listener.onMessage()).verifyComplete();

        verifyNoInteractions(handler);
        verify(dlqPublisher, times(1)).sendToDlq(topic, bytes);
        verify(record.receiverOffset(), times(1)).acknowledge();
    }

    @Test
    @DisplayName("Falla handler con retry(3): tras agotar, DLQ y ACK")
    void onMessage_handler_failure_retries_then_dlq_and_ack() {
        String topic = "reservation.emitted";
        ReservationEmittedEvent evt = new ReservationEmittedEvent(22L);
        byte[] bytes = "{\"reservationId\":22}".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, ReservationEmittedEvent.class)).thenReturn(Mono.just(evt));

        // Handler falla siempre: con Mono.defer en producción, se re-invoca 1 + 3 veces
        when(handler.handle(evt)).thenReturn(Mono.error(new RuntimeException("boom")));
        when(dlqPublisher.sendToDlq(topic, bytes)).thenReturn(Mono.empty());

        StepVerifier.withVirtualTime(() -> listener.onMessage())
                .thenAwait(Duration.ofMillis(1500)) // 3 * 500ms de tus reintentos
                .verifyComplete();

        verify(handler, times(4)).handle(evt); // 1 intento + 3 reintentos
        verify(dlqPublisher, times(1)).sendToDlq(topic, bytes);
        verify(record.receiverOffset(), times(1)).acknowledge();
    }
}
