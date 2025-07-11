package com.aug.flightbooking.application.ports.out;

import com.aug.flightbooking.domain.models.ticket.Ticket;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para persistencia de tiquetes.
 */
public interface TicketRepository {

  /**
   * Persiste un nuevo tiquete emitido.
   * @param ticket el objeto de dominio
   * @return el tiquete guardado con su ID (si aplica)
   */
  Mono<Ticket> save(Ticket ticket);

  Mono<Ticket> findById(Long id);
}
