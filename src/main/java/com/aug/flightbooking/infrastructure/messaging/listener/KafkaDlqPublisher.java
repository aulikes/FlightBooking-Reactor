package com.aug.flightbooking.infrastructure.messaging.listener;

import com.aug.flightbooking.infrastructure.config.AppProperties;
import com.aug.flightbooking.infrastructure.config.KafkaSenderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaDlqPublisher {

    private final AppProperties properties;
    private final KafkaSender<String, byte[]> kafkaSender;

    public Mono<Void> sendToDlq(String mainTopic, byte[] payload) {
        String dlqTopic = resolveDlqTopic(mainTopic);
        ProducerRecord<String, byte[]> pr = new ProducerRecord<>(dlqTopic, payload);

        return kafkaSender.send(Mono.just(SenderRecord.create(pr, null)))
                .next()
                .doOnNext(r -> log.warn("[DLQ] Enviado a {} (mainTopic={})", dlqTopic, mainTopic))
                .then();
    }

    private String resolveDlqTopic(String mainTopic) {
        // Sí se quiere DLQ por tópico específico, añadir getters como getTicketCreatedDlqTopic()
        // y resolverlo acá. Mientras tanto, fallback genérico:
        return mainTopic + ".dlq";
    }
}
