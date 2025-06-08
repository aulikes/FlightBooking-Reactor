package com.aug.flightbooking.infrastructure.persistence.adapter;

import com.aug.flightbooking.domain.model.flight.Flight;
import com.aug.flightbooking.application.port.out.FlightRepository;
import com.aug.flightbooking.infrastructure.persistence.entity.FlightEntity;
import com.aug.flightbooking.infrastructure.persistence.mapper.FlightMapper;
import com.aug.flightbooking.infrastructure.persistence.repository.R2dbcFlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FlightRepositoryAdapter implements FlightRepository {

    private final R2dbcFlightRepository r2dbcFlightRepository;

    @Override
    public Mono<Flight> findById(Long flightId) {
        return r2dbcFlightRepository.findById(flightId).map(FlightMapper::toDomain);
    }

    @Override
    public Mono<Flight> save(Flight flight) {
        FlightEntity entity = FlightMapper.toEntity(flight);
        return r2dbcFlightRepository.save(entity).map(FlightMapper::toDomain);
    }
}
