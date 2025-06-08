package com.aug.flightbooking.infrastructure.persistence.repository;

import com.aug.flightbooking.infrastructure.persistence.entity.FlightEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface R2dbcFlightRepository extends ReactiveCrudRepository<FlightEntity, Long> {

}
