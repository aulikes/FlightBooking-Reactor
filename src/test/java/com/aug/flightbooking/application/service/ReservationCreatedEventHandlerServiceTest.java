package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.FlightseatConfirmedEvent;
import com.aug.flightbooking.application.events.FlightseatRejectedEvent;
import com.aug.flightbooking.application.events.ReservationCreatedEvent;
import com.aug.flightbooking.application.ports.out.FlightRepository;
import com.aug.flightbooking.application.ports.out.FlightseatConfirmedEventPublisher;
import com.aug.flightbooking.application.ports.out.FlightseatRejectedEventPublisher;
import com.aug.flightbooking.domain.models.flight.Flight;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ReservationCreatedEventHandlerService.
 * Verifica publicación de eventos según disponibilidad de asientos.
 */
@ExtendWith(MockitoExtension.class)
class ReservationCreatedEventHandlerServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightseatConfirmedEventPublisher confirmedPublisher;

    @Mock
    private FlightseatRejectedEventPublisher rejectedPublisher;

    @InjectMocks
    private ReservationCreatedEventHandlerService service;

    @Test
    @DisplayName("handle(): vuelo encontrado con asiento disponible publica FlightseatConfirmedEvent")
    void handle_flight_with_seat_confirms() {
        // Se arma el evento con los 4 campos requeridos
        ReservationCreatedEvent event =
                new ReservationCreatedEvent(2L, 200L, "John Doe", "CC-123");

        // Se simula vuelo con asiento disponible
        Flight flight = mock(Flight.class);
        when(flight.tryReserveSeat()).thenReturn(true);
        when(flightRepository.findById(200L)).thenReturn(Mono.just(flight));
        when(flightRepository.save(flight)).thenReturn(Mono.just(flight));
        when(confirmedPublisher.publish(any(FlightseatConfirmedEvent.class))).thenReturn(Mono.empty());

        // Se ejecuta y completa sin errores
        StepVerifier.create(service.handle(event)).verifyComplete();

        // Se verifica publicación de evento confirmado y persistencia del vuelo
        verify(confirmedPublisher, times(1)).publish(new FlightseatConfirmedEvent(2L));
        verify(flightRepository, times(1)).save(flight);
        verifyNoInteractions(rejectedPublisher);
    }

    @Test
    @DisplayName("handle(): vuelo encontrado sin asiento disponible publica FlightseatRejectedEvent")
    void handle_flight_without_seat_rejects() {
        ReservationCreatedEvent event =
                new ReservationCreatedEvent(3L, 300L, "Jane Roe", "CC-999");

        // Se simula vuelo sin asiento
        Flight flight = mock(Flight.class);
        when(flight.tryReserveSeat()).thenReturn(false);
        when(flightRepository.findById(300L)).thenReturn(Mono.just(flight));
        when(rejectedPublisher.publish(any(FlightseatRejectedEvent.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(event)).verifyComplete();

        // Se verifica publicación de evento rechazado
        verify(rejectedPublisher, times(1))
                .publish(new FlightseatRejectedEvent(3L, "No Seat"));
        verifyNoInteractions(confirmedPublisher);
        verify(flightRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle(): si el vuelo no existe, no publica eventos")
    void handle_no_flight_found() {
        ReservationCreatedEvent event =
                new ReservationCreatedEvent(4L, 400L, "Alice", "CC-111");

        when(flightRepository.findById(400L)).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(event)).verifyComplete();

        verifyNoInteractions(confirmedPublisher, rejectedPublisher);
    }

    @Test
    @DisplayName("handle(): si ocurre error técnico, se captura y completa vacío")
    void handle_error_is_caught() {
        ReservationCreatedEvent event =
                new ReservationCreatedEvent(5L, 500L, "Bob", "CC-222");

        when(flightRepository.findById(500L))
                .thenReturn(Mono.error(new RuntimeException("Error técnico")));

        StepVerifier.create(service.handle(event)).verifyComplete();

        verifyNoInteractions(confirmedPublisher, rejectedPublisher);
    }
}
