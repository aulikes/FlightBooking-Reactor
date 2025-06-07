package com.aug.flightbooking.domain.model.reservation;

/**
 * Value Object que representa la información básica del pasajero asociada a una reserva.
 * No tiene identidad propia ni comportamiento, solo contiene datos referenciales.
 */
public class PassengerInfo {

    private final String fullName;
    private final String documentId;

    public PassengerInfo(String fullName, String documentId) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("El nombre completo no puede estar vacío.");
        }
        if (documentId == null || documentId.isBlank()) {
            throw new IllegalArgumentException("El número de identificación no puede estar vacío.");
        }
        this.fullName = fullName;
        this.documentId = documentId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDocumentId() {
        return documentId;
    }

    // Equals y hashCode para respetar las reglas de los Value Objects

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PassengerInfo)) return false;

        PassengerInfo that = (PassengerInfo) o;
        return fullName.equals(that.fullName) && documentId.equals(that.documentId);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode() * 31 + documentId.hashCode();
    }
}
