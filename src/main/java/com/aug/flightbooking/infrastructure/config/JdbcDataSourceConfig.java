package com.aug.flightbooking.infrastructure.config;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Fuerza la creación de un DataSource JDBC para que Liquibase pueda ejecutarse,
 * ya que usamos R2DBC y no se crea automáticamente.
 */
@Configuration
@RequiredArgsConstructor
public class JdbcDataSourceConfig {

  @Value("${spring.r2dbc.urlJdbc}")
  private String url;

  @Value("${spring.r2dbc.username}")
  private String usr;

  @Value("${spring.r2dbc.password}")
  private String psw;

  @Bean
  public DataSource dataSource() {
    return DataSourceBuilder.create()
        .url(url)
        .username(usr)
        .password(psw)
        .build();
  }
}

