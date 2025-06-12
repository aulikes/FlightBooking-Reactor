package com.aug.flightbooking.infrastructure.cache;

import com.aug.flightbooking.application.port.out.ReservationCache;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Servicio que maneja los timeouts de reservas usando Redis.
 * - Cuando se crea una reserva, se registra un timeout en Redis con TTL.
 * - Si la reserva es confirmada o rechazada antes de ese tiempo, se elimina el timeout.
 */
@Component
@Slf4j
public class RedisReservationCache implements ReservationCache {

    // Cliente reactivo completo para acceder a Redis, se debe realizar redisTemplate.opsForValue()
    // para obtener el cliente ReactiveValueOperations<String, String> tipo clave-valor (String -> String)
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final AppProperties.Redis redisProperties;

    public RedisReservationCache(
        @Qualifier("reservationRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
        AppProperties properties) {
        this.redisTemplate = redisTemplate;
        this.redisProperties = properties.getRedis();
    }

    /**
     * Cuando se crea una reserva la registra en Redis con un tiempo de espera,
     * Pasado ese tiempo Redis elimina el registro automáticamente
     */
    @Override
    public Mono<Void> registerTimeout(Long reservationId) {
        String key = redisProperties.getRedisReservation().getKeyPrefixReservationCache() + reservationId;
        // TTL: cuántos segundos esperar antes de que Redis elimine esta clave
        Duration ttl = Duration.ofSeconds(redisProperties.getRedisReservation().getReservationTimeoutSeconds());
        // Guardamos en Redis: clave = reservation:timeout:{id}, valor = "WAITING", TTL = 5 minutos (por ejemplo)
        return redisTemplate.opsForValue().set(key, "WAITING", ttl).then();
    }

    /**
     * Elimina el registro de la reserva en Redis si la reserva fue confirmada o rechazada.
     * Esto evita que luego sea marcada como fallida innecesariamente.
     */
    @Override
    public Mono<Void> cancelTimeout(Long reservationId) {
        String key = redisProperties.getRedisReservation().getKeyPrefixReservationCache() + reservationId;
        return redisTemplate.opsForValue().delete(key).then();
    }

    @Override
    public Mono<String> get(Long reservationId) {
        String key = redisProperties.getRedisReservation().getKeyPrefixReservationCache() + reservationId;
        return redisTemplate.opsForValue().get(key);
    }

}
