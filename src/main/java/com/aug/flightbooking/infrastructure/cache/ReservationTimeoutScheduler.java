
package com.aug.flightbooking.infrastructure.cache;

import com.aug.flightbooking.application.ports.in.FailReservationUseCase;
import com.aug.flightbooking.infrastructure.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public ReservationTimeoutScheduler(AppProperties properties,
                                       FailReservationUseCase failReservationUseCase) {
        this.properties = properties.getRedis().getRedisReservation();
        this.failReservationUseCase = failReservationUseCase;
    }

    public Flux<Void> startSchedulerReservations() {
        log.info("Iniciando Scheduler ReservationTimeoutScheduler, every {} seconds", properties.getPeriodFluxSeconds());
        return Flux.interval(Duration.ZERO, Duration.ofSeconds(30L))
            .doOnSubscribe(s -> log.info("Flux.interval suscrito"))
            // Loguea cada tick recibido
            .doOnNext(tick -> log.debug("Tick recibido: {}", tick))

            // Para cada tick, llamamos a failReservations y medimos su duración
            .flatMap(tick ->
                failReservationUseCase
                    .failReservations(properties.getReservationTimeoutSeconds())
                    // elapsed() convierte el Mono<Void> en Mono<Tuple2<Long, Void>>
                    .elapsed()
                    // doOnNext recibe un Tuple2<duraciónMs, Void>
                    .doOnNext(tuple -> {
                        long durationMs = tuple.getT1();
                        log.info("failReservations completado en {} ms para tick #{}", durationMs, tick);
                    })
                    .then()
            )
            .doOnError(e -> log.error("Error detectado antes de continuar: {}", e.getMessage(), e))
            .onErrorContinue((ex, obj) ->
                    log.error("Error en timeout de reserva: {}", ex.getMessage()));
    }
}
