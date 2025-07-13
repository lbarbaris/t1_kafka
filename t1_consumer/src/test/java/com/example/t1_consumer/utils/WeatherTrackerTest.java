package com.example.t1_consumer.utils;

import com.example.t1_consumer.dto.WeatherDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WeatherTrackerTest {

    private WeatherTracker weatherTracker;

    @BeforeEach
    void setUp() {
        weatherTracker = new WeatherTracker();
    }

    @Test
    void testUpdateStatisticsWithNewCity() {
        WeatherDto weather = new WeatherDto(
                LocalDateTime.now(),
                "Berlin",
                25.3f,
                "Sunny"
        );

        weatherTracker.updateStatistics(weather);

        assertEquals(25.3f, weatherTracker.getMinTemperatures().get("Berlin"));
        assertEquals(25.3f, weatherTracker.getMaxTemperatures().get("Berlin"));
        assertEquals(1, weatherTracker.getSunnyDaysCount().get("Berlin"));
    }

    @Test
    void testUpdateStatisticsWithExistingCity() {
        weatherTracker.updateStatistics(new WeatherDto(
                LocalDateTime.now(),
                "Paris",
                20.0f,
                "Sunny"
        ));

        weatherTracker.updateStatistics(new WeatherDto(
                LocalDateTime.now(),
                "Paris",
                15.0f,
                "Rainy"
        ));

        weatherTracker.updateStatistics(new WeatherDto(
                LocalDateTime.now(),
                "Paris",
                30.0f,
                "Sunny"
        ));

        assertEquals(15.0f, weatherTracker.getMinTemperatures().get("Paris"));
        assertEquals(30.0f, weatherTracker.getMaxTemperatures().get("Paris"));
        assertEquals(2, weatherTracker.getSunnyDaysCount().get("Paris"));
    }

    @Test
    void testUpdateStatisticsWithNonSunnyWeather() {
        weatherTracker.updateStatistics(new WeatherDto(
                LocalDateTime.now(),
                "London",
                18.0f,
                "Rainy"
        ));

        assertNull(weatherTracker.getSunnyDaysCount().get("London"));
    }

    @Test
    void testGetWeatherExtremesReportWithData() {
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "Berlin", 25.0f, "Sunny"));
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "Berlin", 30.0f, "Cloudy"));
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "Paris", 15.0f, "Sunny"));
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "Paris", 10.0f, "Sunny"));
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "London", 20.0f, "Sunny"));
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "London", 22.0f, "Sunny"));

        String report = weatherTracker.getWeatherExtremesReport();

        assertTrue(report.contains("Most hottest city: Berlin (30.0°C)"));
        assertTrue(report.contains("Most coldest city: Paris (10.0°C)"));
        assertTrue(report.contains("Most sunny city: London (2 sun days)"));
    }

    @Test
    void testGetWeatherExtremesReportWithNoData() {
        String report = weatherTracker.getWeatherExtremesReport();

        assertTrue(report.contains("Most hottest city doesn't exist"));
        assertTrue(report.contains("Most coldest city doesn't exist"));
        assertTrue(report.contains("Most sunny city doesn't exist"));
    }

    @Test
    void testGetCityWithHighestTemperature() {
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "Berlin", 25.0f, "Sunny"));
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "Paris", 30.0f, "Sunny"));

        assertEquals("Paris", weatherTracker.getCityWithHighestTemperature());
    }

    @Test
    void testGetCityWithLowestTemperature() {
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "Berlin", 25.0f, "Sunny"));
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "Paris", 15.0f, "Sunny"));

        assertEquals("Paris", weatherTracker.getCityWithLowestTemperature());
    }

    @Test
    void testGetSunniestCity() {
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "Berlin", 25.0f, "Sunny"));
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "Paris", 15.0f, "Sunny"));
        weatherTracker.updateStatistics(new WeatherDto(LocalDateTime.now(), "Paris", 20.0f, "Sunny"));

        assertEquals("Paris", weatherTracker.getSunniestCity());
    }
}
