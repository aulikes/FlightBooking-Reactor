package com.aug.flightbooking.infrastructure.cache;

import com.aug.flightbooking.application.ports.in.FailReservationUseCase;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.Mockito.*;

/**
 * Pruebas para ReservationTimeoutScheduler.
 * Se usa VirtualTime para avanzar el tiempo y verificar invocaciones por tick.
 */
@ExtendWith(MockitoExtension.class)
class ReservationTimeoutSchedulerTest {

    @Mock
    private AppProperties properties;

    @Mock
    private AppProperties.Redis redis;

    @Mock
    private AppProperties.Redis.RedisReservation redisReservation;

    @Mock
    private FailReservationUseCase failReservationUseCase;

    private ReservationTimeoutScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(properties.getRedis()).thenReturn(redis);
        when(redis.getRedisReservation()).thenReturn(redisReservation);
        when(redisReservation.getReservationTimeoutSeconds()).thenReturn(123L);
        when(redisReservation.getPeriodFluxSeconds()).thenReturn(30L);

        scheduler = new ReservationTimeoutScheduler(properties, failReservationUseCase);
    }

    @Test
    @DisplayName("startSchedulerReservations(): invoca failReservations en cada tick y continúa indefinidamente")
    void startScheduler_invokes_on_each_tick() {
        // Primera llamada OK, segunda OK: ambas completan vacío
        when(failReservationUseCase.failReservations(123L))
                .thenReturn(Mono.empty(), Mono.empty());

        StepVerifier.withVirtualTime(() -> scheduler.startSchedulerReservations())
                // Tick 0 (inmediato por Duration.ZERO)
                .thenAwait(Duration.ZERO)
                // Avanza 30s para el segundo tick
                .thenAwait(Duration.ofSeconds(30))
                // Cancelamos (es un Flux infinito y no emite onNext)
                .thenCancel()
                .verify();

        verify(failReservationUseCase, times(2)).failReservations(123L);
    }

    @Test
    @DisplayName("startSchedulerReservations(): si un tick falla, onErrorContinue permite seguir con el siguiente")
    void startScheduler_errors_are_swallowed_and_continue() {
        // Primera emisión con error, segunda OK
        when(failReservationUseCase.failReservations(123L))
                .thenReturn(Mono.error(new RuntimeException("Fallo de negocio")), Mono.empty());

        StepVerifier.withVirtualTime(() -> scheduler.startSchedulerReservations())
                .thenAwait(Duration.ZERO)              // primer tick (con error)
                .thenAwait(Duration.ofSeconds(30))     // segundo tick (exitoso)
                .thenCancel()
                .verify();

        verify(failReservationUseCase, times(2)).failReservations(123L);
    }
}
