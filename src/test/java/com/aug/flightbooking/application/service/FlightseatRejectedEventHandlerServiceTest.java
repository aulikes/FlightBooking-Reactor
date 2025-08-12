package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.FlightseatRejectedEvent;
import com.aug.flightbooking.application.ports.out.ReservationCache;
import com.aug.flightbooking.domain.models.reservation.ReservationStatusAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para FlightseatRejectedEventHandlerService.
 * Se valida que actualice el estado a REJECTED y que cancele el timeout en caché.
 */
@ExtendWith(MockitoExtension.class)
class FlightseatRejectedEventHandlerServiceTest {

    @Mock
    private ReservationStatusUpdater reservationStatusUpdater;

    @Mock
    private ReservationCache reservationCache;

    @InjectMocks
    private FlightseatRejectedEventHandlerService service;

    @Test
    @DisplayName("handle(): debe actualizar a REJECTED y cancelar timeout")
    void handle_happy_path() {
        // Se prepara un evento con reservationId y razón de rechazo
        FlightseatRejectedEvent event = new FlightseatRejectedEvent(111L, "Sin cupos");

        // Se stubbean interacciones exitosas
        when(reservationStatusUpdater.updateStatus(eq(111L), eq("Sin cupos"), eq(ReservationStatusAction.REJECTED)))
                .thenReturn(Mono.empty());
        when(reservationCache.cancelTimeout(111L)).thenReturn(Mono.empty());

        // Se ejecuta el caso de uso
        StepVerifier.create(service.handle(event))
                .verifyComplete();

        // Se verifica actualización a REJECTED
        verify(reservationStatusUpdater, times(1))
                .updateStatus(111L, "Sin cupos", ReservationStatusAction.REJECTED);

        // Se verifica cancelación de timeout
        verify(reservationCache, times(1)).cancelTimeout(111L);

        // Se comprueba el orden: primero actualizar, luego cancelar timeout
        InOrder inOrder = inOrder(reservationStatusUpdater, reservationCache);
        inOrder.verify(reservationStatusUpdater).updateStatus(111L, "Sin cupos", ReservationStatusAction.REJECTED);
        inOrder.verify(reservationCache).cancelTimeout(111L);
    }

    @Test
    @DisplayName("handle(): debe completar y aún intentar cancelar timeout si falla updateStatus (onErrorResume)")
    void handle_proceeds_when_updateStatus_errors() {
        FlightseatRejectedEvent event = new FlightseatRejectedEvent(222L, "Overbooking");

        when(reservationStatusUpdater.updateStatus(eq(222L), eq("Overbooking"), eq(ReservationStatusAction.REJECTED)))
                .thenReturn(Mono.error(new RuntimeException("Fallo al actualizar")));
        when(reservationCache.cancelTimeout(222L)).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(event))
                .verifyComplete();

        // A pesar del error en updateStatus, se intenta cancelar el timeout
        verify(reservationCache, times(1)).cancelTimeout(222L);
    }

    @Test
    @DisplayName("handle(): debe completar aunque falle cancelTimeout (onErrorResume)")
    void handle_ignores_cache_errors() {
        FlightseatRejectedEvent event = new FlightseatRejectedEvent(333L, "Ruta cerrada");

        when(reservationStatusUpdater.updateStatus(eq(333L), eq("Ruta cerrada"), eq(ReservationStatusAction.REJECTED)))
                .thenReturn(Mono.empty());
        when(reservationCache.cancelTimeout(333L))
                .thenReturn(Mono.error(new IllegalStateException("Redis caído")));

        StepVerifier.create(service.handle(event))
                .verifyComplete();
    }
}
