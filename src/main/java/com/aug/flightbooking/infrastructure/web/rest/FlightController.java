package com.aug.flightbooking.infrastructure.web.rest;

import com.aug.flightbooking.application.command.CreateFlightCommand;
import com.aug.flightbooking.application.port.in.CreateFlightUseCase;
import com.aug.flightbooking.infrastructure.web.dto.FlightCreateRequest;
import com.aug.flightbooking.infrastructure.web.dto.FlightCreateResponse;
import com.aug.flightbooking.infrastructure.web.mapper.FlightCreateMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Exposición REST para la gestión de vuelos.
 */
@RestController
@RequestMapping("/api/flight")
@RequiredArgsConstructor
public class FlightController {

    private final CreateFlightUseCase createFlightUseCase;
    private final FlightCreateMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<FlightCreateResponse>> createFlight(@Valid @RequestBody FlightCreateRequest request) {
        CreateFlightCommand command = mapper.toCommand(request);
        return createFlightUseCase
                .create(mapper.toCommand(request))
                .map(
                    r -> ResponseEntity.ok()
                            .body(mapper.toResponse(r))
                ).defaultIfEmpty(ResponseEntity.internalServerError().build());
    }
}
