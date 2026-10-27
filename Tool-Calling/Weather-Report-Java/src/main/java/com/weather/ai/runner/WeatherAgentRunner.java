package com.weather.ai.runner;

import com.weather.ai.service.WeatherAgentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class WeatherAgentRunner implements CommandLineRunner {

    private final WeatherAgentService weatherAgentService;

    public WeatherAgentRunner(WeatherAgentService weatherAgentService) {
        this.weatherAgentService = weatherAgentService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Disabled for web application mode
        // Uncomment below to enable CLI mode
        /*
        String userPrompt = "London";
        System.out.println("\n" + "=".repeat(80) + "\n");
        weatherAgentService.runWeatherAgent("What is the weather of " + userPrompt);
        System.out.println("\n" + "=".repeat(80) + "\n");
        */
    }

}
