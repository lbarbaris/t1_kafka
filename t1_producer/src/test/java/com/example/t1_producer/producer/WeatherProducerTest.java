package com.example.t1_producer.producer;

import com.example.t1_producer.dto.WeatherDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherProducerTest {

    private WeatherProducer weatherProducer;
    private MockProducer<String, String> mockProducer;
    private ObjectMapper objectMapper;
    private final String topicName = "test-topic";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockProducer = new MockProducer<>(true, new StringSerializer(), new StringSerializer());

        weatherProducer = new WeatherProducer(objectMapper);
        weatherProducer.setTopicName(topicName);

        setField(weatherProducer, "producer", mockProducer);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSendMessageSuccess() throws JsonProcessingException {
        var weatherDto = new WeatherDto(
                LocalDateTime.now(),
                "Berlin",
                25.3f,
                "Sunny"
        );

        weatherProducer.sendMessage(weatherDto);

        assertEquals(1, mockProducer.history().size());
        ProducerRecord<String, String> record = mockProducer.history().get(0);
        assertEquals(topicName, record.topic());
        assertEquals(objectMapper.writeValueAsString(weatherDto), record.value());
    }

    @Test
    void testSendMessageWithJsonProcessingException() throws JsonProcessingException {
        var failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Test exception") {});

        var failingProducer = new WeatherProducer(failingMapper);
        failingProducer.setTopicName("test-topic");
        setField(failingProducer, "producer", mockProducer);

        var weatherDto = new WeatherDto(
                LocalDateTime.now(),
                "Berlin",
                25.3f,
                "Sunny"
        );

        failingProducer.sendMessage(weatherDto);

        assertEquals(0, mockProducer.history().size());
    }

    @Test
    void testCloseProducer() {
        weatherProducer.close();
        assertTrue(mockProducer.closed());
    }

    @Test
    void testCloseWhenProducerNull() {
        setField(weatherProducer, "producer", null);
        assertDoesNotThrow(() -> weatherProducer.close());
    }

    @Test
    void testInitProducer() {
        var producer = new WeatherProducer(objectMapper);
        producer.setTopicName("test-topic");
        producer.setBootstrapServers("localhost:9092");

        producer.init();

        assertNotNull(getField(producer, "producer"));
    }

    private Object getField(Object target, String fieldName) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}