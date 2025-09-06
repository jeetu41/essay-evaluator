package com.example.demo.controller;

import com.example.demo.service.AiEvaluationService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EvaluationController {

    private final AiEvaluationService aiService;

    public EvaluationController(AiEvaluationService aiService) {
        this.aiService = aiService;
    }
    @GetMapping("/ping")
public Map<String, String> ping() {
    return Map.of("status", "API is working!");
}

    @PostMapping("/evaluate")
    public Map<String, Object> evaluate(@RequestBody Map<String, String> body) {
        String essay = body.getOrDefault("essay", "");
        return aiService.evaluateEssay(essay);
    }
}
