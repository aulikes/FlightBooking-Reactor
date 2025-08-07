package com.aug.flightbooking.infrastructure.web.mappers;

import com.aug.flightbooking.domain.models.reservation.Reservation;
import com.aug.flightbooking.infrastructure.web.dtos.ReservationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationResponseMapper {
    ReservationResponse toResponse(Reservation reservation);
}
