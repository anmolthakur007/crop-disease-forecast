"""
Mock API for crop disease prediction
This is a lightweight version that doesn't require TensorFlow
Used when the full ML backend is not available due to environment issues
"""

from fastapi import FastAPI, UploadFile, File, Form
from fastapi.responses import JSONResponse
import uvicorn
from PIL import Image
import io
import random
import requests
from datetime import datetime

app = FastAPI(title="Crop Sentinel - Mock Prediction API")

# Supported diseases
DISEASES = [
    "Healthy___Health",
    "Bacterial_Spot___Bacterial_Spot",
    "Early_Blight___Early_Blight",
    "Late_Blight___Late_Blight",
    "Leaf_Mold___Leaf_Mold",
    "Septoria_Leaf_Spot___Septoria_Leaf_Spot",
    "Powdery_Mildew___Powdery_Mildew",
    "Target_Spot___Target_Spot",
]

@app.post("/predict_risk/")
async def predict_risk(
    file: UploadFile = File(...),
    lat: float = Form(...),
    lon: float = Form(...)
):
    """
    Mock prediction endpoint that simulates disease risk assessment.
    Returns synthetic prediction based on image size and location.
    """
    try:
        # Read the uploaded image
        image_bytes = await file.read()
        image = Image.open(io.BytesIO(image_bytes))
        
        # Get image properties for synthetic analysis
        width, height = image.size
        
        # Generate mock predictions based on image characteristics
        # This simulates different risk levels based on image properties
        risk_score = random.uniform(20, 85)
        
        # For images with certain dimensions, bias towards specific diseases
        if width > 500 or height > 500:
            risk_score = max(risk_score, 60)  # Larger images = higher risk
        
        if width < 300 and height < 300:
            risk_score = min(risk_score, 45)  # Small images = lower risk
        
        # Select random disease
        predicted_disease = random.choice(DISEASES)
        
        # Get weather data for the location
        weather_data = get_weather_data(lat, lon)
        
        # Prepare response
        response = {
            "predicted_condition": predicted_disease,
            "visual_confidence": round(random.uniform(70, 99), 2),
            "final_outbreak_risk": round(risk_score, 2),
            "forecasted_temp": weather_data.get("temp", 25.5),
            "forecasted_humidity": weather_data.get("humidity", 65.0),
            "model_version": "mock-v1.0",
            "timestamp": datetime.now().isoformat()
        }
        
        return JSONResponse(content=response)
    
    except Exception as e:
        return JSONResponse(
            status_code=400,
            content={"error": str(e)}
        )

def get_weather_data(lat: float, lon: float) -> dict:
    """
    Fetch weather data for the given coordinates using OpenWeatherMap or similar.
    Falls back to mock data if API is unavailable.
    """
    try:
        # Using a simple weather API (open-meteo.com doesn't require auth)
        response = requests.get(
            f"https://api.open-meteo.com/v1/forecast",
            params={
                "latitude": lat,
                "longitude": lon,
                "current": "temperature_2m,relative_humidity_2m"
            },
            timeout=5
        )
        
        if response.status_code == 200:
            data = response.json()["current"]
            return {
                "temp": data.get("temperature_2m", 25.5),
                "humidity": data.get("relative_humidity_2m", 65.0)
            }
    except:
        pass
    
    # Fallback to mock weather data
    return {
        "temp": round(random.uniform(15, 32), 1),
        "humidity": round(random.uniform(45, 85), 1)
    }

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "Crop Sentinel Mock API"}

if __name__ == "__main__":
    print("🚀 Starting Crop Sentinel Mock Prediction API on port 8000...")
    print("📝 Note: This is a mock API for development purposes")
    print("🔗 Endpoint: http://localhost:8000/predict_risk/")
    uvicorn.run(app, host="0.0.0.0", port=8000)
