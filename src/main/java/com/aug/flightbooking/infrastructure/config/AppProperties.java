package com.aug.flightbooking.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private Redis redis;

    private Kafka kafka;

    // -----------------------------------------------
    // Subclases anidadas
    // -----------------------------------------------

    @Data
    public static class Redis {
        private long reservationTimeoutSeconds;
        private long periodFluxSeconds;
        private String keyPrefixReservationCache;
    }

    // -----------------------------------------------
    // Subclases anidadas
    // -----------------------------------------------

    @Data
    public static class Kafka {
        private String bootstrapServers;
        private String ordenConsumerGroup;
        private Producer producer;
        private Consumer consumer;

        @Data
        public static class Producer {
            private String reservationCreatedTopic;
            private String flightseatConfirmedTopic;
            private String flightseatRejectedTopic;
        }

        @Data
        public static class Consumer {
            private String reservationFlightCreatedGroupId;
            private String flightseatReservationConfirmedGroupId;
            private String flightseatReservationRejectedGroupId;
        }
    }

}
