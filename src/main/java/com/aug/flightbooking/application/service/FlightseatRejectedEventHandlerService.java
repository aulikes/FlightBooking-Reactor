package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.FlightseatRejectedEvent;
import com.aug.flightbooking.application.ports.in.FlightseatRejectedEventHandler;
import com.aug.flightbooking.application.ports.out.ReservationCache;
import com.aug.flightbooking.domain.model.reservation.ReservationStatusAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Servicio de aplicación que maneja eventos externos del contexto Flight y actualiza el estado de
 * las reservas según la disponibilidad.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FlightseatRejectedEventHandlerService implements FlightseatRejectedEventHandler {

  private final ReservationStatusUpdater reservationStatusUpdater;
  private final ReservationCache reservationCache;

  /**
   * Maneja el evento de reserva rechazada y actualiza el estado a REJECTED.
   */
  @Override
  public Mono<Void> handle(FlightseatRejectedEvent event) {
    return reservationStatusUpdater
        .updateStatus(event.reservationId(), ReservationStatusAction.REJECTED)
        .onErrorResume(error -> {
          log.error("Error actualizando estado de reserva {}", event.reservationId(), error);
          return Mono.empty();
        })
        .then(
            reservationCache.cancelTimeout(event.reservationId())
                .onErrorResume(error -> {
                  log.error("Error cancelando timeout de reserva {}", event.reservationId(), error);
                  return Mono.empty();
                })
        );
  }
}
