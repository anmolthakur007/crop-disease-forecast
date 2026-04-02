package com.cropsentinel.backend_api.controller;

import com.cropsentinel.backend_api.model.ForecastHistory;
import com.cropsentinel.backend_api.service.ForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ForecastController {

    @Autowired
    private ForecastService forecastService;

    @PostMapping("/forecast")
    public Map<String, Object> forecast(
            @RequestParam("file") MultipartFile file,
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon
    ) throws Exception {
        return forecastService.getForecast(file, lat, lon);
    }

    @GetMapping("/history")
    public List<ForecastHistory> history() {
        return forecastService.getHistory();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return forecastService.getStats();
    }
}