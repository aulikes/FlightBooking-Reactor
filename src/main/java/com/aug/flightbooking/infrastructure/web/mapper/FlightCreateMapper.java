package com.aug.flightbooking.infrastructure.web.mapper;

import com.aug.flightbooking.application.command.CreateFlightCommand;
import com.aug.flightbooking.domain.model.flight.Flight;
import com.aug.flightbooking.infrastructure.web.dto.FlightCreateRequest;
import com.aug.flightbooking.infrastructure.web.dto.FlightCreateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface FlightCreateMapper {

    @Mapping(target = "departureDate", expression = "java(toInstant(request.getDepartureDate()))")
    @Mapping(target = "arrivalDate", expression = "java(toInstant(request.getArrivalDate()))")
    CreateFlightCommand toCommand(FlightCreateRequest request);

    FlightCreateResponse toResponse(Flight request);

    // Método auxiliar para el parseo
    default Instant toInstant(String value) {
        return Instant.parse(value);
    }
}
