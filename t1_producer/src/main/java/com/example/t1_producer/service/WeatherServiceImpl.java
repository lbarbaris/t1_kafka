package com.example.t1_producer.service;

import com.example.t1_producer.dto.WeatherDto;
import com.example.t1_producer.producer.WeatherProducerInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    private final WeatherProducerInterface weatherProducer;

    @Autowired
    public WeatherServiceImpl(WeatherProducerInterface weatherProducer) {
        this.weatherProducer = weatherProducer;
    }

    @Override
    @Scheduled(fixedRate = 5000)
    public void sendRandomWeather() {
        weatherProducer.sendMessage(WeatherDto.buildRandomDto());
    }
}
