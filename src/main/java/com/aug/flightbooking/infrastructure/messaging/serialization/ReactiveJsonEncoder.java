package com.aug.flightbooking.infrastructure.messaging.serialization;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ReactiveJsonEncoder {

    private final Jackson2JsonEncoder encoder;
    private final DefaultDataBufferFactory bufferFactory;

    public ReactiveJsonEncoder() {
        this.encoder = new Jackson2JsonEncoder();
        this.bufferFactory = new DefaultDataBufferFactory();
    }

    public <T> Mono<byte[]> encode(T value) {
        ResolvableType type = ResolvableType.forInstance(value);

        return DataBufferUtils.join(
                encoder.encode(
                        Mono.just(value),
                        bufferFactory,
                        type,
                        MediaType.APPLICATION_JSON,
                        null
                )
        ).map(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);
            return bytes;
        });
    }
}
