# Crop Sentinel: Hyperlocal Disease Forecast

## Overview
Crop Sentinel is an AI-powered application that integrates computer vision and weather data to forecast crop disease outbreaks.

## Features
- **Visual Detection:** MobileNetV2 model trained on the PlantVillage dataset (38 classes).
- **Environmental Reference:** Fetches hyperlocal weather forecast (temp, humidity) via Open-Meteo API.
- **Fusion Logic:** Combines visual confidence with environmental risk factors to predict outbreak probability.

## Setup

1. **Install Dependencies:**
   ```bash
   pip install -r requirements.txt
   ```

2. **Run Backend:**
   ```bash
   cd backend
   python -m uvicorn main:app --reload
   ```

3. **Run Frontend:**
   ```bash
   streamlit run frontend/app.py
   ```

## Model Training
The model is trained using `notebooks/01_model_training.ipynb`.
**Note:** The trained model file (`.keras`) is excluded from the repo due to size. You must run the notebook to generate it locallly.

## Dataset
Uses the [PlantVillage Dataset](https://www.kaggle.com/datasets/abdallahalidev/plantvillage-dataset).
