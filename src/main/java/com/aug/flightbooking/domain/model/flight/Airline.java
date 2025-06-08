package com.aug.flightbooking.domain.model.flight;

import java.util.Objects;

/**
 * Representa los datos de una aerolínea como un objeto de valor.
 */
public class Airline {

    private final String name;
    private final String code;

    public Airline(String name, String code) {
        this.name = Objects.requireNonNull(name, "El name no puede ser null");
        this.code = Objects.requireNonNull(code, "El code no puede ser null");
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

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
