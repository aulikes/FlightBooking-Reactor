
package com.aug.flightbooking.infrastructure.cache;

import com.aug.flightbooking.application.port.in.FailReservationUseCase;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Scheduler que verifica periódicamente si una reserva debe marcarse como fallida,
 * pero solo se activa cuando se invoca startScheduler() desde AppStartupFinalListener.
 */
@Component
@Slf4j
public class ReservationTimeoutScheduler {

    private final AppProperties.Redis.RedisReservation properties;
    private final FailReservationUseCase failReservationUseCase;
    private Disposable subscription;

    public ReservationTimeoutScheduler(AppProperties properties,
                                       FailReservationUseCase failReservationUseCase,
                                       Disposable subscription) {
        this.properties = properties.getRedis().getRedisReservation();
        this.failReservationUseCase = failReservationUseCase;
        this.subscription = subscription;
    }

    public void startScheduler() {
        if (subscription == null || subscription.isDisposed()) {
            subscription = Flux.interval(Duration.ZERO, Duration.ofSeconds(properties.getPeriodFluxSeconds()))
                    .flatMap(tick -> failReservationUseCase.failReservations(properties.getReservationTimeoutSeconds()))
                    .onErrorContinue((ex, obj) ->
                        log.error("Error en timeout de reserva: {}", ex.getMessage()))
                    .subscribe();
        }
    }

    public void stopScheduler() {
        if (subscription != null) {
            subscription.dispose();
        }
    }
}
