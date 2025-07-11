package com.aug.flightbooking.domain.models.reservation;

import java.util.Objects;

/**
 * Value Object que representa la información básica del pasajero asociada a una reserva.
 * No tiene identidad propia ni comportamiento, solo contiene datos referenciales.
 */
public class PassengerInfo {

    private final String fullName;
    private final String documentId;

    public PassengerInfo(String fullName, String documentId) {
        this.fullName = Objects.requireNonNull(fullName, "El fullName no puede ser nul");
        this.documentId = Objects.requireNonNull(documentId, "El documentId no puede ser nul");
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
