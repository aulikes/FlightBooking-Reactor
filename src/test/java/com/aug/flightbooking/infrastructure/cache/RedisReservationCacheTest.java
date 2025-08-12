package com.aug.flightbooking.infrastructure.cache;

import com.aug.flightbooking.infrastructure.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para RedisReservationCache.
 * Verifica set/get/delete con TTL y prefijo de clave a través del ReactiveRedisTemplate.
 */
@ExtendWith(MockitoExtension.class)
class RedisReservationCacheTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    @Mock
    private AppProperties properties;

    @Mock
    private AppProperties.Redis redis;

    @Mock
    private AppProperties.Redis.RedisReservation redisReservation;

    // No usamos @InjectMocks porque el constructor de RedisReservationCache requiere AppProperties,
    // y queremos controlar los retornos de getRedis() y getRedisReservation().
    private RedisReservationCache cache;

    @BeforeEach
    void setup() {
        // Stubs de propiedades anidadas
        when(properties.getRedis()).thenReturn(redis);
        when(redis.getRedisReservation()).thenReturn(redisReservation);
        when(redisReservation.getKeyPrefixReservationCache()).thenReturn("reservation:timeout:");

        // Redis template -> value ops
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // Instancia del SUT
        cache = new RedisReservationCache(redisTemplate, properties);
    }

    @Test
    @DisplayName("registerTimeout(): debe setear WAITING con TTL y completar")
    void registerTimeout_sets_waiting_with_ttl() {
        Long id = 42L;
        String expectedKey = "reservation:timeout:" + id;

        // Si quieres asegurar un TTL específico, setéalo en el mock de propiedades:
        // when(redisReservation.getReservationTimeoutSeconds()).thenReturn(300);

        // No fijamos el TTL exacto en el stub para evitar Strict stubbing si cambia (p.ej., PT0S)
        when(valueOps.set(eq(expectedKey), eq("WAITING"), any(Duration.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(cache.registerTimeout(id)).verifyComplete();

        // Verificamos argumentos y capturamos el TTL real usado
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOps, times(1)).set(eq(expectedKey), eq("WAITING"), ttlCaptor.capture());

        Duration usedTtl = ttlCaptor.getValue();
        assertThat(usedTtl).isNotNull();
        // Si quieres permitir TTL=0 como válido (según tu config):
        assertThat(usedTtl.isNegative()).isFalse();
        // O si esperas uno específico, descomenta:
        // assertThat(usedTtl).isEqualTo(Duration.ofSeconds(300));
    }

    @Test
    @DisplayName("cancelTimeout(): debe borrar la clave y completar")
    void cancelTimeout_deletes_key() {
        Long id = 77L;
        String expectedKey = "reservation:timeout:" + id;

        // ReactiveValueOperations.delete(K) retorna Mono<Boolean>
        when(valueOps.delete(eq(expectedKey))).thenReturn(Mono.just(true));

        StepVerifier.create(cache.cancelTimeout(id)).verifyComplete();

        verify(valueOps, times(1)).delete(eq(expectedKey));
    }

    @Test
    @DisplayName("get(): cuando existe valor en Redis, lo retorna")
    void get_returns_value_when_present() {
        Long id = 99L;
        String expectedKey = "reservation:timeout:" + id;

        when(valueOps.get(eq(expectedKey))).thenReturn(Mono.just("WAITING"));

        StepVerifier.create(cache.get(id))
                .expectNext("WAITING")
                .verifyComplete();

        verify(valueOps, times(1)).get(eq(expectedKey));
    }

    @Test
    @DisplayName("get(): cuando NO existe valor, completa vacío (sin onNext)")
    void get_completes_empty_when_absent() {
        Long id = 100L;
        String expectedKey = "reservation:timeout:" + id;

        when(valueOps.get(eq(expectedKey))).thenReturn(Mono.empty());

        StepVerifier.create(cache.get(id))
                .verifyComplete(); // no emite valor

        verify(valueOps, times(1)).get(eq(expectedKey));
    }
}
