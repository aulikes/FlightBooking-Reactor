package com.aug.flightbooking.infrastructure.messaging.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReactiveJsonDecoder {

    private final ObjectMapper objectMapper;

    public <T> Mono<T> decode(byte[] data, Class<T> clazz) {
        try {
            // Usamos Mono.just con la conversión directa porque ObjectMapper trabaja sobre memoria
            T value = objectMapper.readValue(data, clazz);
            return Mono.just(value);
        } catch (Exception e) {
            log.error("Error deserializando {}: {}", clazz.getSimpleName(), e.getMessage());
            return Mono.error(new RuntimeException("Error deserializando mensaje", e));
        }
    }
}
