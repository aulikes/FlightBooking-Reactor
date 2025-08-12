package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.TicketCreatedEvent;
import com.aug.flightbooking.application.ports.out.ReservationRepository;
import com.aug.flightbooking.domain.models.reservation.PassengerInfo;
import com.aug.flightbooking.domain.models.reservation.Reservation;
import com.aug.flightbooking.domain.models.reservation.ReservationStatus;
import com.aug.flightbooking.domain.models.reservation.ReservationStatusAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ReservationConfirmedEventHandlerService.
 * Se valida que, al recibir TicketCreatedEvent, confirme la reserva existente.
 */
@ExtendWith(MockitoExtension.class)
class ReservationConfirmedEventHandlerServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationStatusUpdater reservationStatusUpdater;

    @InjectMocks
    private ReservationConfirmedEventHandlerService service;

    private Reservation buildReservation(long id, ReservationStatus status) {
        // Se construye una reserva con el estado indicado para probar transiciones
        PassengerInfo p = new PassengerInfo("John Doe", "CC-9");
        return Reservation.fromPersistence(id, 77L, p, status, Instant.now().minusSeconds(3600), null);
    }

    @Test
    @DisplayName("handle(): debe confirmar la reserva encontrada (EMITTED -> CONFIRMED)")
    void handle_happy_path_confirms_reservation() {
        TicketCreatedEvent event = new TicketCreatedEvent(55L, "Ticket emitido");

        // Se retorna una reserva en estado EMITTED para permitir la transición a CONFIRMED
        Reservation existing = buildReservation(55L, ReservationStatus.EMITTED);
        when(reservationRepository.findById(55L)).thenReturn(Mono.just(existing));

        // Se stubbea la actualización como exitosa
        when(reservationStatusUpdater.updateStatus(eq(existing), eq(ReservationStatusAction.CONFIRMED)))
                .thenReturn(Mono.empty());

        // Se ejecuta el handler
        StepVerifier.create(service.handle(event))
                .verifyComplete();

        // Se verifica la llamada al actualizador con la acción CONFIRMED
        verify(reservationStatusUpdater, times(1))
                .updateStatus(existing, ReservationStatusAction.CONFIRMED);
    }

    @Test
    @DisplayName("handle(): debe completar sin acciones si la reserva no existe")
    void handle_no_reservation_found() {
        TicketCreatedEvent event = new TicketCreatedEvent(66L, "mensaje");

        when(reservationRepository.findById(66L)).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(event))
                .verifyComplete();

        // No debe intentar actualizar estado si no existe la reserva
        verify(reservationStatusUpdater, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("handle(): debe completar aunque falle updateStatus (onErrorResume)")
    void handle_ignores_update_errors() {
        TicketCreatedEvent event = new TicketCreatedEvent(77L, "ticket");
        Reservation existing = buildReservation(77L, ReservationStatus.EMITTED);

        when(reservationRepository.findById(77L)).thenReturn(Mono.just(existing));
        when(reservationStatusUpdater.updateStatus(eq(existing), eq(ReservationStatusAction.CONFIRMED)))
                .thenReturn(Mono.error(new IllegalStateException("Transición inválida")));

        StepVerifier.create(service.handle(event))
                .verifyComplete();
    }
}
