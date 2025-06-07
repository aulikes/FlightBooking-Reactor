package com.aug.flightbooking.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
@Data
public class ApplicationProperties {

    private Redis redis;


    // -----------------------------------------------
    // Subclases anidadas
    // -----------------------------------------------

    @Data
    public static class Redis {
        private long reservationTimeoutSeconds;
        private long periodFluxSeconds;
        private String keyPrefixReservationCache;
    }

}
