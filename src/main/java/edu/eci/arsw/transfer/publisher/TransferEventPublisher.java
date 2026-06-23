package edu.eci.arsw.transfer.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.eci.arsw.transfer.model.TransferEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TransferEventPublisher {

    private static final String STREAM_KEY = "transfer-events";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public TransferEventPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(TransferEvent event) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("eventType", "TRANSFER_CREATED");
            body.put("payload", objectMapper.writeValueAsString(event));

            redisTemplate.opsForStream()
                    .add(STREAM_KEY, body);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error publicando evento de transferencia", e);
        }
    }
}