package com.aug.flightbooking.domain.models.reservation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationStateMachineTest {

    @Test
    void shouldAllowTransitionFromCreatedToPending() {
        // Verifica que una reserva pueda pasar de CREATED a PENDING
        boolean result = ReservationStateMachine.canTransition(
                ReservationStatus.CREATED,
                ReservationStatus.PENDING
        );
        assertTrue(result);
    }

    @Test
    void shouldAllowTransitionFromCreatedToFailed() {
        // Verifica que una reserva pueda pasar de CREATED a FAILED
        boolean result = ReservationStateMachine.canTransition(
                ReservationStatus.CREATED,
                ReservationStatus.FAILED
        );
        assertTrue(result);
    }

    @Test
    void shouldNotAllowTransitionFromCreatedToConfirmed() {
        // Verifica que una reserva no pueda pasar de CREATED a CONFIRMED
        boolean result = ReservationStateMachine.canTransition(
                ReservationStatus.CREATED,
                ReservationStatus.CONFIRMED
        );
        assertFalse(result);
    }

    @Test
    void shouldAllowTransitionFromPendingToEmitted() {
        // Verifica que una reserva pueda pasar de PENDING a EMITTED
        boolean result = ReservationStateMachine.canTransition(
                ReservationStatus.PENDING,
                ReservationStatus.EMITTED
        );
        assertTrue(result);
    }

    @Test
    void shouldNotAllowTransitionFromConfirmedToEmitted() {
        // Verifica que una reserva no pueda regresar de CONFIRMED a EMITTED
        boolean result = ReservationStateMachine.canTransition(
                ReservationStatus.CONFIRMED,
                ReservationStatus.EMITTED
        );
        assertFalse(result);
    }

    @Test
    void shouldNotAllowAnyTransitionFromRejected() {
        // Verifica que no exista transición posible desde REJECTED
        for (ReservationStatus to : ReservationStatus.values()) {
            boolean result = ReservationStateMachine.canTransition(
                    ReservationStatus.REJECTED,
                    to
            );
            assertFalse(result, "Transition from REJECTED to " + to + " should not be allowed");
        }
    }
}
