package com.example.t1_producer.producer;

import com.example.t1_producer.dto.WeatherDto;

public interface WeatherProducerInterface {

    void sendMessage(WeatherDto weatherDto);
}
