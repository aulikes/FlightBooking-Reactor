package com.aug.flightbooking.infrastructure.web.controllers;

import com.aug.flightbooking.application.commands.CreateReservationCommand;
import com.aug.flightbooking.application.ports.in.CreateReservationUseCase;
import com.aug.flightbooking.application.ports.in.GetAllReservationsUseCase;
import com.aug.flightbooking.application.results.ReservationResult;
import com.aug.flightbooking.domain.models.reservation.PassengerInfo;
import com.aug.flightbooking.domain.models.reservation.Reservation;
import com.aug.flightbooking.domain.models.reservation.ReservationStatus;
import com.aug.flightbooking.infrastructure.web.dtos.ReservationRequest;
import com.aug.flightbooking.infrastructure.web.dtos.ReservationResponse;
import com.aug.flightbooking.infrastructure.web.mappers.ReservationCreateMapper;
import com.aug.flightbooking.infrastructure.web.mappers.ReservationResponseMapper;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.mockito.Mockito.*;

@WebFluxTest(controllers = ReservationController.class)
@Import(ReservationControllerTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationControllerTest {

    @TestConfiguration
    static class Config {
        @Bean CreateReservationUseCase createReservationUseCase() { return Mockito.mock(CreateReservationUseCase.class); }
        @Bean GetAllReservationsUseCase getAllReservationsUseCase() { return Mockito.mock(GetAllReservationsUseCase.class); }
        @Bean ReservationCreateMapper reservationCreateMapper() { return Mockito.mock(ReservationCreateMapper.class); }
        @Bean ReservationResponseMapper reservationResponseMapper() { return Mockito.mock(ReservationResponseMapper.class); }
    }

    @Autowired WebTestClient client;

    @Autowired CreateReservationUseCase createReservationUseCase;
    @Autowired GetAllReservationsUseCase getAllReservationsUseCase;
    @Autowired ReservationCreateMapper reservationCreateMapper;
    @Autowired ReservationResponseMapper reservationResponseMapper;

    @Test
    @DisplayName("POST /api/reservation -> 201 OK con body mapeado")
    void createReservation_ok() {
        ReservationRequest req = new ReservationRequest(100L, "John Doe", "CC-1");
        CreateReservationCommand cmd = new CreateReservationCommand(100L, "John Doe", "CC-1");
        when(reservationCreateMapper.toCommand(req)).thenReturn(cmd);

        ReservationResult result = new ReservationResult(
                50L, 100L, "John Doe", "CC-1",
                "CREATED", Instant.parse("2025-08-12T12:00:00Z")
        );
        when(createReservationUseCase.createReservation(cmd)).thenReturn(Mono.just(result));

        ReservationResponse resp = new ReservationResponse(
                50L, 100L, "John Doe", "CC-1",
                "CREATED", Instant.parse("2025-08-12T12:00:00Z")
        );
        when(reservationCreateMapper.toResponse(result)).thenReturn(resp);

        client.post().uri("/api/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(50)
                .jsonPath("$.flightId").isEqualTo(100)
                .jsonPath("$.status").isEqualTo("CREATED");

        verify(createReservationUseCase, times(1)).createReservation(cmd);
    }

    @Test
    @DisplayName("POST /api/reservation -> 500 cuando use case retorna empty")
    void createReservation_empty_internalServerError() {
        ReservationRequest req = new ReservationRequest(100L, "X", "Y");
        CreateReservationCommand cmd = new CreateReservationCommand(100L, "X", "Y");
        when(reservationCreateMapper.toCommand(req)).thenReturn(cmd);
        when(createReservationUseCase.createReservation(cmd)).thenReturn(Mono.empty());

        client.post().uri("/api/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("GET /api/reservation -> 200 lista")
    void getAllReservations_ok() {
        Reservation r1 = Reservation.fromPersistence(
                1L, 100L, new PassengerInfo("A","1"),
                ReservationStatus.CREATED, Instant.parse("2025-08-12T10:00:00Z"), "");
        Reservation r2 = Reservation.fromPersistence(
                2L, 200L, new PassengerInfo("B","2"),
                ReservationStatus.CONFIRMED, Instant.parse("2025-08-12T11:00:00Z"), "");

        when(getAllReservationsUseCase.getAllReservations()).thenReturn(Flux.just(r1, r2));

        ReservationResponse rr1 = new ReservationResponse(1L,100L,"A","1",
                "CREATED", Instant.parse("2025-08-12T10:00:00Z"));
        ReservationResponse rr2 = new ReservationResponse(2L,200L,"B","2",
                "CONFIRMED", Instant.parse("2025-08-12T11:00:00Z"));
        when(reservationResponseMapper.toResponse(r1)).thenReturn(rr1);
        when(reservationResponseMapper.toResponse(r2)).thenReturn(rr2);

        client.get().uri("/api/reservation")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].status").isEqualTo("CREATED")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].status").isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("GET /api/reservation -> 200 []")
    void getAllReservations_empty() {
        when(getAllReservationsUseCase.getAllReservations()).thenReturn(Flux.empty());

        client.get().uri("/api/reservation")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");
    }

    // ===== Validación 400 =====

    @Test
    @DisplayName("POST /api/reservation -> 400 cuando flightId es null")
    void createReservation_null_flightId() {
        String badJson = """
          {
            "fullName":"John Doe",
            "documentId":"CC-1"
          }
        """;

        client.post().uri("/api/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(badJson)
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(createReservationUseCase, reservationCreateMapper);
    }

    @Test
    @DisplayName("POST /api/reservation -> 400 cuando fullName/documentId inválidos")
    void createReservation_bad_names() {
        String badJson = """
          {
            "flightId": 100,
            "fullName": "",
            "documentId": " "
          }
        """;

        client.post().uri("/api/reservation")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(badJson)
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(createReservationUseCase, reservationCreateMapper);
    }
}
