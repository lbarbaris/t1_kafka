package com.example.t1_consumer.consumer;

import com.example.t1_consumer.dto.WeatherDto;
import com.example.t1_consumer.utils.WeatherTracker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class SpringWeatherConsumerTest {

    private ObjectMapper objectMapper;
    private WeatherTracker weatherTracker;
    private SpringWeatherConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        weatherTracker = mock(WeatherTracker.class);
        consumer = new SpringWeatherConsumer(objectMapper, weatherTracker);
    }

    @Test
    void testConsumeValidMessage() throws Exception {
        var dto = new WeatherDto(
                LocalDateTime.of(2025, 7, 13, 12, 0),
                "Berlin",
                25.3f,
                "Sunny"
        );
        var jsonMessage = objectMapper.writeValueAsString(dto);

        consumer.consumeMessage(jsonMessage);

        verify(weatherTracker, times(1)).updateStatistics(Mockito.any(WeatherDto.class));
        verify(weatherTracker, times(1)).getWeatherExtremesReport();
    }

    @Test
    void testConsumeInvalidMessage() {
        var invalidJson = "{bad json}";

        consumer.consumeMessage(invalidJson);

        verify(weatherTracker, never()).updateStatistics(any());
        verify(weatherTracker, never()).getWeatherExtremesReport();
    }

    @Test
    void testConsumeNullMessage() {
        String invalidJson = null;

        consumer.consumeMessage(invalidJson);

        verify(weatherTracker, never()).updateStatistics(any());
        verify(weatherTracker, never()).getWeatherExtremesReport();
    }
}
