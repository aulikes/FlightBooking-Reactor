package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.FlightseatRejectedEvent;
import com.aug.flightbooking.application.ports.out.ReservationCache;
import com.aug.flightbooking.domain.models.reservation.ReservationStatusAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class FlightseatRejectedEventHandlerServiceTest {

    private ReservationStatusUpdater reservationStatusUpdater;
    private ReservationCache reservationCache;
    private FlightseatRejectedEventHandlerService handler;

    @BeforeEach
    void setUp() {
        reservationStatusUpdater = mock(ReservationStatusUpdater.class);
        reservationCache = mock(ReservationCache.class);

        handler = new FlightseatRejectedEventHandlerService(reservationStatusUpdater, reservationCache);
    }

    @Test
    void shouldUpdateStatusToRejectedAndCancelTimeout() {
        // Se define el ID de reserva simulado y la razón de rechazo
        Long reservationId = 1L;
        String reason = "Vuelo lleno";

        // Se simulan las respuestas exitosas de los mocks
        when(reservationStatusUpdater.updateStatus(reservationId, reason, ReservationStatusAction.REJECTED))
                .thenReturn(Mono.empty());
        when(reservationCache.cancelTimeout(reservationId)).thenReturn(Mono.empty());

        // Se construye el evento de rechazo
        FlightseatRejectedEvent event = new FlightseatRejectedEvent(reservationId, reason);

        // Se verifica que el flujo se complete exitosamente
        StepVerifier.create(handler.handle(event))
                .verifyComplete();

        // Se verifica que se llamaron las dependencias con los argumentos esperados
        verify(reservationStatusUpdater).updateStatus(reservationId, reason, ReservationStatusAction.REJECTED);
        verify(reservationCache).cancelTimeout(reservationId);
    }
}
