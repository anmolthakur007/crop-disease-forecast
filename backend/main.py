from fastapi import FastAPI, File, UploadFile, Form
from services.fusion_engine import process_image_and_weather

app = FastAPI(title="Crop Sentinel API")

@app.post("/predict_risk/")
async def predict_risk(
    lat: float = Form(...),
    lon: float = Form(...),
    file: UploadFile = File(...)
):
    # Read the image uploaded by the farmer
    image_bytes = await file.read()
    
    # Run the fusion engine
    result = process_image_and_weather(image_bytes, lat, lon)
    
    return result