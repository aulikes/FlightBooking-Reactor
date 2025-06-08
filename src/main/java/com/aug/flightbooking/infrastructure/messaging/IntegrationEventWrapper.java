package com.aug.flightbooking.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Wrapper genérico para eventos publicados en Kafka o Rabbit.
 * Incluye metadata común y el payload real del evento.
 *
 * @param <T> Tipo del evento real (debe implementar IntegrationEvent)
 */
public record IntegrationEventWrapper<T>(
        @JsonProperty("eventType") String eventType,
        @JsonProperty("version") String version,
        @JsonProperty("traceId") String traceId,
        @JsonProperty("timestamp") Instant timestamp,
        @JsonProperty("data") T data
) {
    @JsonCreator
    public IntegrationEventWrapper {
        // No se necesita lógica adicional: Jackson se encarga del mapping con estas anotaciones
    }

    /**
     * Método estático de fábrica para construir el wrapper de forma clara.
     */
    public static <T> IntegrationEventWrapper<T> wrap(
            T data,
            String eventType,
            String version,
            String traceId,
            Instant timestamp
    ) {
        return new IntegrationEventWrapper<>(eventType, version, traceId, timestamp, data);
    }
}

