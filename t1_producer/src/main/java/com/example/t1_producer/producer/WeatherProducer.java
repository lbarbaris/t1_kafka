package com.example.t1_producer.producer;

import com.example.t1_producer.dto.WeatherDto;
import com.example.t1_producer.service.WeatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
@Setter
public class WeatherProducer implements WeatherProducerInterface {

    @Value("${topic.name}")
    private String topicName;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ObjectMapper objectMapper;
    private Producer<String, String> producer;

    public WeatherProducer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("acks", "all");
        props.put("retries", 3);

        this.producer = new KafkaProducer<>(props);
    }

    @Override
    public void sendMessage(WeatherDto weatherDto) {
        try {
            var message = objectMapper.writeValueAsString(weatherDto);
            var record = new ProducerRecord<String, String>(topicName, message);
            producer.send(record, (metadata, exception) -> {
            });
            log.info("Send message {}", weatherDto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void close() {
        if (producer != null) {
            producer.close();
            log.info("Kafka producer closed");
        }
    }
}