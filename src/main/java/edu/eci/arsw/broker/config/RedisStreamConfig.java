package edu.eci.arsw.broker.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

@Configuration
public class RedisStreamConfig {

    private static final String STREAM_KEY = "transfer-events";
    private static final String GROUP = "fraude-group";

    @Bean
    public ApplicationRunner createConsumerGroup(StringRedisTemplate redisTemplate) {
        return args -> {
            try {
                Boolean streamExists = redisTemplate.hasKey(STREAM_KEY);

                if (Boolean.FALSE.equals(streamExists)) {
                    redisTemplate.opsForStream()
                            .add(STREAM_KEY, Map.of("init", "true"));

                    System.out.println("Stream creado: " + STREAM_KEY);
                }

                redisTemplate.opsForStream()
                        .createGroup(STREAM_KEY, ReadOffset.latest(), GROUP);

                System.out.println("Consumer group creado: " + GROUP);

            } catch (RedisSystemException e) {
                String message = e.getMessage();

                if (message != null && message.contains("BUSYGROUP")) {
                    System.out.println("Consumer group ya existe: " + GROUP);
                } else {
                    throw e;
                }
            }
        };
    }
}