package com.example.t1_consumer.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class WeatherDto {
    LocalDateTime date;
    String city;
    Float temperature;
    String state;
}
