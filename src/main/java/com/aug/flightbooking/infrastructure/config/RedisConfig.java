package com.aug.flightbooking.infrastructure.config;

import io.lettuce.core.ClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

  private final AppProperties.Redis redisProperties;

  public RedisConfig(AppProperties appProperties) {
    this.redisProperties = appProperties.getRedis();
  }

  /**
   * Crea el template para Redis de RESERVAS (reservations).
   * Se conecta a host, puerto y base lógica definidos para ese contexto.
   */
  @Bean("reservationRedisTemplate")
  public ReactiveRedisTemplate<String, String> reservationRedisTemplate() {
    return buildRedisTemplate(
        redisProperties.getRedisReservation().getHost(),
        redisProperties.getRedisReservation().getPort(),
        redisProperties.getRedisReservation().getDatabase()
    );
  }

  /**
   * Crea el template para Redis de VUELOS (flights), utilizado para la seguridad de las IPs del webHook
   * Se conecta a host, puerto y base lógica definidos para ese contexto.
   */
  @Bean("flightRedisTemplate")
  public ReactiveRedisTemplate<String, String> flightRedisTemplate() {
    return buildRedisTemplate(
        redisProperties.getRedisFlight().getHost(),
        redisProperties.getRedisFlight().getPort(),
        redisProperties.getRedisFlight().getDatabase()
    );
  }

  /**
   * Método reutilizable que construye un ReactiveRedisTemplate
   * con configuración manual: serialización String, conexión Lettuce personalizada,
   * timeout y reconexión automática.
   */
  private ReactiveRedisTemplate<String, String> buildRedisTemplate(String host, int port, int db) {
    // Configuración de conexión a Redis (host, puerto, base lógica)
    RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(host, port);
    redisConfig.setDatabase(db);

    /*
     * Configura el cliente Lettuce con opciones de alto rendimiento:
     * - Timeout de comandos
     * - Reconexión automática
     * - Ping previo a activar conexión
     */
    LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
        .commandTimeout(Duration.ofMillis(redisProperties.getTimeout())) // equivalente a spring.data.redis.timeout
        .shutdownTimeout(Duration.ofMillis(100))
        .clientOptions(ClientOptions.builder()
            .autoReconnect(true)                      // reconectar si se pierde conexión
            .pingBeforeActivateConnection(true)       // verificar si la conexión es válida
            .build())
        .build();

    // Fábrica de conexiones reactivas usando la configuración anterior
    LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientConfig);
    factory.afterPropertiesSet(); // Necesario al crear la factory manualmente

    // Define un ReactiveRedisTemplate con serialización básica de claves y valores tipo String <-> String
    RedisSerializationContext<String, String> context = RedisSerializationContext
        .<String, String>newSerializationContext(RedisSerializer.string())
        .key(RedisSerializer.string())
        .value(RedisSerializer.string())
        .build();

    return new ReactiveRedisTemplate<>(factory, context);
  }
}


