package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.events.ReservationEmittedEvent;
import com.aug.flightbooking.application.ports.out.ReservationCache;
import com.aug.flightbooking.application.ports.out.ReservationEmittedEventPublisher;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para FlightseatConfirmedEventHandlerService.
 * Se valida que actualice el estado a EMITTED, cancele el timeout y publique ReservationEmittedEvent.
 */
@ExtendWith(MockitoExtension.class)
class FlightseatConfirmedEventHandlerServiceTest {

    @Mock
    private ReservationStatusUpdater reservationStatusUpdater;

    @Mock
    private ReservationEmittedEventPublisher reservationEmittedEventPublisher;

    @Mock
    private ReservationCache reservationCache;

    @InjectMocks
    private FlightseatConfirmedEventHandlerService service;

    @Test
    @DisplayName("handle(): debe actualizar a EMITTED, cancelar timeout y publicar ReservationEmittedEvent")
    void handle_happy_path() {
        // Se prepara un evento con reservationId válido
        FlightseatConfirmedEvent event = new FlightseatConfirmedEvent(123L);

        // Se stubbean las interacciones para flujo exitoso
        when(reservationStatusUpdater.updateStatus(eq(123L), eq(""), eq(ReservationStatusAction.EMITTED)))
                .thenReturn(Mono.empty());
        when(reservationCache.cancelTimeout(123L)).thenReturn(Mono.empty());
        when(reservationEmittedEventPublisher.publish(any(ReservationEmittedEvent.class))).thenReturn(Mono.empty());

        // Se ejecuta el caso de uso y se verifica que complete
        StepVerifier.create(service.handle(event))
                .verifyComplete();

        // Se verifica que la actualización de estado se haya invocado con EMITTED
        verify(reservationStatusUpdater, times(1))
                .updateStatus(123L, "", ReservationStatusAction.EMITTED);

        // Se verifica cancelación de timeout y publicación del evento
        verify(reservationCache, times(1)).cancelTimeout(123L);
        verify(reservationEmittedEventPublisher, times(1))
                .publish(new ReservationEmittedEvent(123L));

        // Se verifica que la actualización de estado suceda antes del resto
        InOrder inOrder = inOrder(reservationStatusUpdater, reservationCache, reservationEmittedEventPublisher);
        inOrder.verify(reservationStatusUpdater).updateStatus(123L, "", ReservationStatusAction.EMITTED);
        // El orden relativo entre cancelTimeout y publish no se asegura; solo se verifica que ambos ocurran después
        inOrder.verify(reservationCache).cancelTimeout(123L);
        inOrder.verify(reservationEmittedEventPublisher).publish(new ReservationEmittedEvent(123L));
    }

    @Test
    @DisplayName("handle(): debe continuar con cancelTimeout y publish aunque falle updateStatus (onErrorResume)")
    void handle_proceeds_when_updateStatus_errors() {
        FlightseatConfirmedEvent event = new FlightseatConfirmedEvent(200L);

        when(reservationStatusUpdater.updateStatus(eq(200L), eq(""), eq(ReservationStatusAction.EMITTED)))
                .thenReturn(Mono.error(new RuntimeException("Fallo al actualizar")));
        when(reservationCache.cancelTimeout(200L)).thenReturn(Mono.empty());
        when(reservationEmittedEventPublisher.publish(any(ReservationEmittedEvent.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(event))
                .verifyComplete(); // El onErrorResume convierte el error en vacío y permite continuar

        verify(reservationCache, times(1)).cancelTimeout(200L);
        verify(reservationEmittedEventPublisher, times(1)).publish(new ReservationEmittedEvent(200L));
    }

    @Test
    @DisplayName("handle(): debe completar aunque falle cancelTimeout (onErrorResume)")
    void handle_ignores_cache_errors() {
        FlightseatConfirmedEvent event = new FlightseatConfirmedEvent(300L);

        when(reservationStatusUpdater.updateStatus(eq(300L), eq(""), eq(ReservationStatusAction.EMITTED)))
                .thenReturn(Mono.empty());
        when(reservationCache.cancelTimeout(300L)).thenReturn(Mono.error(new IllegalStateException("Redis caído")));
        when(reservationEmittedEventPublisher.publish(any(ReservationEmittedEvent.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(event))
                .verifyComplete();

        verify(reservationEmittedEventPublisher, times(1)).publish(new ReservationEmittedEvent(300L));
    }

    @Test
    @DisplayName("handle(): debe completar aunque falle publish (onErrorResume)")
    void handle_ignores_publish_errors() {
        FlightseatConfirmedEvent event = new FlightseatConfirmedEvent(400L);

        when(reservationStatusUpdater.updateStatus(eq(400L), eq(""), eq(ReservationStatusAction.EMITTED)))
                .thenReturn(Mono.empty());
        when(reservationCache.cancelTimeout(400L)).thenReturn(Mono.empty());
        when(reservationEmittedEventPublisher.publish(any(ReservationEmittedEvent.class)))
                .thenReturn(Mono.error(new RuntimeException("Kafka caído")));

        StepVerifier.create(service.handle(event))
                .verifyComplete();

        verify(reservationCache, times(1)).cancelTimeout(400L);
    }
}
