package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.event.ReservationCreatedEvent;
import com.aug.flightbooking.application.port.in.CreateReservationUseCase;
import com.aug.flightbooking.application.port.out.ReservationEventPublisher;
import com.aug.flightbooking.application.port.out.ReservationRepository;
import com.aug.flightbooking.application.command.CreateReservationCommand;
import com.aug.flightbooking.application.port.out.ReservationCache;
import com.aug.flightbooking.application.result.ReservationResult;
import com.aug.flightbooking.domain.model.reservation.PassengerInfo;
import com.aug.flightbooking.domain.model.reservation.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Caso de uso reactivo para crear una nueva reserva.
 */
@Service
@RequiredArgsConstructor
public class CreateReservationService implements CreateReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationEventPublisher eventPublisher;
    private final ReservationCache timeoutTracker;

    @Override
    public Mono<ReservationResult> createReservation(CreateReservationCommand command) {

        Reservation reservation = Reservation.create(
                command.flightId(),
                new PassengerInfo(command.fullName(), command.documentId())
        );

        return reservationRepository.save(reservation)
                .flatMap(saved -> {
                    // 1. Publicar evento ReservationCreated
                    ReservationCreatedEvent event = new ReservationCreatedEvent(
                            saved.getId(),
                            saved.getFlightId(),
                            saved.getPassengerInfo().getFullName(),
                            saved.getPassengerInfo().getDocumentId()
                    );
                    // 2. De forma asíncrona: publica el evento y envía registro de timeout a Redis
                    Mono<Void> publish = eventPublisher.publishCreated(event);
                    Mono<Void> track = timeoutTracker.registerTimeout(saved.getId());
                    return Mono.when(
                            publish,
                            track
                    ).thenReturn(saved);
                })
                .map(saved -> new ReservationResult(
                        saved.getId(),
                        saved.getFlightId(),
                        saved.getPassengerInfo().getFullName(),
                        saved.getPassengerInfo().getDocumentId(),
                        saved.getStatus().name(),
                        saved.getCreatedAt()
                ));
    }
}
