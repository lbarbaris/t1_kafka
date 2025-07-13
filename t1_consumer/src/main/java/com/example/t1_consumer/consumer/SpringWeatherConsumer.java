package com.example.t1_consumer.consumer;

import com.example.t1_consumer.dto.WeatherDto;
import com.example.t1_consumer.utils.WeatherTracker;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SpringWeatherConsumer { //Версия на основе листенера
    private final ObjectMapper objectMapper;
    private final WeatherTracker weatherTracker;

    @Autowired
    public SpringWeatherConsumer(ObjectMapper objectMapper, WeatherTracker weatherTracker) {
        this.objectMapper = objectMapper;
        this.weatherTracker = weatherTracker;
    }

    //@KafkaListener(topics = "${topic.name}")
    public void consumeMessage(String message) {
        try{
            var weather = objectMapper.readValue(message, WeatherDto.class);
            weatherTracker.updateStatistics(weather);
            log.info("message consumed {}", message);
            log.info(weatherTracker.getWeatherExtremesReport());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
