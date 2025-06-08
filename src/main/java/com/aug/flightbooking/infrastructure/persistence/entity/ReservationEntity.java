package com.aug.flightbooking.infrastructure.persistence.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Table("reservations")
public class ReservationEntity {

    @Id
    private Long id;

    @Column("flight_id")
    private Long flightId;

    @Column("passenger_full_name")
    private String passengerFullName;

    @Column("passenger_document_id")
    private String passengerDocumentId;

    @Column("status")
    private String status;

    @Column("created_at")
    private Instant createdAt;
}
