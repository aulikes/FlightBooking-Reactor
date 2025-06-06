package com.aug.flightbooking.domain.model.checkin;
/**
 * Representa los diferentes estados posibles de un tiquete dentro del sistema.
 */
public enum TicketStatus {
    EMITTED,        //El tiquete ha sido emitido correctamente y está listo para su uso.
    CHECKED_IN,     //El usuario realizó checkin
    CANCELLED,      //El tiquete fue cancelado antes de su uso.
    USED            //El tiquete ya fue utilizado para abordar el vuelo.
}

