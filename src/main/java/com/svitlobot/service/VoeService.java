package com.svitlobot.service;

import com.svitlobot.PowerScheduleParser;
import com.svitlobot.dto.DaySchedule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class VoeService {

    private final PowerScheduleParser powerScheduleParser;

    private static final String API_URL = "https://voe.com.ua/disconnection/detailed";


    public DaySchedule getTodaySchedule() {
        MonthDay monthDay = MonthDay.now();
        List<DaySchedule> daySchedules = fetchDisconnectionData();
        return daySchedules.stream().filter(d -> d.getDate().equals(monthDay)).findFirst().orElse(null);
    }

    public DaySchedule getTomorrowSchedule() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        MonthDay monthDay = MonthDay.from(tomorrow);
        List<DaySchedule> daySchedules = fetchDisconnectionData();
        return daySchedules.stream().filter(d -> d.getDate().equals(monthDay)).findFirst().orElse(null);
    }

    public List<DaySchedule> fetchDisconnectionData() {
        Map<String, String> formData = new HashMap<>();
        formData.put("search_type", "0");
        formData.put("city", "м.. Вінниця (Вінницька Область/М.Вінниця)");
        formData.put("city_id", "510100000");
        formData.put("street", "провулок Івана Миколайчука");
        formData.put("street_id", "1715");
        formData.put("house", "6");
        formData.put("house_id", "32300");
        formData.put("house_id", "");  // Note: duplicate parameter as in original curl
        formData.put("form_build_id", "form-HUzxjQSxAMxQ7YCqc3gHXuNjT5_TThaMVOk58wGPPVY");
        formData.put("form_id", "disconnection_detailed_search_form");
        formData.put("_triggering_element_name", "search");
        formData.put("_triggering_element_value", "Показати");
        formData.put("_drupal_ajax", "1");
        formData.put("ajax_page_state[theme]", "personal");
        formData.put("ajax_page_state[theme_token]", "");
        formData.put("ajax_page_state[libraries]", "eJyFUVuOwzAIvJAbH8kiDnVpiPEae1Xv6dd5bfpRab9ghhGDGPCeJozFUbyLG1n8bOHkNuieauB_lZbG2JVPeLm75EXtAhSNZ1BtdgTFs19QFULXeslop1wT8AC1iJclMRbcBxQL5thHz6-KuQ3r0n0SewdMP11Ytchi92L2VS5Kae40sZ9Ig6_CFOfT_IDmTtw9rYdUSKJJkCFkSI9zy3AxQ42pjkz6wMkkzCr9VBtYRuDb-guK4eK1kJ_bhYuEwLh9841sCV2Ebwqw2rsaqZgkzH_uvb-th6pRhOwfRxLvYDhyOE7YonCYs2S1HzijTQsuWz6_GLDO_w");

        String requestBody = formData.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Cookie", "f_city=%D0%BC..%20%D0%92%D1%96%D0%BD%D0%BD%D0%B8%D1%86%D1%8F%20%28%D0%92%D1%96%D0%BD%D0%BD%D0%B8%D1%86%D1%8C%D0%BA%D0%B0%20%D0%9E%D0%B1%D0%BB%D0%B0%D1%81%D1%82%D1%8C%2F%D0%9C.%D0%92%D1%96%D0%BD%D0%BD%D0%B8%D1%86%D1%8F%29; f_city_id=510100000; f_house=6; f_search_type=0; f_street=%D0%BF%D1%80%D0%BE%D0%B2%D1%83%D0%BB%D0%BE%D0%BA%20%D0%86%D0%B2%D0%B0%D0%BD%D0%B0%20%D0%9C%D0%B8%D0%BA%D0%BE%D0%BB%D0%B0%D0%B9%D1%87%D1%83%D0%BA%D0%B0; f_street_id=1715")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.error("Failed to fetch disconnection data", e);
        }
        String body = response.body();

        if (body == null || body.isEmpty()) {
            log.error("Failed to fetch disconnection data: empty response body");
            return List.of();
        }

        return powerScheduleParser.parse(body);
    }
}