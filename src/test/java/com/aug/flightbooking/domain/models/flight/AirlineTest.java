package com.aug.flightbooking.domain.models.flight;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase Airline.
 * Verifica la creación correcta, validaciones y comportamiento de equals/hashCode.
 */
class AirlineTest {

    @Test
    @DisplayName("Constructor: crea correctamente con valores válidos")
    void constructor_creates_with_valid_values() {
        Airline airline = new Airline("Avianca", "AV");

        assertEquals("Avianca", airline.getName());
        assertEquals("AV", airline.getCode());
    }

    @Test
    @DisplayName("Constructor: lanza NullPointerException si name es null")
    void constructor_throws_if_name_null() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Airline(null, "AV"));
        assertEquals("El name no puede ser null", ex.getMessage());
    }

    @Test
    @DisplayName("Constructor: lanza NullPointerException si code es null")
    void constructor_throws_if_code_null() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Airline("Avianca", null));
        assertEquals("El code no puede ser null", ex.getMessage());
    }

    @Test
    @DisplayName("equals(): dos aerolíneas con el mismo código son iguales")
    void equals_same_code_are_equal() {
        Airline a1 = new Airline("Avianca", "AV");
        Airline a2 = new Airline("Otra", "AV");

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    @DisplayName("equals(): aerolíneas con códigos distintos no son iguales")
    void equals_different_code_not_equal() {
        Airline a1 = new Airline("Avianca", "AV");
        Airline a2 = new Airline("Latam", "LA");

        assertNotEquals(a1, a2);
    }

    @Test
    @DisplayName("equals(): es igual a sí misma")
    void equals_self() {
        Airline a1 = new Airline("Avianca", "AV");
        assertEquals(a1, a1);
    }

    @Test
    @DisplayName("equals(): no es igual a un objeto de otro tipo")
    void equals_other_type() {
        Airline a1 = new Airline("Avianca", "AV");
        assertNotEquals(a1, "cadena");
    }
}
