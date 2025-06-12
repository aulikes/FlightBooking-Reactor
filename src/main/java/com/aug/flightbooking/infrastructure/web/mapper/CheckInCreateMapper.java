package com.aug.flightbooking.infrastructure.web.mapper;

import com.aug.flightbooking.application.command.CreateCheckInCommand;
import com.aug.flightbooking.infrastructure.web.dto.CheckInRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CheckInCreateMapper {
    CreateCheckInCommand toCommand(CheckInRequest request);
}
