package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.infrastructure.config.AppProperties;
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

/**
 * Tests para KafkaDlqPublisher (KafkaSender inyectado):
 *  - Happy path: send OK -> completa.
 *  - Error en sender: el Mono falla.
 *
 * Nota: no inspeccionamos internals de SenderRecord para mantener compatibilidad
 * entre versiones de reactor-kafka.
 */
@ExtendWith(MockitoExtension.class)
class KafkaDlqPublisherTest {

    @Mock private AppProperties props;
    @Mock private KafkaSender<String, byte[]> kafkaSender;

    private KafkaDlqPublisher dlq;

    @BeforeEach
    void setUp() {
        dlq = new KafkaDlqPublisher(props, kafkaSender);
    }

    @Test
    @DisplayName("sendToDlq(): publica en <mainTopic>.dlq y completa")
    void sendToDlq_happy_path() {
        String mainTopic = "ticket.created";
        byte[] payload = "hello".getBytes();

        @SuppressWarnings("unchecked")
        SenderResult<Object> ok = mock(SenderResult.class);
        when(kafkaSender.send(any())).thenReturn(Flux.just(ok));

        StepVerifier.create(dlq.sendToDlq(mainTopic, payload))
                .verifyComplete();

        verify(kafkaSender, times(1)).send(any());
        verifyNoMoreInteractions(kafkaSender);
    }

    @Test
    @DisplayName("sendToDlq(): si el sender falla, el Mono devuelve error")
    void sendToDlq_sender_errors() {
        String mainTopic = "reservation.emitted";
        byte[] payload = "oops".getBytes();

        when(kafkaSender.send(any())).thenReturn(Flux.error(new RuntimeException("send failed")));

        StepVerifier.create(dlq.sendToDlq(mainTopic, payload))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("send failed"))
                .verify();

        verify(kafkaSender, times(1)).send(any());
    }
}
