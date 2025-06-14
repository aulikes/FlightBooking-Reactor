
package com.aug.flightbooking.infrastructure.init;

import com.aug.flightbooking.infrastructure.cache.ReservationTimeoutScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Inicia los procesos cuando el contexto ya está listo
 * y Redis ha respondido satisfactoriamente.
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class AppStartupFinalListener {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    private final ReservationTimeoutScheduler timeoutScheduler;
    private final ReactiveRedisConnectionFactory redisConnectionFactory;
    private final FlightDataInitializer flightDataInitializer;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Esperando disponibilidad de Redis y Kafka...");

        Mono<Void> redisReady = waitForRedisReady();
        Mono<Void> kafkaReady = waitForKafkaReady();

        Mono.zip(redisReady, kafkaReady)
            .doOnSuccess(tuple -> {
                log.info("Redis y Kafka están listos, iniciando scheduler en segundo plano...");
                // Se lanza el Scheduler en un flujo paralelo.
                // No se encadena al flujo principal (no se espera su resultado).
                timeoutScheduler.startScheduler()
                    .subscribeOn(Schedulers.boundedElastic()) // Indica que se ejecute en un hilo de tipo "elastic" (apto para tareas largas, como el scheduler).
                    .subscribe(); // Inicia el flujo
            })
            .then(
                flightDataInitializer.init()
            )
            .doOnSuccess(v -> log.info("Inicialización completa")) // Esto se ejecuta cuando init() termina exitosamente
            .doOnError(ex -> {
                log.error("Error durante inicio de la aplicación: {}", ex.getMessage(), ex);
                // Detener el sistema si falla algo
                System.exit(1);
            })
            .subscribe();
    }

    private Mono<Void> startAppTasks() {
        return Mono.fromRunnable(() -> {
            log.info(">>> Redis y Kafka listos. Iniciando tareas programadas...");
            timeoutScheduler.startScheduler(); // o cualquier otra inicialización
        });
    }

    private Mono<Void> waitForRedisReady() {
        return Flux.interval(Duration.ZERO, Duration.ofSeconds(5))
                .take(Duration.ofMinutes(2))
                .flatMap(tick -> redisConnectionFactory.getReactiveConnection().ping()
                    .doOnNext(pong -> log.info("Redis respondió con: {}", pong))
                )
                .filter("PONG"::equalsIgnoreCase)
                .next()
                .switchIfEmpty(Mono.error(new IllegalStateException("Redis no respondió con PONG en 2 minutos")))
                .doOnSuccess(ok -> log.info("Redis está listo"))
                .then();
    }

    private Mono<Void> waitForKafkaReady() {
        return Mono.defer(() -> {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
            AdminClient client = AdminClient.create(props);

            return Flux.interval(Duration.ZERO, Duration.ofSeconds(5))
                .take(Duration.ofMinutes(2))
                .flatMap(tick -> Mono.fromCallable(() -> {
                        client.listTopics().names().get(1, TimeUnit.SECONDS);
                        return true;
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .onErrorResume(error -> {
                        log.warn("Kafka aún no está listo (intento {}). Detalle: {}", tick, error.getMessage());
                        return Mono.empty();
                    })
                )
                .next()
                .switchIfEmpty(Mono.error(new IllegalStateException("Kafka no respondió en 2 minutos")))
                .doOnSuccess(ok -> log.info("Kafka está listo"))
                .doFinally(signal -> client.close())
                .then();
        });
    }
}
