package com.aug.flightbooking.infrastructure.persistence.mapper;

import com.aug.flightbooking.domain.models.reservation.Reservation;
import com.aug.flightbooking.domain.models.reservation.ReservationStatus;
import com.aug.flightbooking.domain.models.reservation.PassengerInfo;
import com.aug.flightbooking.infrastructure.persistence.entity.ReservationEntity;

public class ReservationMapper {

    // Convierte del dominio a la entidad de base de datos
    public static ReservationEntity toEntity(Reservation reservation) {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(reservation.getId());
        entity.setFlightId(reservation.getFlightId());
        entity.setPassengerFullName(reservation.getPassengerInfo().getFullName());
        entity.setPassengerDocumentId(reservation.getPassengerInfo().getDocumentId());
        entity.setStatus(reservation.getStatus().name());
        entity.setCreatedAt(reservation.getCreatedAt());
        return entity;
    }

    // Convierte desde la entidad persistida al modelo de dominio
    public static Reservation toDomain(ReservationEntity entity) {
        return Reservation.fromPersistence(
                entity.getId(),
                entity.getFlightId(),
                new PassengerInfo(entity.getPassengerFullName(), entity.getPassengerDocumentId()),
                ReservationStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt()
        );
    }
}
