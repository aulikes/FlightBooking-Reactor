package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.port.out.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Servicio que maneja los timeouts de reservas usando Redis.
 *
 * - Cuando se crea una reserva, se registra un timeout en Redis con TTL.
 * - Si la reserva es confirmada o rechazada antes de ese tiempo, se elimina el timeout.
 * - Si nadie responde (ej. Flight no publica nada), podemos verificar si expiró
 *   y marcar la reserva como FALLIDA automáticamente.
 */
@Service
@RequiredArgsConstructor
public class ReservationTimeoutService {

    // Prefijo que se usa en las claves de Redis para separar las reservas
    private static final String KEY_PREFIX = "reservation:timeout:";

    // Cliente para operaciones simples de Redis tipo clave-valor (String -> String)
    private final ReactiveValueOperations<String, String> redisOps;

    // Repositorio para consultar y actualizar la entidad Reservation
    private final ReservationRepository reservationRepository;

    private final ApplicationProperties properties;


    /**
     * Registra el timeout en Redis cuando se crea una reserva.
     * Redis eliminará automáticamente esta clave al pasar el TTL.
     */
    public Mono<Void> registerTimeout(Long reservationId) {
        String key = KEY_PREFIX + reservationId;

        // TTL: cuántos segundos esperar antes de que Redis elimine esta clave
        Duration ttl = Duration.ofSeconds(properties.getReservationTimeoutSeconds());

        // Guardamos en Redis: clave = reservation:timeout:{id}, valor = "WAITING", TTL = 5 minutos (por ejemplo)
        return redisOps.set(key, "WAITING", ttl).then();
    }

    /**
     * Cancela el timeout si la reserva fue confirmada o rechazada.
     * Esto evita que luego sea marcada como fallida innecesariamente.
     */
    public Mono<Void> cancelTimeout(Long reservationId) {
        return redisOps.delete(KEY_PREFIX + reservationId).then();
    }

    /**
     * Se ejecuta después del tiempo esperado para validar si la reserva sigue en estado CREATED.
     * Si es así, se marca como FALLIDA.
     *
     * Este método no lo invoca Redis automáticamente.
     * Nosotros debemos llamarlo con un Scheduler o cron que verifique las reservas vencidas.
     */
    public Mono<Void> checkAndFailIfExpired(Long reservationId) {
        String key = KEY_PREFIX + reservationId;

        // Verificamos si la clave aún existe (es decir, si el TTL no ha vencido).
        return redisOps.get(key)
                .flatMap(value -> {
                    // Si la clave aún existe, no hacemos nada.
                    if (value == null) return Mono.empty();

                    // Si existe, aún está en tiempo de espera.
                    return reservationRepository.findById(reservationId)
                            .flatMap(reservation -> {
                                // Solo se puede fallar si aún está en estado CREATED
                                if (reservation.isCreated()) {
                                    reservation.markAsFailed();
                                    return reservationRepository.save(reservation);
                                }
                                // Si ya cambió de estado, no se hace nada.
                                return Mono.empty();
                            });
                })
                .then(); // Devolvemos Mono<Void>
    }
}
