
package com.aug.flightbooking.infrastructure.init;

import com.aug.flightbooking.infrastructure.cache.ReservationTimeoutScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Inicia los procesos cuando el contexto ya está listo
 * y Redis ha respondido satisfactoriamente.
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class AppStartupFinalListener {

    private final ReservationTimeoutScheduler timeoutScheduler;
    private final ReactiveRedisConnectionFactory redisConnectionFactory;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        startRedisPolling();
    }

    private void startRedisPolling(){
        Flux.interval(Duration.ZERO, Duration.ofSeconds(5)) //El primer intento de inmediato, luego cada 5 seg
            .take(Duration.ofMinutes(2)) //Duración máxima de 2 minutos
            .flatMap(tick -> redisConnectionFactory.getReactiveConnection().ping())
            .doOnNext(pong -> log.info("Respuesta de Redis: {}", pong))
            .filter("PONG"::equalsIgnoreCase)
            .next()
            .doOnSuccess(pong -> {
                if (pong != null) {
                    log.info("Redis activo. Iniciando scheduler de reservas...");
                    timeoutScheduler.startScheduler();
                } else {
                    log.warn("Timeout: no se recibió PONG en 2 minutos.");
                }
            })
            .doOnError(error -> log.error("Error al verificar Redis: {}", error.getMessage()))
            .subscribe();
    }
}
