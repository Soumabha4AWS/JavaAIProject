package com.weather.ai.controller;

import com.weather.ai.service.WeatherAgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController {

    private final WeatherAgentService weatherAgentService;

    public WeatherController(WeatherAgentService weatherAgentService) {
        this.weatherAgentService = weatherAgentService;
    }

    @GetMapping("/")
    public String dashboard() {
        return "Welcome to Weather Report Dashboard page";
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam String location) {
        String userPrompt = "What is the weather of " + location;
        return weatherAgentService.runWeatherAgent(userPrompt);
    }

}
