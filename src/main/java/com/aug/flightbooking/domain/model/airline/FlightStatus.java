package com.aug.flightbooking.domain.model.airline;

public enum FlightStatus {
    CREATED,       // Vuelo creado pero no programado aún
    SCHEDULED,     // Vuelo programado con fecha y hora
    BOARDING,      // En proceso de abordaje de pasajeros
    DELAYED,       // Vuelo retrasado
    FINISHED,      // Vuelo completado con éxito
    CANCELLED      // Vuelo cancelado
}