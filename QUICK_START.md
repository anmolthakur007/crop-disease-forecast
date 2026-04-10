# Crop Disease Forecast - Quick Start Guide

**Version:** 1.0  
**Last Updated:** April 8, 2026

---

## ⚡ Quick Start (5 Minutes)

### Prerequisites
- Docker (for PostgreSQL)
- Java 21
- Python 3.11+
- Maven (or use included Maven Wrapper)

### One-Time Setup
```bash
# 1. Start PostgreSQL
docker-compose up -d postgres

# 2. Build projects
cd backend-api
mvn clean install -DskipTests
cd ../crop-sentinel-ui
mvn clean install -DskipTests
```

### Daily Startup (4 Terminal Windows)

**Terminal 1:**
```bash
cd c:\SEM_6\workspace\New folder\crop-disease-forecast
docker-compose up -d postgres
```

**Terminal 2:**
```bash
cd c:\SEM_6\workspace\New folder\crop-disease-forecast\backend
python mock_api.py
```

**Terminal 3:**
```bash
cd c:\SEM_6\workspace\New folder\crop-disease-forecast\backend-api
mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC"
```

**Terminal 4:**
```bash
cd c:\SEM_6\workspace\New folder\crop-disease-forecast\crop-sentinel-ui
.\mvnw.cmd javafx:run
```

---

## 🎯 Usage

1. **Upload Image** - Click "Choose Image" and select a crop leaf photo
2. **Enter Location** - Search address or enter latitude/longitude
3. **Get Forecast** - Click "Get Forecast" button
4. **View Results** - See disease risk, confidence, weather data
5. **Check History** - View all past predictions
6. **View Stats** - See overall disease statistics

---

## ❌ Common Issues & Fixes

### Backend won't start (Timezone Error)
```bash
# WRONG:
mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Duser.timezone=UTC

# RIGHT (Note the quotes!):
mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-Duser.timezone=UTC"
```

### Python API not starting
```bash
# Check dependencies
pip install fastapi uvicorn python-multipart requests pillow

# Start API
cd backend
python mock_api.py
```

### JavaFX window not visible
```bash
# Use clean build
cd crop-sentinel-ui
mvn clean javafx:run

# Wait 15-20 seconds
# Check Alt+Tab for window
```

### Port already in use
```powershell
# Check what's using the port
netstat -ano | findstr :8080

# Kill process (replace PID with actual number)
taskkill /PID <PID> /F
```

---

## 📱 API Endpoints

### Spring Boot Backend (Port 8080)
- `POST /api/forecast` - Submit image and location
- `GET /api/history` - Get all predictions
- `GET /api/stats` - Get statistics
- `GET /api/disease-info/{diseaseKey}` - Get disease information

### Python API (Port 8000)
- `POST /predict_risk/` - Disease prediction (image, lat, lon)
- `GET /docs` - API documentation
- `GET /openapi.json` - OpenAPI schema

---

## 🐳 Docker Commands

```bash
# Start PostgreSQL
docker-compose up -d postgres

# Stop PostgreSQL
docker-compose down

# View PostgreSQL logs
docker-compose logs postgres

# Connect to database
psql -h localhost -U postgres -d cropsentinel
```

---

## 📂 Project Structure

```
crop-disease-forecast/
├── backend/                    # Python FastAPI prediction service
│   ├── mock_api.py            # → Main API file
│   ├── requirements.txt
│   └── main.py
├── backend-api/               # Spring Boot REST API
│   ├── src/
│   │   ├── main/java/         # Controllers, Services, Models
│   │   ├── resources/         # → application.properties (timezone config)
│   └── pom.xml
├── crop-sentinel-ui/          # JavaFX Desktop Application
│   ├── src/main/java/         # Controllers, App.java
│   └── pom.xml
├── docker-compose.yml         # PostgreSQL configuration
├── FIXES_AND_CHANGES.md       # ← All fixes applied (read this!)
└── README.md
```

---

## 🔑 Key Configuration Files

### backend-api/src/main/resources/application.properties
```properties
# Database Connection
spring.datasource.url=jdbc:postgresql://localhost:5432/cropsentinel
spring.datasource.username=postgres
spring.datasource.password=password

# Hibernate/JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# Server Port
server.port=8080
```

### backend/mock_api.py
```python
# Mock predictions for testing
# Returns simulated disease risk scores
# Integrates with open-meteo.com for weather data
```

---

## 🚨 Support

If you encounter issues:

1. **Check FIXES_AND_CHANGES.md** for detailed troubleshooting
2. **Verify all ports are available** (5432, 8000, 8080)
3. **Check all 4 terminals are running** without errors
4. **Ensure Java 21 is installed**: `java -version`
5. **Ensure Python 3.11+ is installed**: `python --version`

---

## 📊 Verified Configurations

- ✅ Java 21.0.10
- ✅ Spring Boot 3.5.12
- ✅ Python 3.11.9
- ✅ PostgreSQL 15
- ✅ JavaFX (via Spring Boot)
- ✅ FastAPI + Uvicorn
- ✅ Windows 10/11

---

**Happy Forecasting! 🌾🚜**
