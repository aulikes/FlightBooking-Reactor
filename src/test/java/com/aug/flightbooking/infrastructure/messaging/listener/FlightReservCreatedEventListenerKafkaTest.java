package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.events.ReservationCreatedEvent;
import com.aug.flightbooking.application.ports.in.ReservationCreatedEventHandler;
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
 * Tests para FlightReservCreatedEventListenerKafka con:
 *  - Éxito: decode + handler OK ⇒ ACK y sin DLQ.
 *  - Falla decode: NO retry, envía a DLQ y ACK.
 *  - Falla handler con retry(3): tras agotar, envía a DLQ y ACK.
 */
@ExtendWith(MockitoExtension.class)
class FlightReservCreatedEventListenerKafkaTest {

    @Mock private AppProperties props;
    @Mock private AppProperties.Kafka kafka;
    @Mock private AppProperties.Kafka.Producer producer;
    @Mock private AppProperties.Kafka.Consumer consumer;

    @Mock private ReactiveJsonDecoder decoder;
    @Mock private ReservationCreatedEventHandler handler;
    @Mock private KafkaReceiver<String, byte[]> receiver;

    // Publicador DLQ concreto según tu clase (si tienes interfaz, cámbialo a esa interfaz)
    @Mock private KafkaDlqPublisher dlqPublisher;

    @InjectMocks
    private FlightReservCreatedEventListenerKafka listener;

    private MockedStatic<KafkaReceiverFactory> receiverFactoryMock;

    @BeforeEach
    void setup() {
        when(props.getKafka()).thenReturn(kafka);
        when(kafka.getBootstrapServers()).thenReturn("localhost:9092");
        when(kafka.getProducer()).thenReturn(producer);
        when(kafka.getConsumer()).thenReturn(consumer);
        when(producer.getReservationCreatedTopic()).thenReturn("reservation.created");
        when(consumer.getReservationFlightCreatedGroupId()).thenReturn("grp-created");

        // Mock del método estático para devolver el receiver simulado
        receiverFactoryMock = mockStatic(KafkaReceiverFactory.class);
        receiverFactoryMock
                .when(() -> KafkaReceiverFactory.createReceiver(anyString(), anyString(), anyString()))
                .thenReturn(receiver);
    }

    @AfterEach
    void tearDown() {
        if (receiverFactoryMock != null) receiverFactoryMock.close();
    }

    // Helper para construir un ReceiverRecord con offset ack mockeable
    private ReceiverRecord<String, byte[]> mockRecord(byte[] payload) {
        ReceiverRecord<String, byte[]> record = mock(ReceiverRecord.class);
        ReceiverOffset offset = mock(ReceiverOffset.class);
        when(record.value()).thenReturn(payload);
        when(record.receiverOffset()).thenReturn(offset);
        // acknowledge: side-effect sin retorno
        doNothing().when(offset).acknowledge();
        return record;
    }

    @Test
    @DisplayName("Éxito: decodifica, handler OK ⇒ ACK y sin DLQ")
    void onMessage_happy_path_ack_only() {
        String topic = "reservation.created";
        ReservationCreatedEvent evt =
                new ReservationCreatedEvent(11L, 100L, "John Doe", "CC-1");
        byte[] bytes =
                "{\"reservationId\":11,\"flightId\":100,\"fullName\":\"John Doe\",\"documentId\":\"CC-1\"}".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        // El receiver emite un solo record
        when(receiver.receive()).thenReturn(Flux.just(record));
        // Decode OK
        when(decoder.decode(bytes, ReservationCreatedEvent.class)).thenReturn(Mono.just(evt));
        // Handler OK
        when(handler.handle(evt)).thenReturn(Mono.empty());

        StepVerifier.create(listener.onMessage()).verifyComplete();

        verify(handler, times(1)).handle(evt);
        verify(record.receiverOffset(), times(1)).acknowledge();
        verifyNoInteractions(dlqPublisher);
    }

    @Test
    @DisplayName("Falla en decode: NO retry, envía a DLQ y ACK")
    void onMessage_decode_failure_goes_dlq_and_ack() {
        String topic = "reservation.created";
        byte[] bytes = "invalid".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        // Decode falla (no hay retry configurado para decode)
        when(decoder.decode(bytes, ReservationCreatedEvent.class))
                .thenReturn(Mono.error(new RuntimeException("bad json")));
        // DLQ OK
        when(dlqPublisher.sendToDlq(topic, bytes)).thenReturn(Mono.empty());

        StepVerifier.create(listener.onMessage()).verifyComplete();

        verifyNoInteractions(handler); // nunca llega al handler
        verify(dlqPublisher, times(1)).sendToDlq(topic, bytes);
        verify(record.receiverOffset(), times(1)).acknowledge();
    }

    @Test
    @DisplayName("Falla handler con retry(3): tras agotar, DLQ y ACK")
    void onMessage_handler_failure_retries_then_dlq_and_ack() {
        String topic = "reservation.created";
        ReservationCreatedEvent evt =
                new ReservationCreatedEvent(22L, 200L, "Jane Roe", "CC-2");
        byte[] bytes =
                "{\"reservationId\":22,\"flightId\":200,\"fullName\":\"Jane Roe\",\"documentId\":\"CC-2\"}".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, ReservationCreatedEvent.class)).thenReturn(Mono.just(evt));

        // IMPORTANTE: handler falla SIEMPRE.
        // Como en la clase usamos Mono.defer(() -> handler.handle(event)),
        // el retry re-invoca al mock cada vez: 1 + 3 reintentos = 4 invocaciones.
        when(handler.handle(evt)).thenReturn(Mono.error(new RuntimeException("boom")));
        when(dlqPublisher.sendToDlq(topic, bytes)).thenReturn(Mono.empty());

        // Usamos tiempo virtual para "avanzar" los 3 * 500ms del retry
        StepVerifier.withVirtualTime(() -> listener.onMessage())
                .thenAwait(Duration.ofMillis(1500)) // 3 reintentos * 500ms
                .verifyComplete();

        verify(handler, times(4)).handle(evt); // 1 + 3 reintentos
        verify(dlqPublisher, times(1)).sendToDlq(topic, bytes);
        verify(record.receiverOffset(), times(1)).acknowledge();
    }
}
