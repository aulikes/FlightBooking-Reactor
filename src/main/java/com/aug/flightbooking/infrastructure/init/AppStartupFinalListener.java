package com.aug.flightbooking.infrastructure.init;

import com.aug.flightbooking.infrastructure.cache.ReservationTimeoutScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.ReceiverOptions;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Component
@Slf4j
public class AppStartupFinalListener {

    private final ReactiveRedisConnectionFactory redisConnectionFactory;
    private final ReservationTimeoutScheduler timeoutScheduler;
    private final FlightDataInitializer flightDataInitializer;

    private final String kafkaBootstrapServers = "localhost:9092"; // reemplaza por properties si lo deseas

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Esperando disponibilidad de Redis y Kafka...");

        Mono<Void> redisReady = waitForRedisReady();
        Mono<Void> kafkaReady = waitForKafkaReady();

        Mono.when(redisReady, kafkaReady)
            .doOnSuccess(tuple -> {
                log.info("Redis y Kafka están listos...");
            })
            .then(
                flightDataInitializer.init()
                    .doOnSuccess(v -> log.info("Inicialización de datos de vuelo completada"))
            )
            .doOnError(ex -> {
                log.error("Error durante inicio de la aplicación: {}", ex.getMessage(), ex);
                // Detener el sistema si falla algo
                System.exit(1);
            })
            .doOnSuccess(
                v -> {
                    log.info("Inicialización completa");

                    // Se lanza el Scheduler en un flujo paralelo.
                    // No se encadena al flujo principal (no se espera su resultado).
                    // Indica que se ejecute en un hilo de tipo "elastic" (apto para tareas largas, como el scheduler).
                    timeoutScheduler.startSchedulerReservations()
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(); // Aquí sí se lanza por fuera, en paralelo
                }
            ) // Esto se ejecuta cuando init() termina exitosamente
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
