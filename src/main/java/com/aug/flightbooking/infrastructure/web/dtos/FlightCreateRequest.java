package com.aug.flightbooking.infrastructure.web.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class FlightCreateRequest {

    @NotBlank
    private String airlineName;

    @NotBlank
    private String airlineCode;

    @NotBlank
    private String flightCode;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    @Min(1)
    private int totalSeats;

    @Min(0)
    private int reservedSeats;

    /**
     * departureDate:
     *       type: string
     *       format: date-time
     *       description: Fecha y hora de salida en formato ISO 8601 UTC (ej: 2025-06-12T10:00:00Z)
     */
    @NotNull
    private String departureDate;

    /**
     * arrivalDate:
     *       type: string
     *       format: date-time
     *       description: Fecha y hora de llegada en formato ISO 8601 UTC (ej: 2025-06-12T13:00:00Z)
     */
    @NotNull
    private String arrivalDate;
}
