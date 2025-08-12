package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.application.events.TicketCreatedEvent;
import com.aug.flightbooking.application.ports.in.ReservationConfirmedEventHandler;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests para ReservTicketCreatedEventListenerKafka.
 * Comportamiento esperado (como en los otros listeners):
 *  - Éxito: procesa y ACK.
 *  - Error en decode: NO propaga, ACK y completa.
 *  - Error en handler: NO propaga, ACK y completa.
 */
@ExtendWith(MockitoExtension.class)
class ReservTicketCreatedEventListenerKafkaTest {

    @Mock private AppProperties props;
    @Mock private AppProperties.Kafka kafka;
    @Mock private AppProperties.Kafka.Producer producer;
    @Mock private AppProperties.Kafka.Consumer consumer;

    @Mock private ReactiveJsonDecoder decoder;
    @Mock private ReservationConfirmedEventHandler handler;
    @Mock private KafkaReceiver<String, byte[]> receiver;

    @InjectMocks
    private ReservTicketCreatedEventListenerKafka listener;

    private MockedStatic<KafkaReceiverFactory> receiverFactoryMock;

    @BeforeEach
    void setup() {
        when(props.getKafka()).thenReturn(kafka);
        when(kafka.getBootstrapServers()).thenReturn("localhost:9092");
        when(kafka.getProducer()).thenReturn(producer);
        when(kafka.getConsumer()).thenReturn(consumer);
        when(producer.getTicketCreatedTopic()).thenReturn("ticket.created");
        when(consumer.getTicketReservationCreatedGroupId()).thenReturn("grp-ticket-created");

        // Mock estático flexible (evita mismatches de argumentos exactos)
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
        // acknowledge es side-effect; no tiene retorno
        doNothing().when(offset).acknowledge();
        return record;
    }

    @Test
    @DisplayName("onMessage(): éxito — decodifica, handler OK, ACK y completa")
    void onMessage_happy_path() {
        TicketCreatedEvent evt = new TicketCreatedEvent(11L, "OK");
        byte[] bytes = "{\"reservationId\":11,\"message\":\"OK\"}".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, TicketCreatedEvent.class)).thenReturn(Mono.just(evt));
        when(handler.handle(evt)).thenReturn(Mono.empty());

        StepVerifier.create(listener.onMessage()).verifyComplete();

        verify(handler, times(1)).handle(evt);
        verify(record.receiverOffset(), times(1)).acknowledge();
    }

    @Test
    @DisplayName("onMessage(): falla decoder — se absorbe el error, ACK y completa")
    void onMessage_decoder_error_ack_and_complete() {
        byte[] bytes = "invalid".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, TicketCreatedEvent.class))
                .thenReturn(Mono.error(new RuntimeException("bad json")));

        StepVerifier.create(listener.onMessage()).verifyComplete();

        verify(record.receiverOffset(), times(1)).acknowledge();
        verifyNoInteractions(handler);
    }

    @Test
    @DisplayName("onMessage(): falla handler — se absorbe el error, ACK y completa")
    void onMessage_handler_error_ack_and_complete() {
        TicketCreatedEvent evt = new TicketCreatedEvent(22L, "OK");
        byte[] bytes = "{\"reservationId\":22,\"message\":\"OK\"}".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, TicketCreatedEvent.class)).thenReturn(Mono.just(evt));
        when(handler.handle(evt)).thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(listener.onMessage()).verifyComplete();

        verify(handler, times(1)).handle(evt);
        verify(record.receiverOffset(), times(1)).acknowledge();
    }
}
