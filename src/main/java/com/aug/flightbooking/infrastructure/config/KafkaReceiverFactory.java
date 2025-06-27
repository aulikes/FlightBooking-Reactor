package com.aug.flightbooking.infrastructure.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.List;
import java.util.Map;

public class KafkaReceiverFactory {

    /**
     * Crea un KafkaReceiver configurado para procesamiento manual de offset.
     * @param bootstrapServers dirección del broker Kafka
     * @param topic nombre del topic a suscribirse
     * @param groupId identificador del grupo de consumidor
     * @return instancia de KafkaReceiver lista para recibir mensajes
     */
    public static KafkaReceiver<String, byte[]> createReceiver(String bootstrapServers, String topic, String groupId) {
        Map<String, Object> props = Map.of(
                // Dirección del broker Kafka
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,

                // Deserializadores
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class,

                // Grupo de consumidor
                ConsumerConfig.GROUP_ID_CONFIG, groupId,

                // No hacer commit automático de offsets
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,

                // Si no hay offset almacenado, leer desde el inicio del log
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",

                // Máximo de registros por poll (performance)
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100,

                // Solo leer mensajes que ya hayan sido "committed" si el productor es transaccional
                ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"
        );

        ReceiverOptions<String, byte[]> options = ReceiverOptions.<String, byte[]>create(props)
                .subscription(List.of(topic));

        return KafkaReceiver.create(options);
    }
}
