package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.commands.CreateFlightCommand;
import com.aug.flightbooking.application.ports.out.FlightRepository;
import com.aug.flightbooking.domain.models.flight.Flight;
import com.aug.flightbooking.domain.models.flight.FlightStatus;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para CreateFlightService.
 * Se verifica el mapeo del comando al agregado y la interacción con el repositorio.
 */
@ExtendWith(MockitoExtension.class)
class CreateFlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private CreateFlightService service;

    @Test
    @DisplayName("create(): debe construir el agregado y retornar el vuelo guardado con ID")
    void create_success() {
        // Se define un comando consistente con fechas válidas
        Instant dep = Instant.now().plusSeconds(3600);     // salida en 1 hora
        Instant arr = dep.plusSeconds(7200);               // llegada 2 horas después
        CreateFlightCommand cmd = new CreateFlightCommand(
                "Aerolínea X", "AX", "AX123",
                "BOG", "MDE",
                180, 0, dep, arr
        );

        // Se simula que el repositorio asigna ID al persistir.
        // Se captura el Flight que llega a save() para validar su contenido, y se retorna una copia con ID.
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> {
            Flight toSave = inv.getArgument(0);
            Flight saved = Flight.fromPersistence(
                    1L,
                    toSave.getAirline(),
                    toSave.getFlightCode(),
                    toSave.getOrigin(),
                    toSave.getDestination(),
                    toSave.getTotalSeats(),
                    toSave.getReservedSeats(),
                    toSave.getScheduledDeparture(),
                    toSave.getScheduledArrival(),
                    toSave.getStatus()
            );
            return Mono.just(saved);
        });

        // Se ejecuta el caso de uso
        StepVerifier.create(service.create(cmd))
                .assertNext(saved -> {
                    // Se verifica que el repositorio retornó el vuelo con ID y estado inicial SCHEDULED
                    assertThat(saved.getId()).isEqualTo(1L);
                    assertThat(saved.getStatus()).isEqualTo(FlightStatus.SCHEDULED);

                    // Se verifica que los valores del agregado sean los esperados
                    assertThat(saved.getAirline()).isNotNull();
                    assertThat(saved.getAirline().getName()).isEqualTo("Aerolínea X");
                    assertThat(saved.getAirline().getCode()).isEqualTo("AX");
                    assertThat(saved.getFlightCode()).isEqualTo("AX123");
                    assertThat(saved.getOrigin()).isEqualTo("BOG");
                    assertThat(saved.getDestination()).isEqualTo("MDE");
                    assertThat(saved.getTotalSeats()).isEqualTo(180);
                    assertThat(saved.getReservedSeats()).isEqualTo(0);
                    assertThat(saved.getScheduledDeparture()).isEqualTo(dep);
                    assertThat(saved.getScheduledArrival()).isEqualTo(arr);
                })
                .verifyComplete();

        // Se asegura que se llamó save() exactamente una vez con un Flight construido desde el comando
        ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
        verify(flightRepository, times(1)).save(captor.capture());
        Flight sent = captor.getValue();
        assertThat(sent.getId()).isNull(); // Antes de persistir, el agregado no debe tener ID
        assertThat(sent.getStatus()).isEqualTo(FlightStatus.SCHEDULED);
    }

    @Test
    @DisplayName("create(): debe propagar error si el comando es inválido (por ejemplo airlineName null)")
    void create_invalid_nulls() {
        // Se define un comando con airlineName null para gatillar validación obligatoria
        CreateFlightCommand cmd = new CreateFlightCommand(
                null, "AX", "AX123",
                "BOG", "MDE",
                180, 0, Instant.now().plusSeconds(3600), Instant.now().plusSeconds(10800)
        );

        // Se verifica que la NPE sucede al intentar construir Airline/Flight de forma inmediata
        assertThrows(NullPointerException.class, () -> service.create(cmd));

        // Se valida que nunca se intentó guardar al fallar la construcción del agregado
        verify(flightRepository, never()).save(any());
    }

}
