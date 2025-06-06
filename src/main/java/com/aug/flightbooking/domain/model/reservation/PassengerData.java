package com.aug.flightbooking.domain.model.reservation;

/**
 * Representa los datos básicos del pasajero que realiza la reserva.
 * Es un objeto de valor y no debe tener identidad propia.
 */
public class PassengerData {

    /**
     * Nombre completo del pasajero.
     */
    private final String fullName;

    /**
     * Documento de identidad del pasajero.
     */
    private final String document;

    /**
     * Correo electrónico de contacto.
     */
    private final String email;

    /**
     * Crea un nuevo objeto de datos del pasajero.
     * Los campos no deben ser nulos ni vacíos.
     */
    public PassengerData(String fullName, String document, String email) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio.");
        }
        if (document == null || document.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio.");
        }

        this.fullName = fullName;
        this.document = document;
        this.email = email;
    }

    /**
     * Valida que los campos del pasajero sean válidos.
     * Método utilizado por entidades que lo contienen.
     */
    public boolean isValid() {
        return fullName != null && !fullName.isBlank()
                && document != null && !document.isBlank()
                && email != null && !email.isBlank();
    }

    // Getters públicos para exponer los datos

    public String getFullName() {
        return fullName;
    }

    public String getDocument() {
        return document;
    }

    public String getEmail() {
        return email;
    }

    /**
     * Dos objetos PassengerData son iguales si todos sus campos son iguales.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PassengerData other)) return false;
        return fullName.equals(other.fullName)
                && document.equals(other.document)
                && email.equals(other.email);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode() + document.hashCode() + email.hashCode();
    }
}
