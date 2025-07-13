package com.example.t1_consumer.utils;

import com.example.t1_consumer.dto.WeatherDto;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Component
@Getter
public class WeatherTracker {
    private final Map<String, Float> minTemperatures = new HashMap<>();

    private final Map<String, Float> maxTemperatures = new HashMap<>();

    private final Map<String, Integer> sunnyDaysCount = new HashMap<>();

    public void updateStatistics(WeatherDto weatherDto) {

        var city = weatherDto.getCity();
        var currentTemp = weatherDto.getTemperature();
        var isSunny = weatherDto.getState().equals("Sunny");

        minTemperatures.merge(city, currentTemp, Math::min);

        maxTemperatures.merge(city, currentTemp, Math::max);

        if (isSunny) {
            sunnyDaysCount.merge(city, 1, Integer::sum);
        }
    }

    public String getCityWithHighestTemperature() {
        return maxTemperatures.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public String getCityWithLowestTemperature() {
        return minTemperatures.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public String getSunniestCity() {
        return sunnyDaysCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public String getWeatherExtremesReport() {
        var report = new StringBuilder("\nWeather statistics:\n");

        var hottestCity = getCityWithHighestTemperature();
        if (nonNull(hottestCity)) {
            var maxTemp = maxTemperatures.get(hottestCity);
            report.append("Most hottest city: ")
                    .append(hottestCity)
                    .append(" (")
                    .append(maxTemp)
                    .append("°C)\n");
        } else {
            report.append("Most hottest city doesn't exist\n");
        }

        var coldestCity = getCityWithLowestTemperature();
        if (nonNull(coldestCity)) {
            var minTemp = minTemperatures.get(coldestCity);
            report.append("Most coldest city: ")
                    .append(coldestCity)
                    .append(" (")
                    .append(minTemp)
                    .append("°C)\n");
        } else {
            report.append("Most coldest city doesn't exist\n");
        }

        var sunniestCity = getSunniestCity();
        if (nonNull(sunniestCity)) {
            var sunnyDays = sunnyDaysCount.get(sunniestCity);
            report.append("Most sunny city: ")
                    .append(sunniestCity)
                    .append(" (")
                    .append(sunnyDays)
                    .append(" sun days)");
        } else {
            report.append("Most sunny city doesn't exist\n");
        }

        return report.toString();
    }
}