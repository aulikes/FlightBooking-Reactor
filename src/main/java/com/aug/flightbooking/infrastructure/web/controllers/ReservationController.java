package com.aug.flightbooking.infrastructure.web.controllers;

import com.aug.flightbooking.application.ports.in.GetAllReservationsUseCase;
import com.aug.flightbooking.infrastructure.web.dtos.ReservationRequest;
import com.aug.flightbooking.infrastructure.web.dtos.ReservationResponse;
import com.aug.flightbooking.infrastructure.web.mappers.ReservationCreateMapper;
import com.aug.flightbooking.application.ports.in.CreateReservationUseCase;
import com.aug.flightbooking.infrastructure.web.mappers.ReservationResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {

    private final CreateReservationUseCase createReservationUseCase;
    private final ReservationCreateMapper mapper;
    private final GetAllReservationsUseCase getAllReservationsUseCase;
    private final ReservationResponseMapper reservationResponseMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<ReservationResponse>> createReservation(@Valid  @RequestBody ReservationRequest request) {
        return createReservationUseCase
                .createReservation(mapper.toCommand(request))
                .map(r -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(mapper.toResponse(r))
                ).defaultIfEmpty(ResponseEntity.internalServerError().build());
    }

    @GetMapping
    public Flux<ReservationResponse> getAllReservations() {
        return getAllReservationsUseCase.getAllReservations()
                .map(reservationResponseMapper::toResponse);
    }
}
