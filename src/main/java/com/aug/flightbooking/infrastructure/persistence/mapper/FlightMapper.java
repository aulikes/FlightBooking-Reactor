package com.aug.flightbooking.infrastructure.persistence.mapper;

import com.aug.flightbooking.domain.models.flight.Airline;
import com.aug.flightbooking.domain.models.flight.Flight;
import com.aug.flightbooking.domain.models.flight.FlightStatus;
import com.aug.flightbooking.infrastructure.persistence.entity.FlightEntity;

public class FlightMapper {

    // Convierte de entidad de base de datos a entidad de dominio
    public static Flight toDomain(FlightEntity entity) {
        return Flight.fromPersistence(
                entity.getId(),
                new Airline(entity.getAirlineName(), entity.getAirlineCode()),
                entity.getFlightCode(),
                entity.getOrigin(),
                entity.getDestination(),
                entity.getTotalSeats(),
                entity.getReservedSeats(),
                entity.getScheduledDeparture(),
                entity.getScheduledArrival(),
                FlightStatus.valueOf(entity.getStatus())
        );
    }

    // Convierte de entidad de dominio a entidad de base de datos
    public static FlightEntity toEntity(Flight domain) {
        FlightEntity entity = new FlightEntity();
        entity.setId(domain.getId());
        entity.setAirlineName(domain.getAirline().getName());
        entity.setAirlineCode(domain.getAirline().getCode());
        entity.setFlightCode(domain.getFlightCode());
        entity.setOrigin(domain.getOrigin());
        entity.setDestination(domain.getDestination());
        entity.setTotalSeats(domain.getTotalSeats());
        entity.setDestination(domain.getDestination());
        entity.setReservedSeats(domain.getReservedSeats());
        entity.setScheduledDeparture(domain.getScheduledDeparture());
        entity.setScheduledArrival(domain.getScheduledArrival());
        entity.setStatus(domain.getStatus().name());
        return entity;
    }
}
