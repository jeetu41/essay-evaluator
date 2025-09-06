package com.example.demo.controller;

import com.example.demo.service.AiEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class EvaluationController {

    private final AiEvaluationService aiService;

    // ✅ Constructor injection for AiEvaluationService
    public EvaluationController(AiEvaluationService aiService) {
        this.aiService = aiService;
    }

    // ✅ Health check endpoint
    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("status", "API is working!");
    }

    // ✅ Essay evaluation endpoint
    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluate(@RequestBody Map<String, String> body) {
        String topic = body.get("topic");   // optional, if you want topic
        String essay = body.get("essay");

        // FIX: use aiService instead of aiEvaluationService
        return ResponseEntity.ok(aiService.evaluateEssay(essay));
    }
}
