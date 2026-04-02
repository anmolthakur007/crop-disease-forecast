package com.cropsentinel.backend_api.service;

import com.cropsentinel.backend_api.model.ForecastHistory;
import com.cropsentinel.backend_api.repository.ForecastRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastService {

    @Autowired
    private ForecastRepository forecastRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PYTHON_URL = "http://localhost:8000/predict_risk/";

    public Map<String, Object> getForecast(MultipartFile file, double lat, double lon) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() { return file.getOriginalFilename(); }
        });
        body.add("lat", lat);
        body.add("lon", lon);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(PYTHON_URL, request, Map.class);
        Map<String, Object> result = response.getBody();

        ForecastHistory record = new ForecastHistory();
        record.setPredictedCondition((String) result.get("predicted_condition"));
        record.setVisualConfidence(((Number) result.get("visual_confidence")).doubleValue());
        record.setFinalOutbreakRisk(((Number) result.get("final_outbreak_risk")).doubleValue());
        record.setForecastedTemp(((Number) result.get("forecasted_temp")).doubleValue());
        record.setForecastedHumidity(((Number) result.get("forecasted_humidity")).doubleValue());
        record.setLatitude(lat);
        record.setLongitude(lon);
        forecastRepository.save(record);

        return result;
    }

    public List<ForecastHistory> getHistory() {
        return forecastRepository.findAllByOrderByCreatedAtDesc();
    }

    public Map<String, Object> getStats() {
        List<ForecastHistory> all = forecastRepository.findAll();
        Map<String, Object> stats = new HashMap<>();

        if (all.isEmpty()) {
            stats.put("totalForecasts", 0);
            stats.put("averageRisk", 0);
            stats.put("highestRisk", 0);
            stats.put("mostCommonDisease", "No data yet");
            return stats;
        }

        long total = all.size();
        double avgRisk = all.stream()
                .mapToDouble(ForecastHistory::getFinalOutbreakRisk)
                .average().orElse(0);
        double highestRisk = all.stream()
                .mapToDouble(ForecastHistory::getFinalOutbreakRisk)
                .max().orElse(0);
        String mostCommon = all.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        ForecastHistory::getPredictedCondition,
                        java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        stats.put("totalForecasts", total);
        stats.put("averageRisk", Math.round(avgRisk * 10.0) / 10.0);
        stats.put("highestRisk", Math.round(highestRisk * 10.0) / 10.0);
        stats.put("mostCommonDisease", mostCommon.replace("___", " → "));
        return stats;
    }
}