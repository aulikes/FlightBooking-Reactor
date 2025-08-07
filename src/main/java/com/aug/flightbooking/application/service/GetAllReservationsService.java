package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.ports.in.GetAllReservationsUseCase;
import com.aug.flightbooking.application.ports.out.ReservationRepository;
import com.aug.flightbooking.domain.models.reservation.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class GetAllReservationsService implements GetAllReservationsUseCase {

    private final ReservationRepository reservationRepository;

    @Override
    public Flux<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }
}
