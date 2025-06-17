package com.aug.flightbooking.infrastructure.web.rest;

import com.aug.flightbooking.infrastructure.web.dto.ReservationRequest;
import com.aug.flightbooking.infrastructure.web.dto.ReservationResponse;
import com.aug.flightbooking.infrastructure.web.mapper.ReservationCreateMapper;
import com.aug.flightbooking.application.port.in.CreateReservationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {

    private final CreateReservationUseCase createReservationUseCase;
    private final ReservationCreateMapper mapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<ReservationResponse>> createReservation(@Valid  @RequestBody ReservationRequest request) {
        return createReservationUseCase
                .createReservation(mapper.toCommand(request))
                .map(
                        r -> ResponseEntity.ok()
                                .body(mapper.toResponse(r))
                ).defaultIfEmpty(ResponseEntity.internalServerError().build());
    }
}
