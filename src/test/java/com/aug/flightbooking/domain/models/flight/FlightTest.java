package com.aug.flightbooking.domain.models.flight;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el agregado Flight.
 * Valida fábricas, estado inicial, transiciones, reserva de asientos y equals/hashCode.
 */
class FlightTest {

    private Airline airline() {
        return new Airline("Avianca", "AV");
    }

    // ---------- Fábricas ----------

    @Test
    @DisplayName("create(): crea un vuelo programado con valores válidos")
    void create_builds_scheduled_flight() {
        Instant dep = Instant.now().plus(Duration.ofHours(2));
        Instant arr = dep.plus(Duration.ofHours(1));

        Flight f = Flight.create(airline(), "AV123", "BOG", "MDE", 180, 0, dep, arr);

        assertNull(f.getId());
        assertEquals("AV123", f.getFlightCode());
        assertEquals("BOG", f.getOrigin());
        assertEquals("MDE", f.getDestination());
        assertEquals(180, f.getTotalSeats());
        assertEquals(0, f.getReservedSeats());
        assertEquals(dep, f.getScheduledDeparture());
        assertEquals(arr, f.getScheduledArrival());
        assertEquals(FlightStatus.SCHEDULED, f.getStatus());
    }

    @Test
    @DisplayName("fromPersistence(): reconstruye con id y estado dados")
    void fromPersistence_rebuilds() {
        Instant dep = Instant.now().plus(Duration.ofHours(2));
        Instant arr = dep.plus(Duration.ofHours(1));

        Flight rebuilt = Flight.fromPersistence(
                10L, airline(), "AV123", "BOG", "MDE",
                100, 5, dep, arr, FlightStatus.BOARDING
        );

        assertEquals(10L, rebuilt.getId());
        assertEquals(FlightStatus.BOARDING, rebuilt.getStatus());
        assertEquals(5, rebuilt.getReservedSeats());
        assertEquals(100, rebuilt.getTotalSeats());
    }

    @Test
    @DisplayName("fromPersistence(): id null lanza IllegalArgumentException con mensaje esperado")
    void fromPersistence_id_null_throws() {
        Instant dep = Instant.now().plus(Duration.ofHours(1));
        Instant arr = dep.plus(Duration.ofHours(2));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                Flight.fromPersistence(
                        null, airline(), "AV001", "BOG", "CTG",
                        180, 0, dep, arr, FlightStatus.SCHEDULED
                )
        );
        assertEquals("El id no puede ser nulo", ex.getMessage());
    }

    // ---------- Gestión de asientos ----------

    @Test
    @DisplayName("hasAvailableSeats()/tryReserveSeat(): incrementa hasta total y luego retorna false")
    void seats_reservation_flow() {
        Instant dep = Instant.now().plus(Duration.ofHours(2));
        Instant arr = dep.plus(Duration.ofHours(1));
        Flight f = Flight.create(airline(), "AV001", "BOG", "CTG", 2, 1, dep, arr);

        assertTrue(f.hasAvailableSeats());
        assertEquals(1, f.getReservedSeats());

        // Reserva 1: pasa a 2 y retorna true
        assertTrue(f.tryReserveSeat());
        assertEquals(2, f.getReservedSeats());
        assertFalse(f.hasAvailableSeats());

        // Reserva adicional ya no debe cambiar el conteo
        assertFalse(f.tryReserveSeat());
        assertEquals(2, f.getReservedSeats());
    }

    // ---------- Transiciones de estado ----------

    @Test
    @DisplayName("startBoarding(): solo permitido desde SCHEDULED; si no, IllegalStateException")
    void startBoarding_rules() {
        Instant dep = Instant.now().plus(Duration.ofHours(2));
        Instant arr = dep.plus(Duration.ofHours(1));
        Flight f = Flight.create(airline(), "AV002", "BOG", "PEI", 180, 0, dep, arr);

        f.startBoarding();
        assertEquals(FlightStatus.BOARDING, f.getStatus());

        // Intento inválido: iniciar abordaje nuevamente
        IllegalStateException ex = assertThrows(IllegalStateException.class, f::startBoarding);
        assertTrue(ex.getMessage().contains("iniciar abordaje"));
    }

    @Test
    @DisplayName("takeOff(): solo permitido después de startBoarding()")
    void takeOff_rules() {
        Instant dep = Instant.now().plus(Duration.ofHours(2));
        Instant arr = dep.plus(Duration.ofHours(1));
        Flight f = Flight.create(airline(), "AV003", "BOG", "BAQ", 180, 0, dep, arr);

        // No debería permitir despegar sin boarding
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, f::takeOff);
        assertTrue(ex1.getMessage().contains("despegar"));

        f.startBoarding();
        f.takeOff();
        assertEquals(FlightStatus.IN_AIR, f.getStatus());
    }

    @Test
    @DisplayName("land(): solo permitido cuando está en el aire")
    void land_rules() {
        Instant dep = Instant.now().plus(Duration.ofHours(2));
        Instant arr = dep.plus(Duration.ofHours(1));
        Flight f = Flight.create(airline(), "AV004", "BOG", "CLO", 180, 0, dep, arr);

        // No debería permitir aterrizar si no está en el aire
        IllegalStateException ex1 = assertThrows(IllegalStateException.class, f::land);
        assertTrue(ex1.getMessage().contains("aterrizar"));

        f.startBoarding();
        f.takeOff();
        f.land();
        assertEquals(FlightStatus.FINISHED, f.getStatus());
    }

    @Test
    @DisplayName("cancel(): no permite cancelar si está en el aire o ya finalizado; sí desde SCHEDULED/BOARDING")
    void cancel_rules() {
        Instant dep = Instant.now().plus(Duration.ofHours(2));
        Instant arr = dep.plus(Duration.ofHours(1));

        // Cancelación válida desde SCHEDULED
        Flight f = Flight.create(airline(), "AV005", "BOG", "SMR", 180, 0, dep, arr);
        f.cancel();
        assertEquals(FlightStatus.CANCELLED, f.getStatus());

        // Cancelación válida desde BOARDING
        Flight boarding = Flight.create(airline(), "AV006", "BOG", "MDE", 180, 0, dep, arr);
        boarding.startBoarding();
        boarding.cancel();
        assertEquals(FlightStatus.CANCELLED, boarding.getStatus());

        // Desde IN_AIR y FINISHED debe lanzar excepción
        Flight inAir = Flight.create(airline(), "AV007", "BOG", "MDE", 180, 0, dep, arr);
        inAir.startBoarding();
        inAir.takeOff();
        IllegalStateException ex2 = assertThrows(IllegalStateException.class, inAir::cancel);
        assertTrue(ex2.getMessage().contains("No se puede cancelar"));

        Flight finished = Flight.create(airline(), "AV008", "BOG", "CLO", 180, 0, dep, arr);
        finished.startBoarding();
        finished.takeOff();
        finished.land();
        IllegalStateException ex3 = assertThrows(IllegalStateException.class, finished::cancel);
        assertTrue(ex3.getMessage().contains("No se puede cancelar"));
    }

    // ---------- equals/hashCode y toString ----------

    @Test
    @DisplayName("equals()/hashCode: identidad por id (mismo id -> equals true; distinto id -> false)")
    void equality_by_id() {
        Instant dep = Instant.now().plus(Duration.ofHours(1));
        Instant arr = dep.plus(Duration.ofHours(2));

        Flight a = Flight.fromPersistence(1L, airline(), "AV010", "BOG", "MDE", 180, 0, dep, arr, FlightStatus.SCHEDULED);
        Flight b = Flight.fromPersistence(1L, airline(), "OTRO", "BOG", "CTG", 100, 5, dep, arr, FlightStatus.BOARDING);
        Flight c = Flight.fromPersistence(2L, airline(), "AV010", "BOG", "MDE", 180, 0, dep, arr, FlightStatus.SCHEDULED);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("equals(): dos vuelos sin id (null) resultan iguales según la implementación actual")
    void equality_transient_null_id() {
        Instant dep = Instant.now().plus(Duration.ofHours(1));
        Instant arr = dep.plus(Duration.ofHours(2));

        Flight x = Flight.create(airline(), "AX001", "BOG", "MDE", 100, 0, dep, arr);
        Flight y = Flight.create(airline(), "AX002", "BOG", "CTG", 120, 0, dep, arr);

        // Objects.equals(null, null) -> true; esta prueba documenta el comportamiento actual
        assertEquals(x, y);
    }

    @Test
    @DisplayName("toString(): incluye algunos campos clave")
    void toString_contains_fields() {
        Instant dep = Instant.now().plus(Duration.ofHours(1));
        Instant arr = dep.plus(Duration.ofHours(2));

        Flight f = Flight.create(airline(), "AV999", "BOG", "MDE", 180, 0, dep, arr);
        String s = f.toString();

        assertNotNull(s);
        assertTrue(s.contains("AV999"));
        assertTrue(s.contains("BOG"));
        assertTrue(s.contains("MDE"));
        assertTrue(s.contains(f.getStatus().name()));
    }
}
