package com.aug.flightbooking.infrastructure.init;

import com.aug.flightbooking.infrastructure.web.dtos.FlightCreateResponse;
import com.aug.flightbooking.infrastructure.web.dtos.ReservationRequest;
import com.aug.flightbooking.infrastructure.web.dtos.ReservationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Clase que inicializa 20 vuelos consumiendo el controlador HTTP una vez el sistema está listo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationDataInitializer {

    private final WebClient.Builder webClientBuilder;
    private final Environment environment;


    public Mono<List<ReservationResponse>> init(List<FlightCreateResponse> vuelosCreados) {
        if (vuelosCreados == null || vuelosCreados.isEmpty()) {
            return Mono.error(new IllegalStateException("No hay vuelos disponibles para reservar"));
        }

        String port = environment.getProperty("local.server.port");
        String baseUrl = "http://localhost:" + port;
        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        return Flux.range(1, 5)
            .flatMap(i -> {
                int randomIndex = ThreadLocalRandom.current().nextInt(vuelosCreados.size());
                FlightCreateResponse vuelo = vuelosCreados.get(randomIndex);

                ReservationRequest request = new ReservationRequest(
                        vuelo.id(),
                        "Pasajero " + i,
                        "DOC" + (100000 + ThreadLocalRandom.current().nextInt(900000))
                );

                return client.post()
                        .uri("/api/reservation")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(ReservationResponse.class)
                        .doOnSuccess(r -> log.info("Reserva #{} creada para vuelo {}", i, vuelo.flightCode()))
                        .doOnError(e -> log.error("Error creando reserva #{} para vuelo {}", i, vuelo.flightCode(), e));
            })
            .collectList()
            .doOnSuccess(list -> log.info("Se crearon {} reservas exitosamente", list.size()))
            .doOnError(e -> log.error("Error general al inicializar reservas", e));
    }
}
