package com.aug.flightbooking.domain.exception;

public class ReservationChangeStatusException extends RuntimeException {
    public ReservationChangeStatusException(String message) {
        super(message);
    }
}
