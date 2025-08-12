package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.events.FlightseatRejectedEvent;
import com.aug.flightbooking.application.ports.in.FlightseatRejectedEventHandler;
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
 * Tests para ReservFlightseatRejectedEventListenerKafka:
 *  - Éxito: decode + handler OK ⇒ ACK y sin DLQ.
 *  - Falla decode: NO retry, envía a DLQ y ACK.
 *  - Falla handler con retry(3): tras agotar, DLQ y ACK (gracias a Mono.defer()).
 */
@ExtendWith(MockitoExtension.class)
class ReservFlightseatRejectedEventListenerKafkaTest {

    @Mock private AppProperties props;
    @Mock private AppProperties.Kafka kafka;
    @Mock private AppProperties.Kafka.Producer producer;
    @Mock private AppProperties.Kafka.Consumer consumer;

    @Mock private ReactiveJsonDecoder decoder;
    @Mock private FlightseatRejectedEventHandler handler;
    @Mock private KafkaReceiver<String, byte[]> receiver;

    @Mock private KafkaDlqPublisher dlqPublisher;

    @InjectMocks
    private ReservFlightseatRejectedEventListenerKafka listener;

    private MockedStatic<KafkaReceiverFactory> receiverFactoryMock;

    @BeforeEach
    void setup() {
        when(props.getKafka()).thenReturn(kafka);
        when(kafka.getBootstrapServers()).thenReturn("localhost:9092");
        when(kafka.getProducer()).thenReturn(producer);
        when(kafka.getConsumer()).thenReturn(consumer);
        when(producer.getFlightseatRejectedTopic()).thenReturn("flightseat.rejected");
        when(consumer.getFlightseatReservationRejectedGroupId()).thenReturn("grp-flightseat-rejected");

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
        doNothing().when(offset).acknowledge(); // side-effect
        return record;
    }

    @Test
    @DisplayName("Éxito: decodifica, handler OK ⇒ ACK y sin DLQ")
    void onMessage_happy_path_ack_only() {
        String topic = "flightseat.rejected";
        FlightseatRejectedEvent evt = new FlightseatRejectedEvent(11L, "No Seat");
        byte[] bytes = "{\"reservationId\":11,\"reason\":\"No Seat\"}".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, FlightseatRejectedEvent.class)).thenReturn(Mono.just(evt));
        when(handler.handle(evt)).thenReturn(Mono.empty());

        StepVerifier.create(listener.onMessage()).verifyComplete();

        verify(handler, times(1)).handle(evt);
        verify(record.receiverOffset(), times(1)).acknowledge();
        verifyNoInteractions(dlqPublisher);
    }

    @Test
    @DisplayName("Falla decode: NO retry, envía a DLQ y ACK")
    void onMessage_decode_failure_goes_dlq_and_ack() {
        String topic = "flightseat.rejected";
        byte[] bytes = "invalid".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, FlightseatRejectedEvent.class))
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
        String topic = "flightseat.rejected";
        FlightseatRejectedEvent evt = new FlightseatRejectedEvent(22L, "Timeout");
        byte[] bytes = "{\"reservationId\":22,\"reason\":\"Timeout\"}".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, FlightseatRejectedEvent.class)).thenReturn(Mono.just(evt));

        // Con Mono.defer en la clase productiva, el retry re-invoca al mock: 1 intento + 3 reintentos = 4
        when(handler.handle(evt)).thenReturn(Mono.error(new RuntimeException("boom")));
        when(dlqPublisher.sendToDlq(topic, bytes)).thenReturn(Mono.empty());

        StepVerifier.withVirtualTime(() -> listener.onMessage())
                .thenAwait(Duration.ofMillis(1500)) // 3 * 500 ms según tu retry
                .verifyComplete();

        verify(handler, times(4)).handle(evt);
        verify(dlqPublisher, times(1)).sendToDlq(topic, bytes);
        verify(record.receiverOffset(), times(1)).acknowledge();
    }
}
