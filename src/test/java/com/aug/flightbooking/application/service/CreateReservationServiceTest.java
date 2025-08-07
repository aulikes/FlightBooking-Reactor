package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.commands.CreateReservationCommand;
import com.aug.flightbooking.application.events.ReservationCreatedEvent;
import com.aug.flightbooking.application.ports.out.ReservationCache;
import com.aug.flightbooking.application.ports.out.ReservationCreatedEventPublisher;
import com.aug.flightbooking.application.ports.out.ReservationRepository;
import com.aug.flightbooking.domain.models.reservation.PassengerInfo;
import com.aug.flightbooking.domain.models.reservation.Reservation;
import com.aug.flightbooking.domain.models.reservation.ReservationStatus;
import com.aug.flightbooking.domain.models.reservation.ReservationStatusAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CreateReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationCreatedEventPublisher eventPublisher;

    @Mock
    private ReservationCache reservationCache;

    @Mock
    private ReservationStatusUpdater reservationStatusUpdater;

    @InjectMocks
    private CreateReservationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateReservationSuccessfully() {
        // Comando de entrada con datos válidos
        CreateReservationCommand command = new CreateReservationCommand(
                101L,
                "John Doe",
                "123456789"
        );

        // Se simula una reserva ya persistida con todos sus valores
        Reservation reservation = Reservation.fromPersistence(
                1L,
                command.flightId(),
                new PassengerInfo(command.fullName(), command.documentId()),
                ReservationStatus.CREATED,
                Instant.now(),
                null
        );

        // Mock de la persistencia de la reserva
        when(reservationRepository.save(any(Reservation.class))).thenReturn(Mono.just(reservation));

        // Mock de publicación del evento
        when(eventPublisher.publish(any(ReservationCreatedEvent.class))).thenReturn(Mono.empty());

        // Mock del registro en Redis
        when(reservationCache.registerTimeout(1L)).thenReturn(Mono.empty());

        // Mock de la actualización del estado
        when(reservationStatusUpdater.updateStatus(reservation, ReservationStatusAction.PENDING))
                .thenReturn(Mono.empty());

        // Se verifica que el resultado contenga el ID y el estado esperado
        StepVerifier.create(service.createReservation(command))
                .expectNextMatches(result ->
                        result.id().equals(1L) &&
                                result.status().equals(ReservationStatus.CREATED.name())
                )
                .verifyComplete();
    }
}
