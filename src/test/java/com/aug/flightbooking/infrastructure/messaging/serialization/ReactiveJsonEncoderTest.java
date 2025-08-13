package com.aug.flightbooking.infrastructure.messaging.serialization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Valida:
 *  - encode POJO sencillo -> bytes con JSON esperado
 *  - encode Map -> bytes no vacíos con contenido JSON
 * Nota: No se fuerza un caso de error aquí porque Jackson normalmente serializa muchos tipos
 * (incluyendo objetos con pocos campos) sin fallar.
 */
class ReactiveJsonEncoderTest {

    // POJO simple para serialización estable
    public static class TestDto {
        private Long id;
        private String message;

        public TestDto() {}
        public TestDto(Long id, String message) {
            this.id = id;
            this.message = message;
        }
        public Long getId() { return id; }
        public String getMessage() { return message; }
        public void setId(Long id) { this.id = id; }
        public void setMessage(String message) { this.message = message; }
    }

    private final ReactiveJsonEncoder encoder = new ReactiveJsonEncoder();

    @Test
    @DisplayName("encode(): serializa POJO a JSON bytes")
    void encode_serializes_pojo_to_json_bytes() {
        TestDto dto = new TestDto(42L, "OK");

        StepVerifier.create(encoder.encode(dto))
                .assertNext(bytes -> {
                    assertNotNull(bytes);
                    assertTrue(bytes.length > 0);
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    // Validamos contenido esperado (campos y valores)
                    assertTrue(json.contains("\"id\":42"), "JSON debe contener id");
                    assertTrue(json.contains("\"message\":\"OK\""), "JSON debe contener message");
                    // Además, que luzca como JSON
                    assertTrue(json.startsWith("{") && json.endsWith("}"));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("encode(): serializa un Map (contenido no vacío)")
    void encode_serializes_map() {
        var map = java.util.Map.of("a", 1, "b", "x");

        StepVerifier.create(encoder.encode(map))
                .assertNext(bytes -> {
                    assertNotNull(bytes);
                    assertTrue(bytes.length > 0);
                    String json = new String(bytes, StandardCharsets.UTF_8);
                    assertTrue(json.contains("\"a\":1"));
                    assertTrue(json.contains("\"b\":\"x\""));
                })
                .verifyComplete();
    }
}
