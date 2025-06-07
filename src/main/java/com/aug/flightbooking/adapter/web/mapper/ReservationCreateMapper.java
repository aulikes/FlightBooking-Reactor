package com.aug.flightbooking.adapter.web.mapper;

import com.aug.flightbooking.adapter.web.dto.ReservationRequest;
import com.aug.flightbooking.adapter.web.dto.ReservationResponse;
import com.aug.flightbooking.application.command.CreateReservationCommand;
import com.aug.flightbooking.application.result.ReservationResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationCreateMapper {
    CreateReservationCommand toCommand(ReservationRequest request);
    ReservationResponse toResponse(ReservationResult result);
}
