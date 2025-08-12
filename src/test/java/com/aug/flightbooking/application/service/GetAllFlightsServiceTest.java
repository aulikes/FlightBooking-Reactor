package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.ports.out.FlightRepository;
import com.aug.flightbooking.domain.models.flight.Flight;
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
 * Pruebas unitarias para GetAllFlightsService.
 * Verifica que delega correctamente en el repositorio y propaga errores.
 */
@ExtendWith(MockitoExtension.class)
class GetAllFlightsServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private GetAllFlightsService service;

    @Test
    @DisplayName("getAllFlights(): retorna los vuelos del repositorio en el mismo orden")
    void getAllFlights_returns_from_repository() {
        // Se mockean dos vuelos (no se requiere construir el agregado real para esta prueba)
        Flight f1 = mock(Flight.class);
        Flight f2 = mock(Flight.class);

        when(flightRepository.findAll()).thenReturn(Flux.just(f1, f2));

        StepVerifier.create(service.getAllFlights())
                .expectNext(f1, f2)
                .verifyComplete();

        verify(flightRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllFlights(): si el repositorio falla, el error se propaga")
    void getAllFlights_propagates_error() {
        when(flightRepository.findAll()).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(service.getAllFlights())
                .expectError(RuntimeException.class)
                .verify();

        verify(flightRepository, times(1)).findAll();
    }
}
