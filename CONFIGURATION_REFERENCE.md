# Configuration Reference Guide

**Last Updated:** April 8, 2026

---

## 🔧 Critical Configuration Changes

### File: `backend-api/src/main/resources/application.properties`

#### Original (Broken) Configuration:
```properties
spring.application.name=backend-api
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/cropsentinel?serverTimezone=UTC
spring.datasource.username=postgres
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
```

**Problems:**
- ❌ `serverTimezone=UTC` - This is a **MySQL parameter**, not PostgreSQL!
- ❌ PostgreSQL doesn't accept this and ignores it
- ❌ System timezone defaults to "Asia/Calcutta" (deprecated, not recognized by PostgreSQL)
- ❌ Results in: `FATAL: invalid value for parameter "TimeZone": "Asia/Calcutta"`

#### Fixed Configuration:
```properties
spring.application.name=backend-api
server.port=8080

# PostgreSQL connection - NO serverTimezone parameter!
spring.datasource.url=jdbc:postgresql://localhost:5432/cropsentinel
spring.datasource.username=postgres
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# Logging
logging.level.org.hibernate.engine.jdbc.spi.SqlExceptionHelper=OFF
```

**Fixes:**
- ✅ Removed MySQL-specific `serverTimezone` parameter
- ✅ Added JVM argument to override system timezone: `-Duser.timezone=UTC`
- ✅ Explicitly set Hibernate timezone to UTC
- ✅ Optimized batch processing settings
- ✅ Disabled noisy Hibernate logs

---

## 🐍 New File: `backend/mock_api.py`

**Why it was created:**
- TensorFlow installation failed on Windows (path length limits)
- Full ML model couldn't be deployed in dev environment
- Need a functional system for demos and testing

**What it does:**
```python
from fastapi import FastAPI
import uvicorn

app = FastAPI(title="Crop Sentinel - Mock Prediction API")

@app.post("/predict_risk/")
async def predict_risk(file, lat, lon):
    """
    Mock prediction endpoint
    Returns synthetic predictions based on:
    - Image dimensions
    - Random disease selection
    - Weather data from open-meteo.com
    - Simulated confidence scores
    """
    # Generate prediction
    risk_score = simulate_risk(image_properties)
    disease = select_random_disease()
    weather = fetch_weather_data(lat, lon)
    
    return {
        "predicted_condition": disease,
        "visual_confidence": confidence_score,
        "final_outbreak_risk": risk_score,
        "forecasted_temp": weather.temp,
        "forecasted_humidity": weather.humidity
    }
```

**Endpoints:**
- `POST /predict_risk/` - Main prediction endpoint
- `GET /openapi.json` - API schema
- `GET /docs` - Interactive API docs (Swagger UI)

**Dependencies:**
```bash
pip install fastapi uvicorn python-multipart requests pillow
```

---

## 🚀 JVM Arguments

### Why `-Duser.timezone=UTC` is Critical

**Problem without it:**
- System timezone on Windows is "Asia/Calcutta"
- PostgreSQL JDBC driver reads system timezone
- PostgreSQL 15 rejects "Asia/Calcutta" as invalid
- Application crashes on startup

**Solution:**
```bash
mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC"
```

**Important Notes:**
- ⚠️ **Quotes are required!** Without quotes, Maven doesn't parse the argument correctly
- ⚠️ Must use `"-Duser.timezone=UTC"` not just `-Duser.timezone=UTC`
- ✅ Tells JVM to use UTC timezone regardless of system settings
- ✅ Spring Boot passes this to all components (Hibernate, database driver)

---

## 📡 Port Configuration

| Service | Port | Protocol | Status Check |
|---------|------|----------|--------------|
| PostgreSQL | 5432 | TCP/JDBC | `Test-NetConnection -ComputerName localhost -Port 5432` |
| Python API | 8000 | HTTP | `curl http://localhost:8000/openapi.json` |
| Spring Boot | 8080 | HTTP/REST | `curl http://localhost:8080/api/history` |

**If port is in use:**
```powershell
# Find what's using port 8080
netstat -ano | findstr :8080

# Kill the process (replace 1234 with actual PID)
taskkill /PID 1234 /F
```

---

## 🔐 Database Configuration

### PostgreSQL Connection Details
```
Host: localhost
Port: 5432
Database: cropsentinel
Username: postgres
Password: password
```

### Hibernate/JPA Settings

| Property | Value | Reason |
|----------|-------|--------|
| `ddl-auto` | `update` | Auto-migrate schema on startup |
| `dialect` | `PostgreSQLDialect` | PostgreSQL-specific SQL generation |
| `jdbc.time_zone` | `UTC` | Standardize timestamps |
| `batch_size` | `20` | Batch inserts for performance |
| `order_inserts` | `true` | Optimize batch insert order |

---

## 🏗️ Architecture Dependencies

```
JavaFX UI
    ↓ (HTTP POST/GET)
Spring Boot Backend (port 8080)
    ├→ PostgreSQL Database (port 5432)
    └→ Python API (port 8000)
        └→ Weather API (open-meteo.com)
```

### Key Classes & Services

**Spring Boot Backend:**
- `ForecastController` - REST endpoints
- `ForecastService` - Business logic, calls Python API
- `ForecastRepository` - Database access
- `DiseaseInfoService` - Disease information

**Python API:**
- `mock_api.py` - FastAPI application
- `predict_risk()` - Main prediction function
- `get_weather_data()` - Fetches real weather data

**JavaFX UI:**
- `App.java` - Entry point
- `PredictController.java` - Main prediction UI
- `HistoryController.java` - History view
- `StatsController.java` - Statistics view

---

## 🧪 Testing the System

### 1. Test PostgreSQL Connection
```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/history"
# Should return HTTP 200 with JSON array
```

### 2. Test Python API
```powershell
Invoke-WebRequest -Uri "http://localhost:8000/openapi.json"
# Should return HTTP 200 with OpenAPI schema
```

### 3. Test Image Upload (via Spring Boot)
```powershell
$filePath = "C:\path\to\image.jpg"
$form = @{
    file = Get-Item $filePath
    lat = "28.7"
    lon = "77.2"
}
Invoke-WebRequest -Uri "http://localhost:8080/api/forecast" -Form $form -Method Post
```

### 4. Test UI Application
- Open application window
- Click "Choose Image"
- Enter location
- Click "Get Forecast"
- Verify prediction appears

---

## 📋 Startup Checklist

Before launching the application:

- [ ] Docker is installed and running
- [ ] PostgreSQL container started: `docker-compose up -d postgres`
- [ ] Python API running: `python backend\mock_api.py`
- [ ] Spring Boot API running: `mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC"`
- [ ] JavaFX UI started: `.\mvnw.cmd javafx:run`
- [ ] Verify port 5432: `Test-NetConnection -ComputerName localhost -Port 5432`
- [ ] Verify port 8000: `Invoke-WebRequest http://localhost:8000/openapi.json`
- [ ] Verify port 8080: `Invoke-WebRequest http://localhost:8080/api/history`
- [ ] UI window visible on screen or check Alt+Tab

---

## 🔄 If You Need to Modify Configuration

### To Change Database
1. Edit `backend-api/src/main/resources/application.properties`
2. Update these lines:
   ```properties
   spring.datasource.url=jdbc:postgresql://your-host:5432/your-db
   spring.datasource.username=your-user
   spring.datasource.password=your-password
   ```
3. Rebuild: `mvn clean install -DskipTests`
4. Restart Spring Boot

### To Change API Ports
1. For Spring Boot - edit `application.properties`:
   ```properties
   server.port=8000  # Change from 8080
   ```
2. For Python API - edit `mock_api.py` last line:
   ```python
   uvicorn.run(app, host="0.0.0.0", port=9000)  # Change from 8000
   ```
3. Update JDBC URL and Java client code accordingly

### To Enable Real ML Model
1. Install TensorFlow (requires WSL2 or special setup):
   ```bash
   pip install tensorflow keras
   ```
2. Replace `backend/mock_api.py` with real ML code
3. Update `ForecastService` to call the new endpoint

---

**Need help? Refer to FIXES_AND_CHANGES.md for detailed troubleshooting.**
