package com.example.t1_consumer.consumer;

import com.example.t1_consumer.dto.WeatherDto;
import com.example.t1_consumer.utils.WeatherTracker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class WeatherConsumerTest {

    private ObjectMapper objectMapper;
    private WeatherTracker weatherTracker;
    private MockConsumer<String, String> mockConsumer;
    private WeatherConsumer weatherConsumer;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        weatherTracker = mock(WeatherTracker.class);

        mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);
        TopicPartition topicPartition = new TopicPartition("test-topic", 0);
        mockConsumer.assign(Collections.singletonList(topicPartition));
        HashMap<TopicPartition, Long> beginningOffsets = new HashMap<>();
        beginningOffsets.put(topicPartition, 0L);
        mockConsumer.updateBeginningOffsets(beginningOffsets);

        weatherConsumer = new WeatherConsumer(
                objectMapper,
                "test-topic",
                "localhost:9092",
                "test-group",
                weatherTracker
        );

        setField(weatherConsumer, "consumer", mockConsumer);

        executorService = Executors.newSingleThreadExecutor();
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
    void testConsumeValidMessage() throws Exception {
        var dto = new WeatherDto(
                LocalDateTime.of(2025, 7, 13, 12, 0),
                "Berlin",
                25.3f,
                "Sunny"
        );
        var jsonMessage = objectMapper.writeValueAsString(dto);

        mockConsumer.addRecord(new ConsumerRecord<>("test-topic", 0, 0, "key", jsonMessage));

        executorService.submit(() -> weatherConsumer.consumeMessages());

        Thread.sleep(500);

        verify(weatherTracker, times(1)).updateStatistics(Mockito.any(WeatherDto.class));
        verify(weatherTracker, times(1)).getWeatherExtremesReport();
    }

    @Test
    void testConsumeInvalidMessage() throws Exception {
        var invalidJson = "{bad json}";

        mockConsumer.addRecord(new ConsumerRecord<>("test-topic", 0, 0, "key", invalidJson));

        executorService.submit(() -> weatherConsumer.consumeMessages());

        Thread.sleep(500);

        verify(weatherTracker, never()).updateStatistics(any());
        verify(weatherTracker, never()).getWeatherExtremesReport();
    }

    @Test
    void testStopConsumer() {
        setField(weatherConsumer, "executorService", executorService);
        setField(weatherConsumer, "running", true);

        weatherConsumer.destroy();

        assertTrue(executorService.isShutdown());
        assertTrue(mockConsumer.closed());
    }

    @Test
    void testNoMessages() throws Exception {
        executorService.submit(() -> weatherConsumer.consumeMessages());

        Thread.sleep(500);

        verify(weatherTracker, never()).updateStatistics(any());
        verify(weatherTracker, never()).getWeatherExtremesReport();
    }
}
