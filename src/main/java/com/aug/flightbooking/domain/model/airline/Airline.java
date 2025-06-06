package com.aug.flightbooking.domain.model.airline;

/**
 * Representa los datos de una aerolínea como un objeto de valor.
 */
public record Airline(String name, String code) {

    // Dos aerolíneas son iguales si su código es igual
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Airline)) return false;
        Airline other = (Airline) obj;
        return code != null && code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return code != null ? code.hashCode() : 0;
    }
}
