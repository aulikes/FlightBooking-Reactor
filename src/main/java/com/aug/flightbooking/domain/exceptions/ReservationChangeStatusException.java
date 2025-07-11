package com.aug.flightbooking.domain.exceptions;

public class ReservationChangeStatusException extends RuntimeException {
    public ReservationChangeStatusException(String message) {
        super(message);
    }
}
