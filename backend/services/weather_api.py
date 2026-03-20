import requests
import pandas as pd

def get_weather_risk_factors(lat: float, lon: float, days: int = 3):
    """Fetches weather and returns average temp and humidity for the forecast period."""
    url = "https://api.open-meteo.com/v1/forecast"
    params = {
        "latitude": lat,
        "longitude": lon,
        "hourly": ["temperature_2m", "relative_humidity_2m"],
        "forecast_days": days
    }
    
    response = requests.get(url, params=params)
    if response.status_code != 200:
        return {"error": "Weather API failed"}
        
    data = response.json()
    df = pd.DataFrame({
        "temp": data['hourly']['temperature_2m'],
        "humidity": data['hourly']['relative_humidity_2m']
    })
    
    # Return the averages for the forecasted days
    return {
        "avg_temp": df['temp'].mean(),
        "avg_humidity": df['humidity'].mean()
    }