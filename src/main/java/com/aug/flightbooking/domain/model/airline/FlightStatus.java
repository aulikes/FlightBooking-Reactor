package com.aug.flightbooking.domain.model.airline;

public enum FlightStatus {
    SCHEDULED,     // Vuelo programado con fecha y hora
    BOARDING,      // En proceso de abordaje de pasajeros
    DELAYED,       // Vuelo retrasado
    IN_AIR,         // Vuelo en el aire
    FINISHED,      // Vuelo completado con éxito
    CANCELLED      // Vuelo cancelado
}