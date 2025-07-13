package com.example.t1_producer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Random;

@Builder
@Value
@AllArgsConstructor
public class WeatherDto {
    LocalDateTime date;
    String city;
    Float temperature;
    String state;

    public static WeatherDto buildRandomDto(){
        var random = new Random();
        var cities = new String[] {
                "Moscow", "Saint-Petersburg", "Novosibirsk", "Ekaterinburg", "Kazan",
                "Nizhny Novgorod", "Chelyabinsk", "Samara", "Omsk", "Rostov-on-Don",
                "Ufa", "Krasnoyarsk", "Perm", "Voronezh", "Volgograd"
        };

        var states = new String[] {
                "Sunny", "Rainy", "Windy"
        };

        var randomDate = LocalDateTime.now()
                .minusDays(random.nextInt(30))
                .minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60));

        var randomTemperature = -20 + random.nextFloat() * 50;

        return WeatherDto.builder()
                .date(randomDate)
                .temperature(randomTemperature)
                .city(cities[random.nextInt(cities.length)])
                .state(states[random.nextInt(states.length)])
                .build();
    }
}
