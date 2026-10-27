package com.weather.ai.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import java.util.function.Function;

@Configuration
public class WeatherToolsConfig {

    @Bean
    @Description("Get current weather in a given location")
    public Function<WeatherRequest, String> getCurrentWeather() {
        return request -> {
            System.out.println("-> TOOL ACTIVATED: get_current_weather for " + request.location() + "...");
            try {
                return WeatherApiClient.getCurrentWeather(request.location());
            } catch (Exception e) {
                return "Error fetching weather: " + e.getMessage();
            }
        };
    }

}
