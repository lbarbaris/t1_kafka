package com.example.t1_producer.producer;

import com.example.t1_producer.dto.WeatherDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringWeatherProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SpringWeatherProducer springWeatherProducer;

    private final String topicName = "test-topic";
    private WeatherDto testWeatherDto;

    @BeforeEach
    void setUp() {
        springWeatherProducer.setTopicName(topicName);
        testWeatherDto = new WeatherDto(
                LocalDateTime.now(),
                "Berlin",
                25.3f,
                "Sunny"
        );
    }

    @Test
    void testSendMessageSuccess() throws Exception {
        var jsonMessage = "{\"city\":\"Berlin\",\"temperature\":25.3}";
        when(objectMapper.writeValueAsString(testWeatherDto)).thenReturn(jsonMessage);

        springWeatherProducer.sendMessage(testWeatherDto);

        verify(objectMapper).writeValueAsString(testWeatherDto);
        verify(kafkaTemplate).send(eq(topicName), eq(jsonMessage));
    }

    @Test
    void testSendMessageJsonProcessingException() throws Exception {
        when(objectMapper.writeValueAsString(any(WeatherDto.class)))
                .thenThrow(new JsonProcessingException("Test exception") {});

        springWeatherProducer.sendMessage(testWeatherDto);

        verify(objectMapper).writeValueAsString(testWeatherDto);
        verify(kafkaTemplate, never()).send(anyString(), anyString());
    }

    @Test
    void testSendMessageKafkaException() throws Exception {
        var jsonMessage = "{\"city\":\"Berlin\",\"temperature\":25.3}";
        when(objectMapper.writeValueAsString(testWeatherDto)).thenReturn(jsonMessage);
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), anyString());

        springWeatherProducer.sendMessage(testWeatherDto);

        verify(objectMapper).writeValueAsString(testWeatherDto);
        verify(kafkaTemplate).send(eq(topicName), eq(jsonMessage));
    }
}