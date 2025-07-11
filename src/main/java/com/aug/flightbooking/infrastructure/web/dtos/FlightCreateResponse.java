package com.aug.flightbooking.infrastructure.web.dtos;

import java.time.Instant;

public record FlightCreateResponse (
        Long id,
        String airlineName,
        String airlineCode,
        String flightCode,
        String origin,
        String destination,
        int totalSeats,
        Instant departureDate,
        Instant arrivalDate
)
{}
