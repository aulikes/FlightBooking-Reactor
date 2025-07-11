package com.aug.flightbooking.infrastructure.persistence.adapters;

import com.aug.flightbooking.domain.models.flight.Flight;
import com.aug.flightbooking.application.ports.out.FlightRepository;
import com.aug.flightbooking.infrastructure.persistence.entities.FlightEntity;
import com.aug.flightbooking.infrastructure.persistence.mappers.FlightMapper;
import com.aug.flightbooking.infrastructure.persistence.repositories.R2dbcFlightRepository;
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
