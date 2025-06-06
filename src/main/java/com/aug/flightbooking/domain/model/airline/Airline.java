package com.aug.flightbooking.domain.model.airline;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa una aerolínea como Aggregate Root.
 * Contiene una lista de vuelos asociados como entidades dependientes.
 */
public class Airline {

    private Long id;
    private String name;
    private String iataCode;
    private List<Flight> flights;

    /**
     * Constructor por defecto requerido para algunas librerías y frameworks.
     */
    protected Airline() {
        this.flights = new ArrayList<>();
    }

    /**
     * Constructor principal de la entidad Airline.
     *
     * @param name     nombre de la aerolínea.
     * @param iataCode código IATA de la aerolínea.
     */
    public Airline(String name, String iataCode) {
        this.name = name;
        this.iataCode = iataCode;
        this.flights = new ArrayList<>();
    }

    /**
     * Agrega un vuelo a la lista de vuelos de la aerolínea.
     *
     * @param flight objeto de vuelo que se agregará.
     */
    public void addFlight(Flight flight) {
        if (flight == null) {
            throw new IllegalArgumentException("El vuelo no puede ser nulo.");
        }
        this.flights.add(flight);
    }

    /**
     * Busca un vuelo por su código.
     *
     * @param flightCode código del vuelo.
     * @return el objeto Flight si se encuentra, de lo contrario null.
     */
    public Flight findFlightByCode(String flightCode) {
        return flights.stream()
                .filter(f -> f.getFlightCode().equalsIgnoreCase(flightCode))
                .findFirst()
                .orElse(null);
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIataCode() {
        return iataCode;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Airline)) return false;
        Airline airline = (Airline) o;
        return Objects.equals(id, airline.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
