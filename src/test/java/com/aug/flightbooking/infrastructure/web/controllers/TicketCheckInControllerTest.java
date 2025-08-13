package com.aug.flightbooking.infrastructure.web.controllers;

import com.aug.flightbooking.application.commands.CreateCheckInCommand;
import com.aug.flightbooking.application.ports.in.CheckInTicketUseCase;
import com.aug.flightbooking.infrastructure.web.dtos.CheckInRequest;
import com.aug.flightbooking.infrastructure.web.mappers.CheckInCreateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests de slice web para TicketCheckInController con WebFluxTest.
 * No usamos @MockBean: se inyectan mocks a través de @TestConfiguration.
 *
 * Controller bajo prueba:
 *   - POST /api/ticket/checkin
 *   - checkIn(@RequestBody CheckInRequest) -> 202 si ok, 400 si use case falla (onErrorResume)
 */
@WebFluxTest(controllers = TicketCheckInController.class)
@Import(TicketCheckInControllerTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TicketCheckInControllerTest {

    @TestConfiguration
    static class Config {
        @Bean CheckInTicketUseCase checkInTicketUseCase() { return Mockito.mock(CheckInTicketUseCase.class); }
        @Bean CheckInCreateMapper checkInCreateMapper() { return Mockito.mock(CheckInCreateMapper.class); }
    }

    @Autowired WebTestClient client;

    @Autowired CheckInTicketUseCase checkInTicketUseCase;
    @Autowired CheckInCreateMapper checkInCreateMapper;

    @Test
    @DisplayName("POST /api/ticket/checkin -> 202 Accepted cuando el check-in completa")
    void checkIn_ok_returns_202() {
        // Request válido (record con 2 campos @NotNull, pero el controller no usa @Valid)
        CheckInRequest req = new CheckInRequest(42L, 1_725_000_000_000L);

        // Mapper: request -> command
        CreateCheckInCommand cmd = new CreateCheckInCommand(42L, 1_725_000_000_000L);
        when(checkInCreateMapper.toCommand(any(CheckInRequest.class))).thenReturn(cmd);

        // Use case: completa sin error
        when(checkInTicketUseCase.checkIn(cmd)).thenReturn(Mono.empty());

        client.post().uri("/api/ticket/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isAccepted();

        verify(checkInCreateMapper, times(1)).toCommand(any(CheckInRequest.class));
        verify(checkInTicketUseCase, times(1)).checkIn(cmd);
        verifyNoMoreInteractions(checkInCreateMapper, checkInTicketUseCase);
    }

    @Test
    @DisplayName("POST /api/ticket/checkin -> 400 Bad Request cuando el use case falla")
    void checkIn_usecase_error_returns_400() {
        CheckInRequest req = new CheckInRequest(99L, 1_800_000_000_000L);

        CreateCheckInCommand cmd = new CreateCheckInCommand(99L, 1_800_000_000_000L);
        when(checkInCreateMapper.toCommand(any(CheckInRequest.class))).thenReturn(cmd);

        // Simula error del use case → el controller hace onErrorResume → 400
        when(checkInTicketUseCase.checkIn(cmd)).thenReturn(Mono.error(new IllegalArgumentException("boom")));

        client.post().uri("/api/ticket/checkin")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isBadRequest();

        verify(checkInCreateMapper, times(1)).toCommand(any(CheckInRequest.class));
        verify(checkInTicketUseCase, times(1)).checkIn(cmd);
        verifyNoMoreInteractions(checkInCreateMapper, checkInTicketUseCase);
    }
}
