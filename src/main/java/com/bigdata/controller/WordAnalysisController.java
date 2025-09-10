package com.bigdata.controller;

import com.bigdata.service.WordAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WordAnalysisController {

    private final WordAnalysisService wordAnalysisService;
    private static final String FILE_PATH = "es-wiki-abstracts.txt";

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Análisis de Big Data - Contador de Palabras");
        return "index";
    }

    @PostMapping("/api/analyze")
    @ResponseBody
    public ResponseEntity<?> analyzeWords() {
        try {
            log.info("Iniciando análisis de palabras para archivo: {}", FILE_PATH);
            
            WordAnalysisService.AnalysisResult result = wordAnalysisService.analyzeFile(FILE_PATH);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("message", "Análisis completado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error durante el análisis: ", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error durante el análisis: " + e.getMessage());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/api/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "ready");
        status.put("file", FILE_PATH);
        status.put("processors", Runtime.getRuntime().availableProcessors());
        status.put("maxMemory", Runtime.getRuntime().maxMemory() / (1024 * 1024)); // MB
        status.put("freeMemory", Runtime.getRuntime().freeMemory() / (1024 * 1024)); // MB
        
        return ResponseEntity.ok(status);
    }
}
