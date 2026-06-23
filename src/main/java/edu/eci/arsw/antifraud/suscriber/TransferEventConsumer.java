package edu.eci.arsw.antifraud.suscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import edu.eci.arsw.transfer.model.TransferEvent;

import java.util.List;

@Component
public class TransferEventConsumer {

    private static final String STREAM_KEY = "transfer-events";
    private static final String GROUP = "fraude-group";
    private static final String CONSUMER = "consumer-1";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public TransferEventConsumer(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
                Consumer.from(GROUP, CONSUMER),
                StreamReadOptions.empty().count(10),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
        );

        if (records == null || records.isEmpty()) {
            return;
        }

        for (MapRecord<String, Object, Object> record : records) {
            try {
                Object eventType = record.getValue().get("eventType");
                Object payload = record.getValue().get("payload");

                if (eventType == null || payload == null) {
                    redisTemplate.opsForStream()
                            .acknowledge(STREAM_KEY, GROUP, record.getId());
                    continue;
                }

                System.out.println("Processing: " + eventType);
                System.out.println("Payload: " + payload);

                TransferEvent event = objectMapper.readValue(
                        payload.toString(),
                        TransferEvent.class
                );

                System.out.println("Transferencia recibida en antifraud: " + event);

                redisTemplate.opsForStream()
                        .acknowledge(STREAM_KEY, GROUP, record.getId());

            } catch (Exception e) {
                System.err.println("Error procesando evento: " + record.getId());
                e.printStackTrace();
            }
        }
    }
}