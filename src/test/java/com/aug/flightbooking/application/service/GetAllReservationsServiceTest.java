package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.ports.out.ReservationRepository;
import com.aug.flightbooking.domain.models.reservation.Reservation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para GetAllReservationsService.
 * Verifica que delega en el repositorio y que los errores se propagan.
 */
@ExtendWith(MockitoExtension.class)
class GetAllReservationsServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private GetAllReservationsService service;

    @Test
    @DisplayName("getAllReservations(): retorna las reservas del repositorio en el mismo orden")
    void getAllReservations_returns_from_repository() {
        // Se mockean dos reservas (no es necesario construir el agregado real)
        Reservation r1 = mock(Reservation.class);
        Reservation r2 = mock(Reservation.class);

        when(reservationRepository.findAll()).thenReturn(Flux.just(r1, r2));

        StepVerifier.create(service.getAllReservations())
                .expectNext(r1, r2)
                .verifyComplete();

        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllReservations(): si el repositorio falla, el error se propaga")
    void getAllReservations_propagates_error() {
        when(reservationRepository.findAll()).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(service.getAllReservations())
                .expectError(RuntimeException.class)
                .verify();

        verify(reservationRepository, times(1)).findAll();
    }
}
