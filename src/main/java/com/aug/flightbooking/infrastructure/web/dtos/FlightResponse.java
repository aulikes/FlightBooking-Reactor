package com.aug.flightbooking.infrastructure.web.dtos;

import com.aug.flightbooking.domain.models.flight.FlightStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Representa la respuesta expuesta al cliente con datos de un vuelo.
 */
@Value
@Builder
public class FlightResponse {
    Long id;
    String airline;
    String origin;
    String destination;
    LocalDateTime departureDate;
    LocalDateTime arrivalDate;
    FlightStatus status;
}
