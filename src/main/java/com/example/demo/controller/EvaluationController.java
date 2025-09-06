
package com.example.demo.controller;

import com.example.demo.service.AiEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
public ResponseEntity<?> evaluate(@RequestBody Map<String, String> body) {
    String topic = body.get("topic");
    String essay = body.get("essay");
    return ResponseEntity.ok(aiEvaluationService.evaluateEssay(topic, essay));
}
    
}
