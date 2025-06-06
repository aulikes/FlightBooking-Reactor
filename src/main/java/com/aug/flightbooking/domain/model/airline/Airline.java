package com.aug.flightbooking.domain.model.airline;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Representa la entidad raíz del contexto Airline. Contiene los vuelos asociados a la aerolínea.
 */
public class Airline {

    // Identificador único de la aerolínea, generado por la base de datos
    private Long id;

    // Nombre comercial de la aerolínea
    private String name;

    // País de origen de la aerolínea
    private String country;

    // Lista de vuelos asociados a esta aerolínea (entidad dependiente)
    private final List<Flight> flights = new ArrayList<>();

    /**
     * Constructor de la entidad Airline.
     * Inicializa el estado con los valores principales.
     */
    public Airline(Long id, String name, String country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    /**
     * Permite agregar un nuevo vuelo a la lista de vuelos de esta aerolínea.
     */
    public void addFlight(Flight flight) {
        this.flights.add(flight);
    }

    /**
     * Elimina un vuelo según su identificador.
     * Si el ID coincide, se remueve de la lista.
     */
    public void removeFlightById(Long flightId) {
        this.flights.removeIf(f -> f.getId().equals(flightId));
    }

    /**
     * Busca y retorna un vuelo por su identificador.
     * Si no existe, retorna un Optional vacío.
     */
    public Optional<Flight> findFlightById(Long flightId) {
        return this.flights.stream()
                .filter(f -> f.getId().equals(flightId))
                .findFirst();
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
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

    public void setCountry(String country) {
        this.country = country;
    }
}
