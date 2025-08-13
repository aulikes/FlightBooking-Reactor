package com.aug.flightbooking.infrastructure.web.controllers;

import com.aug.flightbooking.application.commands.CreateFlightCommand;
import com.aug.flightbooking.application.ports.in.CreateFlightUseCase;
import com.aug.flightbooking.application.ports.in.GetAllFlightsUseCase;
import com.aug.flightbooking.domain.models.flight.Airline;
import com.aug.flightbooking.domain.models.flight.Flight;
import com.aug.flightbooking.domain.models.flight.FlightStatus;
import com.aug.flightbooking.infrastructure.web.dtos.FlightCreateRequest;
import com.aug.flightbooking.infrastructure.web.dtos.FlightCreateResponse;
import com.aug.flightbooking.infrastructure.web.dtos.FlightResponse;
import com.aug.flightbooking.infrastructure.web.mappers.FlightCreateMapper;
import com.aug.flightbooking.infrastructure.web.mappers.FlightResponseMapper;
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
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

/**
 * Slice test de la capa web con @WebFluxTest,
 * SIN usar @MockBean. En su lugar, definimos @Bean mocks
 * dentro de una @TestConfiguration importada.
 */
@WebFluxTest(controllers = FlightController.class)
@Import(FlightControllerTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FlightControllerTest {

    @TestConfiguration
    static class Config {
        @Bean CreateFlightUseCase createFlightUseCase() { return Mockito.mock(CreateFlightUseCase.class); }
        @Bean GetAllFlightsUseCase getAllFlightsUseCase() { return Mockito.mock(GetAllFlightsUseCase.class); }
        @Bean FlightCreateMapper flightCreateMapper() { return Mockito.mock(FlightCreateMapper.class); }
        @Bean FlightResponseMapper flightResponseMapper() { return Mockito.mock(FlightResponseMapper.class); }
    }

    @Autowired WebTestClient client;

    @Autowired CreateFlightUseCase createFlightUseCase;
    @Autowired GetAllFlightsUseCase getAllFlightsUseCase;
    @Autowired FlightCreateMapper flightCreateMapper;
    @Autowired FlightResponseMapper flightResponseMapper;

    @Test
    @DisplayName("POST /api/flight -> 201 Created con body mapeado")
    void createFlight_created() {
        FlightCreateRequest req = new FlightCreateRequest();
        req.setAirlineName("Avianca");
        req.setAirlineCode("AV");
        req.setFlightCode("AV123");
        req.setOrigin("BOG");
        req.setDestination("MDE");
        req.setTotalSeats(180);
        req.setDepartureDate("2025-08-12T14:00:00Z");
        req.setArrivalDate("2025-08-12T16:30:00Z");

        CreateFlightCommand cmd = new CreateFlightCommand(
                "Avianca","AV","AV123","BOG","MDE",180, 0,
                Instant.parse("2025-08-12T14:00:00Z"),
                Instant.parse("2025-08-12T16:30:00Z")
        );
        when(flightCreateMapper.toCommand(req)).thenReturn(cmd);

        Flight created = Flight.fromPersistence(
                10L, new Airline("Avianca","AV"), "AV123", "BOG","MDE",
                180, 0,
                Instant.parse("2025-08-12T14:00:00Z"),
                Instant.parse("2025-08-12T16:30:00Z"),
                FlightStatus.SCHEDULED
        );
        when(createFlightUseCase.create(cmd)).thenReturn(Mono.just(created));

        FlightCreateResponse resp = new FlightCreateResponse(
                10L,"Avianca","AV","AV123","BOG","MDE",180,
                Instant.parse("2025-08-12T14:00:00Z"),
                Instant.parse("2025-08-12T16:30:00Z")
        );
        when(flightCreateMapper.toResponse(created)).thenReturn(resp);

        client.post().uri("/api/flight")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()          // ← ahora 201
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(10)
                .jsonPath("$.airlineName").isEqualTo("Avianca")
                .jsonPath("$.flightCode").isEqualTo("AV123");

        verify(createFlightUseCase, times(1)).create(cmd);
    }

    @Test
    @DisplayName("POST /api/flight -> 500 cuando use case retorna empty")
    void createFlight_empty_internalServerError() {
        FlightCreateRequest req = new FlightCreateRequest();
        req.setAirlineName("X");
        req.setAirlineCode("X");
        req.setFlightCode("X1");
        req.setOrigin("AAA");
        req.setDestination("BBB");
        req.setTotalSeats(1);
        req.setDepartureDate("2025-08-12T14:00:00Z");
        req.setArrivalDate("2025-08-12T16:30:00Z");

        CreateFlightCommand cmd = new CreateFlightCommand(
                "X","X","X1","AAA","BBB",1, 0,
                Instant.parse("2025-08-12T14:00:00Z"),
                Instant.parse("2025-08-12T16:30:00Z")
        );
        when(flightCreateMapper.toCommand(req)).thenReturn(cmd);
        when(createFlightUseCase.create(cmd)).thenReturn(Mono.empty());

        client.post().uri("/api/flight")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("GET /api/flight -> 200 con lista de FlightResponse")
    void getAllFlights_ok() {
        Flight f1 = Flight.fromPersistence(1L, new Airline("LATAM","LA"),
                "LA001","SCL","LIM",200,10, Instant.now(),
                Instant.now().plusSeconds(7200), FlightStatus.SCHEDULED);
        Flight f2 = Flight.fromPersistence(2L, new Airline("Avianca","AV"),
                "AV002","BOG","MDE",180,20, Instant.now(),
                Instant.now().plusSeconds(3600), FlightStatus.BOARDING);

        when(getAllFlightsUseCase.getAllFlights()).thenReturn(Flux.just(f1, f2));

        FlightResponse r1 = FlightResponse.builder()
                .id(1L).airline("LATAM").origin("SCL").destination("LIM")
                .departureDate(LocalDateTime.now()).arrivalDate(LocalDateTime.now().plusHours(2))
                .status(FlightStatus.SCHEDULED).build();
        FlightResponse r2 = FlightResponse.builder()
                .id(2L).airline("Avianca").origin("BOG").destination("MDE")
                .departureDate(LocalDateTime.now()).arrivalDate(LocalDateTime.now().plusHours(1))
                .status(FlightStatus.BOARDING).build();

        when(flightResponseMapper.toResponse(f1)).thenReturn(r1);
        when(flightResponseMapper.toResponse(f2)).thenReturn(r2);

        client.get().uri("/api/flight")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[1].id").isEqualTo(2);
    }

    @Test
    @DisplayName("GET /api/flight -> 200 lista vacía cuando no hay datos")
    void getAllFlights_empty() {
        when(getAllFlightsUseCase.getAllFlights()).thenReturn(Flux.empty());

        client.get().uri("/api/flight")
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");
    }

    // ===== Validación 400 (Bean Validation en @WebFluxTest) =====

    @Test
    @DisplayName("POST /api/flight -> 400 cuando totalSeats <= 0")
    void createFlight_bad_totalSeats() {
        String badJson = """
          {
            "airlineName":"Avianca",
            "airlineCode":"AV",
            "flightCode":"AV123",
            "origin":"BOG",
            "destination":"MDE",
            "totalSeats":0,
            "departureDate":"2025-08-12T14:00:00Z",
            "arrivalDate":"2025-08-12T16:30:00Z"
          }
        """;

        client.post().uri("/api/flight")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(badJson)
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(createFlightUseCase, flightCreateMapper);
    }

    @Test
    @DisplayName("POST /api/flight -> 400 cuando faltan campos obligatorios (airlineName/flightCode)")
    void createFlight_missing_fields() {
        String badJson = """
          {
            "airlineCode":"AV",
            "origin":"BOG",
            "destination":"MDE",
            "totalSeats":180,
            "departureDate":"2025-08-12T14:00:00Z",
            "arrivalDate":"2025-08-12T16:30:00Z"
          }
        """;

        client.post().uri("/api/flight")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(badJson)
                .exchange()
                .expectStatus().isBadRequest();

        verifyNoInteractions(createFlightUseCase, flightCreateMapper);
    }
}
