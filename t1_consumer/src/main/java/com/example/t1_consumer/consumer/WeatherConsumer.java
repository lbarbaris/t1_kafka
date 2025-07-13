package com.example.t1_consumer.consumer;

import com.example.t1_consumer.dto.WeatherDto;
import com.example.t1_consumer.utils.WeatherTracker;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Objects.nonNull;

@Slf4j
@Component
public class WeatherConsumer { // версия на основе консьюмера
    private final ObjectMapper objectMapper;
    private final String topicName;
    private final String bootstrapServers;
    private final String groupId;

    private Consumer<String, String> consumer;
    private ExecutorService executorService;
    private volatile boolean running = true;

    private final WeatherTracker weatherTracker;

    @Autowired
    public WeatherConsumer(
            ObjectMapper objectMapper,
            @Value("${topic.name}") String topicName,
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id}") String groupId, WeatherTracker weatherTracker) {
        this.objectMapper = objectMapper;
        this.topicName = topicName;
        this.bootstrapServers = bootstrapServers;
        this.groupId = groupId;
        this.weatherTracker = weatherTracker;
    }

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topicName));

        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::consumeMessages);
    }

    public void consumeMessages() {
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                records.forEach(record -> {
                    try {
                        var weather = objectMapper.readValue(record.value(), WeatherDto.class);
                        weatherTracker.updateStatistics(weather);
                        log.info("Message consumed: {}", record.value());
                        log.info(weatherTracker.getWeatherExtremesReport());
                    } catch (Exception e) {
                        log.error("Error processing message: {}", record.value(), e);
                    }
                });

                consumer.commitSync();
            }
        } finally {
            consumer.close();
        }
    }

    @PreDestroy
    public void destroy() {
        running = false;
        if (nonNull(executorService)) {
            executorService.shutdown();
        }
        if (nonNull(consumer)) {
            consumer.close();
        }
    }
}