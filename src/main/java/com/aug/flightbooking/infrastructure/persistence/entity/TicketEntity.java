package com.aug.flightbooking.infrastructure.persistence.entity;

import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad persistente para tiquetes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("ticket")
public class TicketEntity {
    @Id
    private Long id;
    private Long reservationId;
    private Instant issuedAt;
    private String status;
}