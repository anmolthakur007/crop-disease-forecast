package com.cropsentinel.backend_api.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class DiseaseInfoService {

    private static final Map<String, Map<String, String>> DISEASE_INFO = new HashMap<>();

    static {
        addDisease("Tomato___Bacterial_spot",
                "Bacterial Spot",
                "Dark, water-soaked spots on leaves and fruits. Spots turn brown with yellow halos.",
                "Remove infected leaves immediately. Apply copper-based fungicide every 7 days. Avoid overhead watering. Rotate crops next season.");

        addDisease("Tomato___Early_blight",
                "Early Blight",
                "Brown concentric rings on lower leaves forming a target-like pattern. Leaves turn yellow and drop.",
                "Apply chlorothalonil or mancozeb fungicide. Remove affected lower leaves. Mulch around base to prevent soil splash.");

        addDisease("Tomato___Late_blight",
                "Late Blight",
                "Large, irregular dark patches on leaves. White mold may appear on underside in humid conditions.",
                "Apply metalaxyl or cymoxanil fungicide immediately. Destroy infected plants. Do not compost infected material.");

        addDisease("Tomato___Leaf_Mold",
                "Leaf Mold",
                "Yellow patches on upper leaf surface with olive-green mold on underside.",
                "Improve air circulation. Reduce humidity. Apply fungicide containing chlorothalonil.");

        addDisease("Tomato___Septoria_leaf_spot",
                "Septoria Leaf Spot",
                "Small circular spots with dark borders and light centers on lower leaves.",
                "Remove infected leaves. Apply fungicide every 7-10 days. Avoid wetting foliage when watering.");

        addDisease("Tomato___Spider_mites Two-spotted_spider_mite",
                "Spider Mites",
                "Tiny yellow or white speckles on leaves. Fine webbing visible on underside.",
                "Spray with miticide or neem oil. Increase humidity around plants. Remove heavily infested leaves.");

        addDisease("Tomato___Target_Spot",
                "Target Spot",
                "Brown spots with concentric rings on leaves, stems and fruits.",
                "Apply fungicide containing azoxystrobin. Remove infected plant debris. Improve air circulation.");

        addDisease("Tomato___Tomato_Yellow_Leaf_Curl_Virus",
                "Yellow Leaf Curl Virus",
                "Leaves curl upward and turn yellow. Plant growth is stunted.",
                "No cure available. Remove and destroy infected plants immediately. Control whitefly population with insecticide.");

        addDisease("Tomato___Tomato_mosaic_virus",
                "Tomato Mosaic Virus",
                "Mottled light and dark green pattern on leaves. Leaves may be distorted.",
                "No cure. Remove infected plants. Disinfect tools. Control aphid population. Plant resistant varieties next season.");

        addDisease("Potato___Early_blight",
                "Potato Early Blight",
                "Dark brown spots with concentric rings on older leaves.",
                "Apply mancozeb or chlorothalonil. Remove infected leaves. Ensure adequate potassium fertilization.");

        addDisease("Potato___Late_blight",
                "Potato Late Blight",
                "Water-soaked lesions that turn brown-black. White mold on leaf underside.",
                "Apply metalaxyl fungicide immediately. Destroy infected plants. Harvest remaining tubers as soon as possible.");

        addDisease("Corn_(maize)___Common_rust_",
                "Common Rust",
                "Small, circular to elongated brown pustules on both leaf surfaces.",
                "Apply fungicide containing propiconazole. Plant resistant hybrids. Scout fields regularly.");

        addDisease("Corn_(maize)___Northern_Leaf_Blight",
                "Northern Leaf Blight",
                "Long, elliptical gray-green to tan lesions on leaves.",
                "Apply fungicide at tasseling stage. Plant resistant varieties. Rotate crops.");

        addDisease("Apple___Apple_scab",
                "Apple Scab",
                "Olive-green to brown velvety spots on leaves and fruits.",
                "Apply fungicide from bud break. Rake and destroy fallen leaves. Prune for better air circulation.");

        addDisease("Apple___Black_rot",
                "Black Rot",
                "Purple spots on leaves, rotting fruits with black concentric rings.",
                "Remove mummified fruits and dead wood. Apply copper fungicide. Prune infected branches.");

        addDisease("Grape___Black_rot",
                "Grape Black Rot",
                "Brown circular spots on leaves. Fruits shrivel into hard black mummies.",
                "Apply fungicide from early season. Remove mummified fruits. Prune for air circulation.");

        addDisease("Pepper,_bell___Bacterial_spot",
                "Pepper Bacterial Spot",
                "Small, water-soaked spots on leaves that turn brown with yellow margins.",
                "Apply copper bactericide. Avoid overhead irrigation. Use disease-free seeds.");
    }

    private static void addDisease(String key, String name, String symptoms, String treatment) {
        Map<String, String> info = new HashMap<>();
        info.put("name", name);
        info.put("symptoms", symptoms);
        info.put("treatment", treatment);
        DISEASE_INFO.put(key, info);
    }

    public Map<String, String> getInfo(String diseaseKey) {
        return DISEASE_INFO.getOrDefault(diseaseKey, getDefaultInfo(diseaseKey));
    }

    private Map<String, String> getDefaultInfo(String key) {
        Map<String, String> info = new HashMap<>();
        if (key.toLowerCase().contains("healthy")) {
            info.put("name", "Healthy Plant");
            info.put("symptoms", "No disease detected. Plant appears healthy.");
            info.put("treatment", "Continue regular watering and fertilization. Monitor weekly.");
        } else {
            info.put("name", key.replace("___", " - ").replace("_", " "));
            info.put("symptoms", "Visual symptoms detected by AI model.");
            info.put("treatment", "Consult a local agricultural expert for treatment advice.");
        }
        return info;
    }
}