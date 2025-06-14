package com.aug.flightbooking.infrastructure.init;

import com.aug.flightbooking.infrastructure.web.dto.FlightCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que inicializa 20 vuelos consumiendo el controlador HTTP una vez el sistema está listo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlightDataInitializer {

    private final WebClient.Builder webClientBuilder;
    private final Environment environment;

    public Mono<Void> init() {
        log.info("Inicializando vuelos de prueba mediante el endpoint HTTP...");

        // Obtener host y puerto real donde está corriendo el sistema
        String port = environment.getProperty("local.server.port", "8080");
        String baseUrl = "http://localhost:" + port;

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        // Crear 20 vuelos con datos dinámicos
        List<FlightCreateRequest> requests = new ArrayList<>();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);

        for (int i = 1; i <= 20; i++) {
            FlightCreateRequest flight = new FlightCreateRequest();
            flight.setAirlineName("Airline " + i);
            flight.setAirlineCode("AL" + String.format("%02d", i));
            flight.setFlightCode("FL" + String.format("%03d", i));
            flight.setOrigin("BOG");
            flight.setDestination("MDE");
            flight.setTotalSeats(100 + i);
            flight.setDepartureDate(now.plus(i * 10, ChronoUnit.MINUTES).toString());
            flight.setArrivalDate(now.plus(i * 10 + 180, ChronoUnit.MINUTES).toString());

            requests.add(flight);
        }

        // Ejecutar llamadas HTTP en paralelo
        return Flux.fromIterable(requests)
                .flatMap(req -> client.post()
                        .uri("/api/flight")
                        .bodyValue(req)
                        .retrieve()
                        .toBodilessEntity()
                        .doOnSuccess(r -> log.info("Vuelo creado: {}", req.getFlightCode()))
                        .doOnError(e -> log.error("Error creando vuelo {}", req.getFlightCode(), e))
                )
                .then()
                .doOnSuccess(v -> log.info("Todos los vuelos inicializados correctamente"))
                .doOnError(e -> log.error("Error general al inicializar vuelos", e));
    }
}
