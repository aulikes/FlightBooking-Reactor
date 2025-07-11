package com.aug.flightbooking.infrastructure.persistence.entities;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("flight")
public class FlightEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("airline_name")
    private String airlineName;

    @Column("airline_code")
    private String airlineCode;

    @Column("flight_code")
    private String flightCode;

    @Column("origin")
    private String origin;

    @Column("destination")
    private String destination;

    @Column("total_seats")
    private int totalSeats;

    @Column("reserved_seats")
    private int reservedSeats;

    @Column("scheduled_departure")
    private Instant scheduledDeparture;

    @Column("scheduled_arrival")
    private Instant scheduledArrival;

    @Column("status")
    private String status;
}
