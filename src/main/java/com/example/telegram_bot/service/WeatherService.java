package com.example.telegram_bot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
public class WeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getWeather(String cityName) {
        try {
            // Thiết lập Header User-Agent để máy chủ Open-Meteo không chặn kết nối từ Java
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // ================= BƯỚC 1: LẤY TOẠ ĐỘ THÀNH PHỐ TỪ OPEN-METEO =================
            String geoUrl = UriComponentsBuilder
                    .fromUriString("https://geocoding-api.open-meteo.com/v1/search")
                    .queryParam("name", cityName.trim())
                    .queryParam("count", 1)
                    .queryParam("language", "vi")
                    .queryParam("format", "json")
                    .toUriString();

            ResponseEntity<String> geoResponse = restTemplate.exchange(geoUrl, HttpMethod.GET, entity, String.class);
            if (geoResponse.getBody() == null) {
                return "❌ Không nhận được dữ liệu từ máy chủ thời tiết.";
            }

            JsonNode geoBody = objectMapper.readTree(geoResponse.getBody());

            if (!geoBody.has("results") || geoBody.get("results").isEmpty()) {
                return "❌ Không tìm thấy thông tin cho địa điểm: *" + cityName + "*. Vui lòng kiểm tra lại tên thành phố!";
            }

            JsonNode location = geoBody.get("results").get(0);
            double latitude = location.get("latitude").asDouble();
            double longitude = location.get("longitude").asDouble();
            String foundCity = location.has("name") ? location.get("name").asText() : cityName;
            String country = location.has("country") ? location.get("country").asText() : "";

            // ================= BƯỚC 2: LẤY THỜI TIẾT THEO TOẠ ĐỘ =================
            String weatherUrl = UriComponentsBuilder
                    .fromUriString("https://api.open-meteo.com/v1/forecast")
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("current_weather", true)
                    .toUriString();

            ResponseEntity<String> weatherResponse = restTemplate.exchange(weatherUrl, HttpMethod.GET, entity, String.class);
            if (weatherResponse.getBody() == null) {
                return "⚠️ Hiện tại không lấy được dữ liệu thời tiết cho khu vực này.";
            }

            JsonNode weatherBody = objectMapper.readTree(weatherResponse.getBody());

            if (!weatherBody.has("current_weather")) {
                return "⚠️ Hiện tại không lấy được dữ liệu thời tiết cho khu vực này.";
            }

            JsonNode currentWeather = weatherBody.get("current_weather");
            double temp = currentWeather.get("temperature").asDouble();
            double windspeed = currentWeather.get("windspeed").asDouble();
            int weatherCode = currentWeather.get("weathercode").asInt();

            String weatherDescription = decodeWeatherCode(weatherCode);

            return String.format(
                    """
                            🌤 *THÔNG TIN THỜI TIẾT (OPEN-METEO)*
                            📍 *Địa điểm:* %s %s
                            🌡️ *Nhiệt độ:* %.1f°C
                            🌥️ *Trạng thái:* %s
                            💨 *Tốc độ gió:* %.1f km/h""",
                    foundCity,
                    country.isBlank() ? "" : "(" + country + ")",
                    temp,
                    weatherDescription,
                    windspeed
            );

        } catch (Exception e) {
            log.error("Lỗi khi gọi Open-Meteo cho {}: {}", cityName, e.getMessage(), e);
            return "❌ Đã có lỗi xảy ra khi lấy dữ liệu thời tiết: " + e.getMessage();
        }
    }

    private String decodeWeatherCode(int code) {
        return switch (code) {
            case 0 -> "Trời quang đãng, nắng đẹp ☀️";
            case 1, 2, 3 -> "Trời nhiều mây rải rác ⛅";
            case 45, 48 -> "Có sương mù 🌫️";
            case 51, 53, 55 -> "Mưa phùn nhỏ 🌦️";
            case 61, 63, 65 -> "Mưa rào 🌧️";
            case 80, 81, 82 -> "Mưa rào nặng hạt từng cơn ⛈️";
            case 95, 96, 99 -> "Có dông bão sấm chớp ⚡";
            default -> "Thời tiết ổn định 🌤️";
        };
    }
}
