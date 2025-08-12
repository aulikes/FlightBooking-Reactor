package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.ports.out.ReservationRepository;
import com.aug.flightbooking.domain.exceptions.ReservationChangeStatusException;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas de unidad para ReservationStatusUpdater.
 * Cubre el método que recibe (id, msg, action) y el que recibe (reservation, action).
 */
@ExtendWith(MockitoExtension.class)
class ReservationStatusUpdaterTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationStatusUpdater updater;

    private Reservation buildReservation(long id, ReservationStatus status) {
        PassengerInfo p = new PassengerInfo("John Doe", "CC-1");
        return Reservation.fromPersistence(id, 777L, p, status, Instant.now().minusSeconds(3600), null);
    }

    @Test
    @DisplayName("updateStatus(id,msg,action): aplica transición válida, setea mensaje y guarda")
    void update_byId_success_sets_message_and_saves() {
        Reservation existing = buildReservation(10L, ReservationStatus.EMITTED); // EMITTED -> CONFIRMED es válida
        when(reservationRepository.findById(10L)).thenReturn(Mono.just(existing));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(Mono.just(existing));

        StepVerifier.create(updater.updateStatus(10L, "ok", ReservationStatusAction.CONFIRMED))
                .verifyComplete();

        // Captura y verifica que el mensaje haya sido seteado y el estado cambiado
        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(captor.capture());
        Reservation saved = captor.getValue();
        assertThat(saved.getMessage()).isEqualTo("ok");
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("updateStatus(id,msg,action): si no existe la reserva -> IllegalArgumentException")
    void update_byId_not_found() {
        when(reservationRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(updater.updateStatus(99L, "msg", ReservationStatusAction.CONFIRMED))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus(id,msg,action): si falla save -> propaga error")
    void update_byId_save_error() {
        Reservation existing = buildReservation(11L, ReservationStatus.EMITTED);
        when(reservationRepository.findById(11L)).thenReturn(Mono.just(existing));
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(Mono.error(new RuntimeException("DB down")));

        StepVerifier.create(updater.updateStatus(11L, "x", ReservationStatusAction.CONFIRMED))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("updateStatus(reservation, action): transición válida y guardado ok -> completa")
    void update_byEntity_success() {
        Reservation existing = buildReservation(20L, ReservationStatus.PENDING); // PENDING -> EMITTED válida
        when(reservationRepository.save(any(Reservation.class))).thenReturn(Mono.just(existing));

        StepVerifier.create(updater.updateStatus(existing, ReservationStatusAction.EMITTED))
                .verifyComplete();

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ReservationStatus.EMITTED);
    }

    @Test
    @DisplayName("updateStatus(reservation, action): transición inválida -> ReservationChangeStatusException")
    void update_byEntity_invalid_transition() {
        // EMITTED -> PENDING NO es válida según la state machine
        Reservation existing = buildReservation(21L, ReservationStatus.EMITTED);

        StepVerifier.create(updater.updateStatus(existing, ReservationStatusAction.PENDING))
                .expectError(ReservationChangeStatusException.class)
                .verify();

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatus(reservation, action): save falla -> propaga error")
    void update_byEntity_save_error() {
        Reservation existing = buildReservation(22L, ReservationStatus.PENDING);
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(updater.updateStatus(existing, ReservationStatusAction.EMITTED))
                .expectError(RuntimeException.class)
                .verify();
    }
}
