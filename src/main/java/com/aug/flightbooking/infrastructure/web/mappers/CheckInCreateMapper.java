package com.aug.flightbooking.infrastructure.web.mappers;

import com.aug.flightbooking.application.commands.CreateCheckInCommand;
import com.aug.flightbooking.infrastructure.web.dtos.CheckInRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CheckInCreateMapper {
    CreateCheckInCommand toCommand(CheckInRequest request);
}
