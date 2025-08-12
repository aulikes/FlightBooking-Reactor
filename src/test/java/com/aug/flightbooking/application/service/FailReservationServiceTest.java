package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.ports.out.ReservationCache;
import com.aug.flightbooking.application.ports.out.ReservationRepository;
import com.aug.flightbooking.domain.models.reservation.PassengerInfo;
import com.aug.flightbooking.domain.models.reservation.Reservation;
import com.aug.flightbooking.domain.models.reservation.ReservationStatus;
import com.aug.flightbooking.domain.models.reservation.ReservationStatusAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para FailReservationService.
 * Se valida que identifique reservas vencidas y actualice su estado a FAILED.
 */
@ExtendWith(MockitoExtension.class)
class FailReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationCache reservationCache;

    @Mock
    private ReservationStatusUpdater reservationStatusUpdater;

    @InjectMocks
    private FailReservationService service;

    private Reservation buildReservation(long id, ReservationStatus status, Instant createdAt) {
        // Se construye la reserva con estado y fechas controladas para la prueba
        PassengerInfo p = new PassengerInfo("John Doe", "CC-1");
        return Reservation.fromPersistence(id, 77L, p, status, createdAt, null);
    }

    @Test
    @DisplayName("failReservations(): debe marcar FAILED cuando la reserva está vencida y no está en caché (MISSING)")
    void fail_whenMissingInCache() {
        // Se prepara una reserva con createdAt en el pasado
        Instant createdAt = Instant.now().minusSeconds(7200);
        Reservation r = buildReservation(10L, ReservationStatus.CREATED, createdAt);

        // Se simula que el repositorio devuelve esa reserva para los estados CREATED y PENDING
        when(reservationRepository.findReservationsBefore(any(Instant.class), eq(List.of(ReservationStatus.CREATED.name(), ReservationStatus.PENDING.name()))))
                .thenReturn(Flux.just(r));

        // Se simula que en caché no hay valor (defaultIfEmpty -> "MISSING")
        when(reservationCache.get(10L)).thenReturn(Mono.empty());

        // Se stubbea la actualización de estado a FAILED sin error
        when(reservationStatusUpdater.updateStatus(eq(10L), anyString(), eq(ReservationStatusAction.FAILED)))
                .thenReturn(Mono.empty());

        // Se ejecuta el caso de uso
        StepVerifier.create(service.failReservations(3600))
                .verifyComplete();

        // Se capturan los argumentos del mensaje para validar contenido relevante
        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);
        verify(reservationStatusUpdater, times(1))
                .updateStatus(eq(10L), msgCaptor.capture(), eq(ReservationStatusAction.FAILED));

        String msg = msgCaptor.getValue();
        // Se verifica que el mensaje refleje que no estaba en caché y contenga marcas de timeout
        assertTrue(msg.contains("NO encontrada en la caché"), "El mensaje debe indicar ausencia en caché");
        assertTrue(msg.contains("TimeOut"), "El mensaje debe indicar timeout");
        assertTrue(msg.contains("Created At"), "El mensaje debe incluir la fecha de creación");
    }

    @Test
    @DisplayName("failReservations(): debe marcar FAILED aunque exista un valor en caché")
    void fail_whenPresentInCache() {
        // Se prepara una reserva pendiente y antigua
        Instant createdAt = Instant.now().minusSeconds(7200);
        Reservation r = buildReservation(11L, ReservationStatus.PENDING, createdAt);

        when(reservationRepository.findReservationsBefore(any(Instant.class), anyList()))
                .thenReturn(Flux.just(r));

        // Se simula que la caché tiene algún valor
        when(reservationCache.get(11L)).thenReturn(Mono.just("OK"));

        when(reservationStatusUpdater.updateStatus(eq(11L), anyString(), eq(ReservationStatusAction.FAILED)))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.failReservations(3600))
                .verifyComplete();

        // Se valida que se intentó actualizar a FAILED
        verify(reservationStatusUpdater, times(1))
                .updateStatus(eq(11L), anyString(), eq(ReservationStatusAction.FAILED));
    }

    @Test
    @DisplayName("failReservations(): debe completar sin acciones cuando no hay reservas vencidas")
    void no_expired_reservations() {
        when(reservationRepository.findReservationsBefore(any(Instant.class), anyList()))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.failReservations(3600))
                .verifyComplete();

        verify(reservationStatusUpdater, never()).updateStatus(anyLong(), anyString(), any());
    }

    @Test
    @DisplayName("failReservations(): debe propagar error si la actualización de estado falla")
    void propagate_error_from_updater() {
        Instant createdAt = Instant.now().minusSeconds(7200);
        Reservation r = buildReservation(12L, ReservationStatus.CREATED, createdAt);

        when(reservationRepository.findReservationsBefore(any(Instant.class), anyList()))
                .thenReturn(Flux.just(r));

        when(reservationCache.get(12L)).thenReturn(Mono.empty());

        when(reservationStatusUpdater.updateStatus(eq(12L), anyString(), eq(ReservationStatusAction.FAILED)))
                .thenReturn(Mono.error(new IllegalStateException("No se pudo actualizar")));

        StepVerifier.create(service.failReservations(3600))
                .expectError(IllegalStateException.class)
                .verify();
    }
}
