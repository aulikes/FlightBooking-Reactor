package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.ReservationEmittedEvent;
import com.aug.flightbooking.application.events.TicketCreatedEvent;
import com.aug.flightbooking.application.ports.out.TicketCreatedEventPublisher;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ReservationEmittedEventHandlerService.
 * Se valida el retorno de Ticket en éxito y comportamiento ante errores (flujo vacío).
 */
@ExtendWith(MockitoExtension.class)
class ReservationEmittedEventHandlerServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketCreatedEventPublisher publisher;

    @InjectMocks
    private ReservationEmittedEventHandlerService service;

    @BeforeEach
    void defaultStubs() {
        // Se asegura que findByReservationId nunca retorne null
        when(ticketRepository.findByReservationId(anyLong())).thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("handle(): crea y retorna Ticket EMITTED, persiste y publica TicketCreatedEvent")
    void handle_create_save_publish_and_return_ticket() {
        ReservationEmittedEvent event = new ReservationEmittedEvent(99L);

        // Se simula persistencia que asigna ID
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            return Mono.just(Ticket.fromPersistence(1L, t.getReservationId(), t.getStatus(), t.getIssuedAt()));
        });
        // Publicación exitosa
        when(publisher.publish(any(TicketCreatedEvent.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.handle(event))
                .assertNext(ticket -> {
                    // Se verifica que el ticket retornado sea el persistido (EMITTED y con reservationId esperado)
                    assertThat(ticket.getReservationId()).isEqualTo(99L);
                    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.EMITTED);
                    assertThat(ticket.getId()).isEqualTo(1L);
                })
                .verifyComplete();

        // Se valida que se guardó el ticket construido
        ArgumentCaptor<Ticket> toSave = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(1)).save(toSave.capture());
        assertThat(toSave.getValue().getReservationId()).isEqualTo(99L);
        assertThat(toSave.getValue().getStatus()).isEqualTo(TicketStatus.EMITTED);

        // Se valida publicación del evento con el reservationId correcto
        ArgumentCaptor<TicketCreatedEvent> evt = ArgumentCaptor.forClass(TicketCreatedEvent.class);
        verify(publisher, times(1)).publish(evt.capture());
        assertThat(evt.getValue().reservationId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("handle(): si ya existe Ticket, lo retorna y NO persiste ni publica")
    void handle_existing_ticket_returns_without_saving_or_publishing() {
        ReservationEmittedEvent event = new ReservationEmittedEvent(77L);

        // Ticket ya existente
        Ticket existing = Ticket.fromPersistence(10L, 77L, TicketStatus.EMITTED, java.time.Instant.now());
        when(ticketRepository.findByReservationId(77L)).thenReturn(Mono.just(existing));

        StepVerifier.create(service.handle(event))
                .expectNext(existing) // Se espera el mismo ticket
                .verifyComplete();

        // No debe guardar ni publicar
        verify(ticketRepository, never()).save(any());
        verify(publisher, never()).publish(any());
    }

    @Test
    @DisplayName("handle(): si falla publish, el flujo se captura y retorna vacío (sin Ticket)")
    void handle_publish_error_returns_empty() {
        ReservationEmittedEvent event = new ReservationEmittedEvent(55L);

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            return Mono.just(Ticket.fromPersistence(2L, t.getReservationId(), t.getStatus(), t.getIssuedAt()));
        });
        when(publisher.publish(any(TicketCreatedEvent.class)))
                .thenReturn(Mono.error(new RuntimeException("Kafka caído")));

        StepVerifier.create(service.handle(event))
                .verifyComplete(); // No emite Ticket por onErrorResume -> Mono.empty()

        verify(publisher, times(1)).publish(any(TicketCreatedEvent.class));
    }

    @Test
    @DisplayName("handle(): si falla save, el flujo se captura y retorna vacío")
    void handle_save_error_returns_empty() {
        ReservationEmittedEvent event = new ReservationEmittedEvent(66L);

        when(ticketRepository.save(any(Ticket.class)))
                .thenReturn(Mono.error(new IllegalStateException("DB error")));

        StepVerifier.create(service.handle(event))
                .verifyComplete();

        verify(publisher, never()).publish(any());
    }

    @Test
    @DisplayName("handle(): si falla findByReservationId, el flujo se captura y retorna vacío")
    void handle_find_error_returns_empty() {
        ReservationEmittedEvent event = new ReservationEmittedEvent(44L);

        when(ticketRepository.findByReservationId(44L))
                .thenReturn(Mono.error(new RuntimeException("Repo error")));

        StepVerifier.create(service.handle(event))
                .verifyComplete();

        verify(ticketRepository, never()).save(any());
        verify(publisher, never()).publish(any());
    }
}
