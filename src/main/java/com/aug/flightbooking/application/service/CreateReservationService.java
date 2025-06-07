package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.port.in.CreateReservationUseCase;
import com.aug.flightbooking.application.port.out.ReservationRepository;
import com.aug.flightbooking.application.command.CreateReservationCommand;
import com.aug.flightbooking.application.result.ReservationResult;
import com.aug.flightbooking.domain.model.reservation.PassengerInfo;
import com.aug.flightbooking.domain.model.reservation.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Caso de uso reactivo para crear una nueva reserva.
 */
@Service
@RequiredArgsConstructor
public class CreateReservationService implements CreateReservationUseCase {

    private final ReservationRepository reservationRepository;

    @Override
    public Mono<ReservationResult> createReservation(CreateReservationCommand command) {

        Reservation reservation = Reservation.create(
                command.flightId(),
                new PassengerInfo(command.fullName(), command.documentId()),
                Instant.now()
        );

        return reservationRepository.save(reservation).map(this::toResult);
    }

    /**
     * Transforma la entidad Reservation a un DTO ReservationResult
     */
    private ReservationResult toResult(Reservation reservation) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getFlightId(),
                reservation.getPassengerInfo().getFullName(),
                reservation.getPassengerInfo().getDocumentId(),
                reservation.getStatus().name(),
                reservation.getCreatedAt()
        );
    }
}
