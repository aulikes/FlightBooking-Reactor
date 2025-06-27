package com.aug.flightbooking.infrastructure.init;

import com.aug.flightbooking.infrastructure.cache.ReservationTimeoutScheduler;
import com.aug.flightbooking.infrastructure.messaging.listener.ReactiveListenersOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Component
@Slf4j
public class AppStartupFinalListener {

    private final ReactiveRedisConnectionFactory redisConnectionFactory;
    private final ReservationTimeoutScheduler timeoutScheduler;
    private final FlightDataInitializer flightDataInitializer;
    private final ReservationDataInitializer reservationDataInitializer;
    private final ReactiveListenersOrchestrator reactiveListenersOrchestrator;

    private final String kafkaBootstrapServers = "localhost:9092"; // reemplaza por properties si lo deseas

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Esperando disponibilidad de Redis y Kafka...");

        Mono<Void> redisReady = waitForRedisReady();
        Mono<Void> kafkaReady = waitForKafkaReady();

        /**
         * No se puede usar Mono.zip, debido a que este espera un resultado, y en este caso ambos son void
         * Si se usa Mono.zip en este caso, se ejecuta el contenido del flujo sin esperar al otro, por ser VOID
         */
        Mono.when(redisReady, kafkaReady)
            .doOnSuccess(v -> log.info("Redis y Kafka están listos..."))
//            .then(flightDataInitializer.init()) // Mono<List<FlightCreateResponse>>
            .then(Mono.defer(() -> {
                // redisReady y kafkaReady emiten un VOID, por eso se debe usar THEN, y no FLATMAP
                // Si se utiliza FLATMAP el flujo no entra debido a que espera un valor y los que se emite anteriormente es VOID
                // Se usa Mono.defer solo para poder imprimir el log, se puede quitar y ejecutar directamente flightDataInitializer.init()
                log.info("Inicio de creación de Vuelos");
                return flightDataInitializer.init(); // Mono<List<FlightCreateResponse>>
            }))
            .flatMap(flightResponses -> {
                log.info("Inicio de creación de Reservas");
                return reservationDataInitializer.init(flightResponses); // Mono<List<ReservationResponse>>
            })
            .then(Mono.defer(() -> {
                log.info("Activando listeners reactivamente...");
                return reactiveListenersOrchestrator.startAllListeners();
            }))
            .doOnSuccess(v -> {
                log.info("Inicialización completa");

                // Se lanza el Scheduler en un flujo paralelo.
                // No se encadena al flujo principal (no se espera su resultado).
                // Indica que se ejecute en un hilo de tipo "elastic" (apto para tareas largas, como el scheduler).
                timeoutScheduler.startSchedulerReservations()
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe();
            })
            .doOnError(ex -> {
                log.error("Error durante inicio de la aplicación: {}", ex.getMessage(), ex);
                System.exit(1);
            })
            .subscribe();
    }

    private Mono<Void> waitForRedisReady() {
        AtomicInteger attempt = new AtomicInteger(0);

        return Mono.defer(() -> {
                    int tick = attempt.getAndIncrement();
                    log.info("[{}] Enviando ping a Redis...", tick);
                    return redisConnectionFactory.getReactiveConnection().ping()
                            .doOnNext(pong -> log.info("[{}] Redis respondió con: {}", tick, pong))
                            .filter("PONG"::equalsIgnoreCase);
                })
                .repeatWhenEmpty(repeat -> repeat.delayElements(Duration.ofSeconds(5)))
                .timeout(Duration.ofMinutes(2))
                .doOnSuccess(pong -> log.info("Redis está listo"))
                .doOnError(e -> log.error("Redis no respondió a tiempo: {}", e.getMessage()))
                .then();
    }

    private Mono<Void> waitForKafkaReady() {
        AtomicInteger attempt = new AtomicInteger(0);

        return Mono.defer(() -> {
                int tick = attempt.getAndIncrement();
                log.info("[{}] Enviando request a Kafka...", tick);

                Properties props = new Properties();
                props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
                AdminClient adminClient = AdminClient.create(props);

                return Mono.fromFuture(adminClient.listTopics().names().toCompletionStage().toCompletableFuture())
                        .doFinally(signal -> adminClient.close())
                        .flatMap(topics -> {
                            if (topics != null && !topics.isEmpty()) {
                                log.info("[{}] Kafka respondió con topics: {}", tick, topics);
                                return Mono.just(true);
                            } else {
                                log.warn("[{}] Kafka no devolvió topics válidos todavía", tick);
                                return Mono.empty();
                            }
                        });
            })
            .repeatWhenEmpty(repeat -> repeat.delayElements(Duration.ofSeconds(5)))
            .timeout(Duration.ofMinutes(2)) // Esto sí falla correctamente
            .doOnSuccess(ok -> log.info("Kafka está listo"))
            .then();
    }

}
