package com.aug.flightbooking.infrastructure.persistence.repositories;

import com.aug.flightbooking.infrastructure.persistence.entities.FlightEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface R2dbcFlightRepository extends ReactiveCrudRepository<FlightEntity, Long> {

}
