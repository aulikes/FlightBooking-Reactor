
package com.aug.flightbooking.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private final AppProperties.Kafka.Producer producer;

    public KafkaTopicConfig(AppProperties appProperties) {
        this.producer = appProperties.getKafka().getProducer();
    }

    @Bean
    public NewTopic reservationCreatedTopic() {
        return TopicBuilder.name(producer.getReservationCreatedTopic()).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic reservationConfirmedTopic() {
        return TopicBuilder.name(producer.getReservationConfirmedTopic()).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic flightseatConfirmedTopic() {
        return TopicBuilder.name(producer.getFlightseatConfirmedTopic()).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic flightseatRejectedTopic() {
        return TopicBuilder.name(producer.getFlightseatRejectedTopic()).partitions(1).replicas(1).build();
    }

}
