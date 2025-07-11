package com.aug.flightbooking.infrastructure.web.mappers;

import com.aug.flightbooking.infrastructure.web.dtos.ReservationRequest;
import com.aug.flightbooking.infrastructure.web.dtos.ReservationResponse;
import com.aug.flightbooking.application.commands.CreateReservationCommand;
import com.aug.flightbooking.application.results.ReservationResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationCreateMapper {
    CreateReservationCommand toCommand(ReservationRequest request);
    ReservationResponse toResponse(ReservationResult result);
}
