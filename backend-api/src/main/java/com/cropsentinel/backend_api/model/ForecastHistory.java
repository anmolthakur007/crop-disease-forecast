package com.cropsentinel.backend_api.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forecast_history")
public class ForecastHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "predicted_condition")
    private String predictedCondition;

    @Column(name = "visual_confidence")
    private Double visualConfidence;

    @Column(name = "final_outbreak_risk")
    private Double finalOutbreakRisk;

    @Column(name = "forecasted_temp")
    private Double forecastedTemp;

    @Column(name = "forecasted_humidity")
    private Double forecastedHumidity;

    private Double latitude;
    private Double longitude;

    @Column(name = "created_at" , updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPredictedCondition() { return predictedCondition; }
    public void setPredictedCondition(String predictedCondition) { this.predictedCondition = predictedCondition; }

    public Double getVisualConfidence() { return visualConfidence; }
    public void setVisualConfidence(Double visualConfidence) { this.visualConfidence = visualConfidence; }

    public Double getFinalOutbreakRisk() { return finalOutbreakRisk; }
    public void setFinalOutbreakRisk(Double finalOutbreakRisk) { this.finalOutbreakRisk = finalOutbreakRisk; }

    public Double getForecastedTemp() { return forecastedTemp; }
    public void setForecastedTemp(Double forecastedTemp) { this.forecastedTemp = forecastedTemp; }

    public Double getForecastedHumidity() { return forecastedHumidity; }
    public void setForecastedHumidity(Double forecastedHumidity) { this.forecastedHumidity = forecastedHumidity; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

