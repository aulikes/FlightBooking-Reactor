package com.aug.flightbooking.application.service;

import com.aug.flightbooking.application.events.ReservationCreatedEvent;
import com.aug.flightbooking.application.ports.in.CreateReservationUseCase;
import com.aug.flightbooking.application.ports.out.ReservationCreatedEventPublisher;
import com.aug.flightbooking.application.ports.out.ReservationRepository;
import com.aug.flightbooking.application.commands.CreateReservationCommand;
import com.aug.flightbooking.application.ports.out.ReservationCache;
import com.aug.flightbooking.application.results.ReservationResult;
import com.aug.flightbooking.domain.models.reservation.PassengerInfo;
import com.aug.flightbooking.domain.models.reservation.Reservation;
import com.aug.flightbooking.domain.models.reservation.ReservationStatusAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Caso de uso reactivo para crear una nueva reserva.
 */
@Service
@RequiredArgsConstructor
public class CreateReservationService implements CreateReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationCreatedEventPublisher eventPublisher;
    private final ReservationCache reservationCache;
    private final ReservationStatusUpdater reservationStatusUpdater;

    @Override
    public Mono<ReservationResult> createReservation(CreateReservationCommand command) {

        Reservation reservation = Reservation.create(
                command.flightId(),
                new PassengerInfo(command.fullName(), command.documentId())
        );

        return reservationRepository.save(reservation)
            .flatMap(saved -> {
                // 1. Crear el evento
                ReservationCreatedEvent event = new ReservationCreatedEvent(
                      saved.getId(),
                      saved.getFlightId(),
                      saved.getPassengerInfo().getFullName(),
                      saved.getPassengerInfo().getDocumentId()
                );
                // 2. De forma asíncrona: publica el evento y envía registro de timeout a Redis
                Mono<Void> publish = eventPublisher.publish(event);
                Mono<Void> track = reservationCache.registerTimeout(saved.getId());

                // 3. Esperar a que ambos terminen, luego actualizar estado a PENDING
                return Mono.when(publish, track)
                        //Esperamos que ambos terminen con Exito, y guardamos el nuevo estado
                        .then(reservationStatusUpdater.updateStatus(
                                saved,
                                ReservationStatusAction.PENDING
                        ))
                        // En este caso Then es un estado opcional, se puede quitar pero no se hace el updateStatus
                        .thenReturn(saved);
                })

            .map(saved -> new ReservationResult(
                    saved.getId(),
                    saved.getFlightId(),
                    saved.getPassengerInfo().getFullName(),
                    saved.getPassengerInfo().getDocumentId(),
                    saved.getStatus().name(),
                    saved.getCreatedAt()
            ));
    }
}
