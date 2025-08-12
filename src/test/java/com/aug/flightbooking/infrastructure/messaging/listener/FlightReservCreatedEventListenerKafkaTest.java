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

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Pruebas unitarias para FlightReservCreatedEventListenerKafka.
 * Cubre: happy path, error en decoder y error en handler, asegurando acknowledge del offset.
 */
@ExtendWith(MockitoExtension.class)
class FlightReservCreatedEventListenerKafkaTest {

    @Mock private ReactiveJsonDecoder decoder;
    @Mock private ReservationCreatedEventHandler handler;

    @Mock private AppProperties props;
    @Mock private AppProperties.Kafka kafka;
    @Mock private AppProperties.Kafka.Consumer consumer;
    @Mock private AppProperties.Kafka.Producer producer;

    @Mock private KafkaReceiver<String, byte[]> receiver;

    @InjectMocks
    private FlightReservCreatedEventListenerKafka listener;

    private MockedStatic<KafkaReceiverFactory> receiverFactoryMock;

    @BeforeEach
    void setup() {
        when(props.getKafka()).thenReturn(kafka);
        when(kafka.getBootstrapServers()).thenReturn("localhost:9092");
        when(kafka.getConsumer()).thenReturn(consumer);
        when(kafka.getProducer()).thenReturn(producer);
        when(consumer.getReservationFlightCreatedGroupId()).thenReturn("grp-created");
        when(producer.getReservationCreatedTopic()).thenReturn("reservation.created");

        // Mock estático: acepta cualquier combinación de strings
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
        when(record.offset()).thenReturn(123L);
        when(record.partition()).thenReturn(0);
        doNothing().when(offset).acknowledge(); // side-effect sin retorno
        return record;
    }

    @Test
    @DisplayName("onMessage(): happy path — decodifica, delega al handler y ack del offset")
    void onMessage_happy_path() {
        ReservationCreatedEvent evt =
                new ReservationCreatedEvent(1L, 100L, "John Doe", "CC-1");
        byte[] bytes =
                "{\"reservationId\":1,\"flightId\":100,\"fullName\":\"John Doe\",\"documentId\":\"CC-1\"}".getBytes();

        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, ReservationCreatedEvent.class)).thenReturn(Mono.just(evt));
        when(handler.handle(evt)).thenReturn(Mono.empty());

        StepVerifier.create(listener.onMessage()).verifyComplete();

        verify(handler, times(1)).handle(evt);
        verify(record.receiverOffset(), times(1)).acknowledge();
    }

    @Test
    @DisplayName("onMessage(): si decoder falla, se absorbe el error y se ack el offset")
    void onMessage_decoder_error_ack_and_continue() {
        byte[] bytes = "invalid".getBytes();
        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, ReservationCreatedEvent.class))
                .thenReturn(Mono.error(new RuntimeException("bad json")));

        StepVerifier.create(listener.onMessage()).verifyComplete();

        verify(record.receiverOffset(), times(1)).acknowledge();
        verifyNoInteractions(handler);
    }

    @Test
    @DisplayName("onMessage(): si handler falla, se absorbe el error y se ack el offset")
    void onMessage_handler_error_ack_and_complete() {
        ReservationCreatedEvent evt =
                new ReservationCreatedEvent(2L, 200L, "Jane Roe", "CC-2");
        byte[] bytes =
                "{\"reservationId\":2,\"flightId\":200,\"fullName\":\"Jane Roe\",\"documentId\":\"CC-2\"}".getBytes();

        ReceiverRecord<String, byte[]> record = mockRecord(bytes);

        when(receiver.receive()).thenReturn(Flux.just(record));
        when(decoder.decode(bytes, ReservationCreatedEvent.class)).thenReturn(Mono.just(evt));
        when(handler.handle(evt)).thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(listener.onMessage()).verifyComplete();

        verify(handler, times(1)).handle(evt);
        verify(record.receiverOffset(), times(1)).acknowledge();
    }
}
