package com.aug.flightbooking.application.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Contrato base para todos los eventos que se publican
 * Cada evento debe extender esta interfaz.
 */
public interface IntegrationEvent extends Serializable {

    /**
     * Establece la Traza del proceso, por el momento lo vamos a dejar instanciado aquí, pero cada proceso lo debe instanciar
     */
    default String getTraceId(){
        return UUID.randomUUID().toString();
    };

    /**
     * ID único del evento, útil para trazabilidad.
     */
    default String getEventId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Marca de tiempo del evento.
     */
    default Instant getTimestamp() {
        return Instant.now();
    }

    /**
     * Nombre lógico del evento, usado como header para identificación.
     * Ejemplo: "OrdenCreadaEvent"
     */
    String getEventType();

    /**
     * Versión del evento, útil para evolución sin romper consumidores.
     * Ejemplo: "v1"
     */
    String getVersion();

}
