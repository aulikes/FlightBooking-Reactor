package com.aug.flightbooking.infrastructure.init;

import com.aug.flightbooking.infrastructure.cache.ReservationTimeoutScheduler;
import com.aug.flightbooking.infrastructure.messaging.listener.ReactiveListenersOrchestrator;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class AppStartupFinalListener {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ReservationTimeoutScheduler timeoutScheduler;
    private final FlightDataInitializer flightDataInitializer;
    private final ReservationDataInitializer reservationDataInitializer;
    private final ReactiveListenersOrchestrator reactiveListenersOrchestrator;

    @Value("${app.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    public AppStartupFinalListener(
            @Qualifier("reservationRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
            ReservationTimeoutScheduler timeoutScheduler,
            FlightDataInitializer flightDataInitializer,
            ReservationDataInitializer reservationDataInitializer,
            ReactiveListenersOrchestrator reactiveListenersOrchestrator
    ) {
        this.redisTemplate = redisTemplate;
        this.timeoutScheduler = timeoutScheduler;
        this.flightDataInitializer = flightDataInitializer;
        this.reservationDataInitializer = reservationDataInitializer;
        this.reactiveListenersOrchestrator = reactiveListenersOrchestrator;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Esperando disponibilidad de Redis y Kafka...");

        Mono<Void> redisReady = waitForRedisReady();
        Mono<Void> kafkaReady = waitForKafkaReady();

        /*
          No se puede usar Mono.zip, debido a que este espera un resultado, y en este caso ambos son void
          Si se usa Mono.zip en este caso, se ejecuta el contenido del flujo sin esperar al otro, por ser VOID
         */
        Mono.when(redisReady, kafkaReady)
            .doOnSuccess(v -> log.info("Redis y Kafka están listos..."))
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
//                timeoutScheduler.startSchedulerReservations()
//                        .subscribeOn(Schedulers.boundedElastic())
//                        .subscribe();
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
            // Imprime en el log que se está intentando contactar a Redis
            log.info("[{}] Enviando SET de prueba a Redis...", tick);

            // Intenta guardar una clave temporal ("redis-healthcheck") con valor "ok" por 5 segundos
            return redisTemplate.opsForValue()
                    .set("redis-healthcheck", "ok", Duration.ofSeconds(5)) // TTL = 5 segundos
                    .doOnNext(success -> {
                        if (Boolean.TRUE.equals(success)) {
                            log.info("Redis respondió correctamente al SET");
                        } else {
                            log.warn("Redis respondió al SET, pero no confirmó éxito");
                        }
                    });
        })
        // Si falla, reintenta cada 5 segundos
        .repeatWhenEmpty(repeat -> repeat.delayElements(Duration.ofSeconds(5)))
        // Límite de espera total: 2 minutos
        .timeout(Duration.ofMinutes(2))
        .doOnSuccess(v -> log.info("Redis está listo"))
        .doOnError(e -> log.error("Redis no respondió a tiempo: {}", e.getMessage()))
        .then(); // Devuelve Mono<Void>
    }

    private Mono<Void> waitForKafkaReady() {
        AtomicInteger attempt = new AtomicInteger(0);

        return Mono.defer(() -> {
            int tick = attempt.getAndIncrement();
            log.info("[{}] Verificando disponibilidad del cluster Kafka...", tick);

            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
            AdminClient adminClient = AdminClient.create(props);

            return Mono.fromFuture(adminClient.describeCluster().nodes().toCompletionStage().toCompletableFuture())
                    .doFinally(signal -> adminClient.close())
                    .flatMap(nodes -> {
                        if (nodes != null && !nodes.isEmpty()) {
                            log.info("[{}] Kafka cluster está accesible con {} nodo(s): {}", tick, nodes.size(), nodes);
                            return Mono.just(true);
                        } else {
                            log.warn("[{}] Kafka respondió sin nodos activos", tick);
                            return Mono.empty();
                        }
                    });
        })
        .repeatWhenEmpty(repeat -> repeat.delayElements(Duration.ofSeconds(5)))
        .timeout(Duration.ofMinutes(2))
        .doOnSuccess(ok -> log.info("Kafka está listo"))
        .doOnError(e -> log.error("Kafka no respondió a tiempo: {}", e.getMessage(), e))
        .then();
    }


}
