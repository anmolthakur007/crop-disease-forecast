package com.cropsentinel.backend_api.dto;

// This class MUST match the JSON keys coming from your Python FastAPI
public class ForecastResponse {
    private Double visual_confidence;
    private String predicted_condition;
    private Double environmental_multiplier;
    private Double final_outbreak_risk;
    private Double forecasted_temp;
    private Double forecasted_humidity;

    // Getters and Setters (so Java can fill this object with JSON data)
    public Double getVisual_confidence() { return visual_confidence; }
    public void setVisual_confidence(Double visual_confidence) { this.visual_confidence = visual_confidence; }

    public String getPredicted_condition() { return predicted_condition; }
    public void setPredicted_condition(String predicted_condition) { this.predicted_condition = predicted_condition; }

    public Double getEnvironmental_multiplier() { return environmental_multiplier; }
    public void setEnvironmental_multiplier(Double environmental_multiplier) { this.environmental_multiplier = environmental_multiplier; }

    public Double getFinal_outbreak_risk() { return final_outbreak_risk; }
    public void setFinal_outbreak_risk(Double final_outbreak_risk) { this.final_outbreak_risk = final_outbreak_risk; }

    public Double getForecasted_temp() { return forecasted_temp; }
    public void setForecasted_temp(Double forecasted_temp) { this.forecasted_temp = forecasted_temp; }

    public Double getForecasted_humidity() { return forecasted_humidity; }
    public void setForecasted_humidity(Double forecasted_humidity) { this.forecasted_humidity = forecasted_humidity; }
}