import keras
import tensorflow as tf
import numpy as np
from PIL import Image
import io
import os
from .weather_api import get_weather_risk_factors

# Class names from PlantVillage dataset (alphabetical order)
CLASS_NAMES = [
    'Apple___Apple_scab', 'Apple___Black_rot', 'Apple___Cedar_apple_rust', 'Apple___healthy', 
    'Blueberry___healthy', 'Cherry_(including_sour)___Powdery_mildew', 
    'Cherry_(including_sour)___healthy', 'Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot', 
    'Corn_(maize)___Common_rust_', 'Corn_(maize)___Northern_Leaf_Blight', 'Corn_(maize)___healthy', 
    'Grape___Black_rot', 'Grape___Esca_(Black_Measles)', 'Grape___Leaf_blight_(Isariopsis_Leaf_Spot)', 
    'Grape___healthy', 'Orange___Haunglongbing_(Citrus_greening)', 'Peach___Bacterial_spot', 
    'Peach___healthy', 'Pepper,_bell___Bacterial_spot', 'Pepper,_bell___healthy', 
    'Potato___Early_blight', 'Potato___Late_blight', 'Potato___healthy', 
    'Raspberry___healthy', 'Soybean___healthy', 'Squash___Powdery_mildew', 
    'Strawberry___Leaf_scorch', 'Strawberry___healthy', 'Tomato___Bacterial_spot', 
    'Tomato___Early_blight', 'Tomato___Late_blight', 'Tomato___Leaf_Mold', 
    'Tomato___Septoria_leaf_spot', 'Tomato___Spider_mites Two-spotted_spider_mite', 
    'Tomato___Target_Spot', 'Tomato___Tomato_Yellow_Leaf_Curl_Virus', 'Tomato___Tomato_mosaic_virus', 
    'Tomato___healthy'
]

# Load your trained model
try:
    BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    model_path = os.path.join(BASE_DIR, "models", "crop_sentinel_mobilenetv2.keras")
    model = keras.models.load_model(model_path)
    print(f"Model loaded successfully from {model_path}")
except Exception as e:
    model = None
    print(f"Critical error loading model: {e}")

def process_image_and_weather(image_bytes, lat, lon):
    # 1. Image Processing (The "Eyes")
    image = Image.open(io.BytesIO(image_bytes)).resize((224, 224))
    img_array = keras.utils.img_to_array(image)
    img_array = np.expand_dims(img_array, 0) # Create a batch
    
    if model:
        predictions = model.predict(img_array)
        
        # Get the highest probability class
        predicted_index = np.argmax(predictions[0])
        confidence = float(predictions[0][predicted_index])
        predicted_class = CLASS_NAMES[predicted_index]
        
        print(f"Predicted Class: {predicted_class}, Confidence: {confidence}")
        
        # If the predicted class is "healthy", the disease risk is low (e.g., 0)
        # Otherwise, the risk is the model's confidence in the disease detection
        if "healthy" in predicted_class.lower():
            disease_probability = 0.05 # Low risk for healthy plants
        else:
            disease_probability = confidence
            
    else:
        disease_probability = 0.65 # Dummy value
        predicted_class = "Unknown"
        
    # 2. Weather Processing (The "Environment")
    weather = get_weather_risk_factors(lat, lon)
    
    # 3. Data Fusion Logic (The "Brain")
    # Fungal diseases love high humidity (>80%) and moderate temps (20-30C)
    env_multiplier = 1.0
    if weather['avg_humidity'] > 80 and (20 <= weather['avg_temp'] <= 30):
        env_multiplier = 1.4 
    elif weather['avg_humidity'] < 50:
        env_multiplier = 0.5 
        
    # Calculate final risk score
    final_risk_score = min((disease_probability * env_multiplier) * 100, 100)
    
    return {
        "visual_confidence": round(disease_probability * 100, 2),
        "predicted_condition": predicted_class,
        "environmental_multiplier": env_multiplier,
        "final_outbreak_risk": round(final_risk_score, 2),
        "forecasted_temp": round(weather['avg_temp'], 1),
        "forecasted_humidity": round(weather['avg_humidity'], 1)
    }