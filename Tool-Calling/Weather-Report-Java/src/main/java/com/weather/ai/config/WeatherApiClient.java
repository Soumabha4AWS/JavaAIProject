package com.weather.ai.config;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class WeatherApiClient {

    private static final String API_HOST = "weather-api167.p.rapidapi.com";
    private static final String API_KEY = "1a58725e79msh9fbb6b9a7daf4e3p169acejsnae4454a41e6b";
    private static final String API_URL = "https://weather-api167.p.rapidapi.com/api/weather/forecast";

    public static String getCurrentWeather(String location) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String encodedLocation = URLEncoder.encode(location + ",GB", StandardCharsets.UTF_8);
        String queryUrl = String.format("%s?place=%s&cnt=3&units=standard&type=three_hour&mode=json&lang=en",
                API_URL, encodedLocation);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(queryUrl))
                .header("x-rapidapi-key", API_KEY)
                .header("x-rapidapi-host", API_HOST)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return response.body();
    }

}
