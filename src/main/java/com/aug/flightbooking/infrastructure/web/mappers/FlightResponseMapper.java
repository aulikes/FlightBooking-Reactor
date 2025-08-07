package com.aug.flightbooking.infrastructure.web.mappers;

import com.aug.flightbooking.domain.models.flight.Flight;
import com.aug.flightbooking.infrastructure.web.dtos.FlightResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct que transforma un Flight del dominio en un DTO FlightResponse.
 */
@Mapper(componentModel = "spring")
public interface FlightResponseMapper {

    @Mapping(source = "airline.name", target = "airline")
    FlightResponse toResponse(Flight flight);
}
