package com.example.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.*;

@Service
public class AiEvaluationService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Now accepts both topic and essay
    public Map<String, Object> evaluateEssay(String topic, String essay) {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        // Prompt for Gemini
        Map<String, Object> request = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text",
                                        "Evaluate the following essay strictly in JSON format with keys: " +
                                                "content, structure, grammar, vocabulary, coherence, creativity, suggestions. " +
                                                "Scores should be integers (1–10). " +
                                                "\n\nEssay Topic: " + topic +
                                                "\nEssay: " + essay)
                        ))
                )
        );

        try {
            // Call Gemini API
            Map response = restTemplate.postForObject(apiUrl, request, Map.class);

            // 🔍 Debug log raw Gemini response
            System.out.println("Raw Gemini response: " + response);

            // Extract text output
            List candidates = (List) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return fallbackScores("No candidates in response");
            }

            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            Map part = (Map) parts.get(0);
            String text = (String) part.get("text");

            System.out.println("Gemini raw text: " + text);

            // Try to extract JSON from text
            Map<String, Object> parsed = tryParseJson(text);
            if (parsed != null) {
                // ✅ Add total score calculation
                addTotalScore(parsed);
                return parsed;
            }

            // If parsing fails → fallback
            return fallbackScores("Failed to parse Gemini JSON");

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackScores("Error: " + e.getMessage());
        }
    }

    private Map<String, Object> tryParseJson(String text) {
        try {
            // Extract JSON block using regex
            Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String json = matcher.group();
                return objectMapper.readValue(json, Map.class);
            }
        } catch (Exception e) {
            System.out.println("JSON parsing failed: " + e.getMessage());
        }
        return null;
    }

    private void addTotalScore(Map<String, Object> parsed) {
        int sum = 0, count = 0;
        for (String key : List.of("content", "structure", "grammar", "vocabulary", "coherence", "creativity")) {
            Object val = parsed.get(key);
            if (val instanceof Number) {
                sum += ((Number) val).intValue();
                count++;
            }
        }
        if (count > 0) {
            parsed.put("totalScore", sum);
            parsed.put("averageScore", sum / count);
        }
    }

    private Map<String, Object> fallbackScores(String reason) {
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("content", 6);
        fallback.put("structure", 7);
        fallback.put("grammar", 8);
        fallback.put("vocabulary", 7);
        fallback.put("coherence", 6);
        fallback.put("creativity", 7);
        fallback.put("suggestions", List.of("Add more examples.", "Improve coherence between paragraphs."));

        // Add total + average
        int total = 6 + 7 + 8 + 7 + 6 + 7;
        fallback.put("totalScore", total);
        fallback.put("averageScore", total / 6);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("fallback", fallback);
        result.put("error", reason);
        return result;
    }
}
