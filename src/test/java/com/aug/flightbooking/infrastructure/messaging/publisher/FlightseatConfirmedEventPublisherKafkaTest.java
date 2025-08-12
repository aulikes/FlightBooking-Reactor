package com.aug.flightbooking.infrastructure.messaging.publisher;

import com.aug.flightbooking.application.events.FlightseatConfirmedEvent;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.messaging.serialization.ReactiveJsonEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightseatConfirmedEventPublisherKafkaTest {

    @Mock private KafkaSender<String, byte[]> kafkaSender;
    @Mock private AppProperties props;
    @Mock private AppProperties.Kafka kafka;
    @Mock private AppProperties.Kafka.Producer producerProps;
    @Mock private ReactiveJsonEncoder encoder;

    private FlightseatConfirmedEventPublisherKafka publisher;

    @BeforeEach
    void setup() {
        when(props.getKafka()).thenReturn(kafka);
        when(kafka.getProducer()).thenReturn(producerProps);
        when(producerProps.getFlightseatConfirmedTopic()).thenReturn("flightseat.confirmed");

        publisher = new FlightseatConfirmedEventPublisherKafka(kafkaSender, props, encoder);
    }

    @Test
    @DisplayName("publish(): encode OK -> send OK -> completa")
    void publish_happy_path() {
        FlightseatConfirmedEvent event = new FlightseatConfirmedEvent(99L);
        byte[] payload = "{\"reservationId\":99}".getBytes();

        when(encoder.encode(event)).thenReturn(Mono.just(payload));
        @SuppressWarnings("unchecked")
        SenderResult<Object> ok = mock(SenderResult.class);
        when(kafkaSender.send(any())).thenReturn(Flux.just(ok));

        StepVerifier.create(publisher.publish(event)).verifyComplete();

        verify(encoder, times(1)).encode(event);
        verify(kafkaSender, times(1)).send(any());
        verifyNoMoreInteractions(kafkaSender);
    }

    @Test
    @DisplayName("publish(): si el encoder falla -> NO se llama send y el Mono falla")
    void publish_encoder_error() {
        FlightseatConfirmedEvent event = new FlightseatConfirmedEvent(10L);
        when(encoder.encode(event)).thenReturn(Mono.error(new RuntimeException("encode failed")));

        StepVerifier.create(publisher.publish(event))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("encode failed"))
                .verify();

        verify(encoder, times(1)).encode(event);
        verify(kafkaSender, never()).send(any());
    }

    @Test
    @DisplayName("publish(): encode OK pero sender falla -> el Mono falla")
    void publish_sender_error() {
        FlightseatConfirmedEvent event = new FlightseatConfirmedEvent(77L);
        byte[] payload = "{\"reservationId\":77}".getBytes();

        when(encoder.encode(event)).thenReturn(Mono.just(payload));
        when(kafkaSender.send(any())).thenReturn(Flux.error(new RuntimeException("send failed")));

        StepVerifier.create(publisher.publish(event))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("send failed"))
                .verify();

        verify(encoder, times(1)).encode(event);
        verify(kafkaSender, times(1)).send(any());
    }
}
