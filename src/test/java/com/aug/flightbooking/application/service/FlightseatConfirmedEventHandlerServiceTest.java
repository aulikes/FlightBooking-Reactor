package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.events.ReservationEmittedEvent;
import com.aug.flightbooking.application.ports.out.ReservationCache;
import com.aug.flightbooking.application.ports.out.ReservationEmittedEventPublisher;
import com.aug.flightbooking.application.service.ReservationStatusUpdater;
import com.aug.flightbooking.domain.models.reservation.ReservationStatusAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class FlightseatConfirmedEventHandlerServiceTest {

    private ReservationStatusUpdater reservationStatusUpdater;
    private ReservationEmittedEventPublisher reservationEmittedEventPublisher;
    private ReservationCache reservationCache;
    private FlightseatConfirmedEventHandlerService handler;

    @BeforeEach
    void setUp() {
        reservationStatusUpdater = mock(ReservationStatusUpdater.class);
        reservationEmittedEventPublisher = mock(ReservationEmittedEventPublisher.class);
        reservationCache = mock(ReservationCache.class);

        handler = new FlightseatConfirmedEventHandlerService(
                reservationStatusUpdater,
                reservationEmittedEventPublisher,
                reservationCache
        );
    }

    @Test
    void shouldHandleConfirmedEventAndUpdateStatusAndPublishEvent() {
        // Se define el ID de reserva simulado
        Long reservationId = 1L;

        // Se simulan los comportamientos esperados de los mocks
        when(reservationStatusUpdater.updateStatus(reservationId, "", ReservationStatusAction.EMITTED))
                .thenReturn(Mono.empty());

        when(reservationCache.cancelTimeout(reservationId)).thenReturn(Mono.empty());
        when(reservationEmittedEventPublisher.publish(new ReservationEmittedEvent(reservationId)))
                .thenReturn(Mono.empty());

        // Se crea el evento a manejar
        FlightseatConfirmedEvent event = new FlightseatConfirmedEvent(reservationId);

        // Se verifica que el flujo se complete exitosamente
        StepVerifier.create(handler.handle(event))
                .verifyComplete();

        // Se verifica que se hayan llamado los mocks
        verify(reservationStatusUpdater).updateStatus(reservationId, "", ReservationStatusAction.EMITTED);
        verify(reservationCache).cancelTimeout(reservationId);
        verify(reservationEmittedEventPublisher).publish(new ReservationEmittedEvent(reservationId));
    }
}
