package com.aug.flightbooking.infrastructure.messaging.serialization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Valida:
 *  - decode JSON válido -> POJO esperado
 *  - decode JSON inválido -> emite error
 */
class ReactiveJsonDecoderTest {

    // POJO simple para deserialización estable
    public static class TestDto {
        private Long id;
        private String message;

        public TestDto() {}
        public Long getId() { return id; }
        public String getMessage() { return message; }
        public void setId(Long id) { this.id = id; }
        public void setMessage(String message) { this.message = message; }
    }

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private final ReactiveJsonDecoder decoder = new ReactiveJsonDecoder(objectMapper);

    @Test
    @DisplayName("decode(): deserializa JSON válido a POJO")
    void decode_valid_json_to_pojo() {
        String json = "{\"id\":42,\"message\":\"OK\"}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        StepVerifier.create(decoder.decode(bytes, TestDto.class))
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(42L, dto.getId());
                    assertEquals("OK", dto.getMessage());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("decode(): JSON inválido -> emite error")
    void decode_invalid_json_emits_error() {
        String invalid = "{ this is not valid json ";
        byte[] bytes = invalid.getBytes(StandardCharsets.UTF_8);

        StepVerifier.create(decoder.decode(bytes, TestDto.class))
                .expectErrorMatches(ex -> ex instanceof RuntimeException || ex instanceof com.fasterxml.jackson.core.JsonProcessingException)
                .verify();
    }
}
