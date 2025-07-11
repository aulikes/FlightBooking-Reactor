package com.aug.flightbooking.infrastructure.web.rest;

import com.aug.flightbooking.application.ports.in.CheckInTicketUseCase;
import com.aug.flightbooking.infrastructure.web.dto.CheckInRequest;
import com.aug.flightbooking.infrastructure.web.mapper.CheckInCreateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para ejecutar el check-in de un ticket.
 */
@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketCheckInController {

    private final CheckInTicketUseCase checkInTicketUseCase;
    private final CheckInCreateMapper mapper;

    /**
     * Ejecuta el check-in de un ticket.
     */
    @PostMapping("/checkin")
    public Mono<ResponseEntity<Object>> checkIn(@RequestBody CheckInRequest checkInRequest) {
        return checkInTicketUseCase.checkIn(mapper.toCommand(checkInRequest))
            .thenReturn(ResponseEntity.accepted().build())
            .onErrorResume(ex ->
                Mono.just(ResponseEntity.badRequest().build())
            );
    }
}