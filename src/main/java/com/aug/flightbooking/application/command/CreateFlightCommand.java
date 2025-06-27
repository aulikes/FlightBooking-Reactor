package com.aug.flightbooking.application.command;

import java.time.Instant;

public record CreateFlightCommand(
        String airlineName,
        String airlineCode,
        String flightCode,
        String origin,
        String destination,
        int totalSeats,
        int reservedSeats,
        Instant departureDate,
        Instant arrivalDate
) {}
