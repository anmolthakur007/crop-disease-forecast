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
}