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
        private long timeout;
        private RedisReservation redisReservation;
        private RedisFlight redisFlight;

        @Data
        public static class RedisReservation {
            private String host;
            private int port;
            private int database;
            private long reservationTimeoutSeconds;
            private long periodFluxSeconds;
            private String keyPrefixReservationCache;
        }

        @Data
        public static class RedisFlight {
            private String host;
            private int port;
            private int database;
        }
    }

    @Data
    public static class Kafka {
        private String bootstrapServers;
        private String ordenConsumerGroup;
        private Producer producer;
        private Consumer consumer;

        @Data
        public static class Producer {
            private String reservationCreatedTopic;
            private String reservationConfirmedTopic;
            private String flightseatConfirmedTopic;
            private String flightseatRejectedTopic;
        }

        @Data
        public static class Consumer {
            private String reservationFlightCreatedGroupId;
            private String reservationTicketConfirmedGroupId;
            private String flightseatReservationConfirmedGroupId;
            private String flightseatReservationRejectedGroupId;
        }
    }

}
