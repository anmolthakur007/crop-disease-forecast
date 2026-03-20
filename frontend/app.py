import streamlit as st
import requests
import pandas as pd

st.set_page_config(page_title="Crop Sentinel", layout="centered")

st.title("🌱 Crop Sentinel: Hyperlocal Disease Forecast")
st.write("Upload a photo of your crop and provide your location to get a 3-day outbreak risk forecast.")

# Create two columns
col1, col2 = st.columns(2)

with col1:
    uploaded_file = st.file_uploader("Upload Leaf Image", type=["jpg", "jpeg", "png"])
    
with col2:
    st.write("Farm Location Coordinates")
    lat = st.number_input("Latitude", value=18.5204) # Default to Pune, India
    lon = st.number_input("Longitude", value=73.8567)

# Display a simple map
if lat and lon:
    st.map(pd.DataFrame({'lat': [lat], 'lon': [lon]}))

if st.button("Calculate Outbreak Risk") and uploaded_file is not None:
    with st.spinner("Analyzing image and fetching weather forecast..."):
        
        # Send data to your FastAPI backend
        files = {"file": (uploaded_file.name, uploaded_file.getvalue(), uploaded_file.type)}
        data = {"lat": lat, "lon": lon}
        
        # Note: This assumes your FastAPI server is running locally on port 8000
        response = requests.post("http://localhost:8000/predict_risk/", files=files, data=data)
        
        if response.status_code == 200:
            result = response.json()
            
            st.divider()
            st.subheader("Forecast Results")
            
            # Display risk metrics cleanly
            risk = result['final_outbreak_risk']
            if risk > 75:
                st.error(f"🔴 High Outbreak Risk: {risk}%")
            elif risk > 40:
                st.warning(f"🟡 Moderate Risk (Monitor Closely): {risk}%")
            else:
                st.success(f"🟢 Low Risk: {risk}%")
                
            st.write(f"**Visual Detection Confidence:** {result['visual_confidence']}%")
            st.write(f"**3-Day Forecast:** {result['forecasted_temp']}°C | {result['forecasted_humidity']}% Humidity")
            
        else:
            st.error("Error connecting to the inference engine.")