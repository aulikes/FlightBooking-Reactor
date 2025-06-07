package com.aug.flightbooking.infrastructure.cache;

import com.aug.flightbooking.application.port.out.ReservationCache;
import com.aug.flightbooking.infrastructure.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Servicio que maneja los timeouts de reservas usando Redis.
 * - Cuando se crea una reserva, se registra un timeout en Redis con TTL.
 * - Si la reserva es confirmada o rechazada antes de ese tiempo, se elimina el timeout.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisReservationCache implements ReservationCache {

    // Cliente para operaciones simples de Redis tipo clave-valor (String -> String)
    private final ReactiveValueOperations<String, String> redisOps;
    private final ApplicationProperties properties;

    /**
     * Cuando se crea una reserva la registra en Redis con un tiempo de espera,
     * Pasado ese tiempo Redis elimina el registro automáticamente
     */
    @Override
    public Mono<Void> registerTimeout(Long reservationId) {
        String key = properties.getRedis().getKeyPrefixReservationCache() + reservationId;
        // TTL: cuántos segundos esperar antes de que Redis elimine esta clave
        Duration ttl = Duration.ofSeconds(properties.getRedis().getReservationTimeoutSeconds());
        // Guardamos en Redis: clave = reservation:timeout:{id}, valor = "WAITING", TTL = 5 minutos (por ejemplo)
        return redisOps.set(key, "WAITING", ttl).then();
    }

    /**
     * Elimina el registro de la reserva en Redis si la reserva fue confirmada o rechazada.
     * Esto evita que luego sea marcada como fallida innecesariamente.
     */
    @Override
    public Mono<Void> cancelTimeout(Long reservationId) {
        return redisOps.delete(properties.getRedis().getKeyPrefixReservationCache() + reservationId).then();
    }

    @Override
    public Mono<String> get(Long reservationId) {
        return redisOps.get(properties.getRedis().getKeyPrefixReservationCache() + reservationId);
    }

}
