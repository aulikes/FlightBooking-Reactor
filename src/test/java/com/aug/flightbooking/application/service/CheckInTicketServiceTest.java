package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.commands.CreateCheckInCommand;
import com.aug.flightbooking.application.ports.out.TicketRepository;
import com.aug.flightbooking.domain.models.ticket.Ticket;
import com.aug.flightbooking.domain.models.ticket.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de CheckInTicketService.
 */
@ExtendWith(MockitoExtension.class)
class CheckInTicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private CheckInTicketService service;

    private Ticket emittedTicket;

    @BeforeEach
    void setUp() {
        // Se crea un tiquete emitido inicial para simular el check-in exitoso
        emittedTicket = Ticket.create(10L);
    }

    @Test
    @DisplayName("checkIn(): debe completar y guardar el ticket cuando está dentro de la ventana válida")
    void checkIn_successful() {
        // Se calcula una hora de salida 5 horas en el futuro. "now" cae dentro de la ventana [T-24h, T-2h]
        Instant departure = Instant.now().plus(Duration.ofHours(5));
        CreateCheckInCommand cmd = new CreateCheckInCommand(1L, departure.toEpochMilli());

        // Se stubbea el repositorio para encontrar y guardar el tiquete
        when(ticketRepository.findById(1L)).thenReturn(Mono.just(emittedTicket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        // Se ejecuta el caso de uso y se verifica que complete sin error
        StepVerifier.create(service.checkIn(cmd))
                .verifyComplete();

        // Se captura el argumento con el que se guardó el tiquete y se valida que cambió a CHECKED_IN
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(1)).save(captor.capture());
        Ticket saved = captor.getValue();

        // Se verifica que el estado haya sido actualizado a CHECKED_IN
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.CHECKED_IN);
        // Se verifica que se haya hecho un único guardado
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    @DisplayName("checkIn(): debe fallar cuando el ticket no existe")
    void checkIn_ticketNotFound() {
        // Se prepara un comando con cualquier fecha válida
        Instant departure = Instant.now().plus(Duration.ofHours(5));
        CreateCheckInCommand cmd = new CreateCheckInCommand(999L, departure.toEpochMilli());

        // Se simula que el repositorio no encuentra el tiquete
        when(ticketRepository.findById(999L)).thenReturn(Mono.empty());

        // Se verifica que el flujo emite IllegalArgumentException con el mensaje esperado
        StepVerifier.create(service.checkIn(cmd))
                .expectErrorMatches(ex -> ex instanceof IllegalArgumentException
                        && ex.getMessage().contains("Ticket no encontrado: 999"))
                .verify();

        // Se asegura que nunca se intenta guardar al no existir el tiquete
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkIn(): debe fallar y no guardar cuando se intenta fuera de la ventana permitida")
    void checkIn_outsideWindow() {
        // Se fija una salida 1 hora en el futuro, lo que hace que 'now' esté fuera del límite superior (T-2h)
        Instant departure = Instant.now().plus(Duration.ofHours(1));
        CreateCheckInCommand cmd = new CreateCheckInCommand(2L, departure.toEpochMilli());

        when(ticketRepository.findById(2L)).thenReturn(Mono.just(emittedTicket));

        // Se verifica que se propaga IllegalStateException desde las reglas de dominio
        StepVerifier.create(service.checkIn(cmd))
                .expectError(IllegalStateException.class)
                .verify();

        // Se valida que no se haya intentado persistir cambios al fallar la validación
        verify(ticketRepository, never()).save(any());
    }
}
