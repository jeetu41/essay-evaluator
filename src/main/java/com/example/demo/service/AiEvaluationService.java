package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class AiEvaluationService {

    private final WebClient webClient;
    private final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    @Value("${gemini.api.key}")
    private String apiKey;

    public AiEvaluationService() {
        this.webClient = WebClient.builder().build();
    }

    public Map<String, Object> evaluateEssay(String essay) {
        String prompt = """
        You are an essay evaluator. 
        Analyze the essay for: content, structure, grammar, vocabulary, coherence, creativity. 
        Return ONLY valid JSON in this format (no extra text, no markdown):

        {
          "content": 0-10,
          "structure": 0-10,
          "grammar": 0-10,
          "vocabulary": 0-10,
          "coherence": 0-10,
          "creativity": 0-10,
          "suggestions": ["short actionable suggestion 1", "short actionable suggestion 2"]
        }

        Essay: %s
        """.formatted(essay);

        try {
            Map response = webClient.post()
                    .uri(GEMINI_URL + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(Map.of("contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // Extract main text from Gemini response
            String evaluationText = extractText(response);

            if (evaluationText != null) {
                try {
                    return parseJsonString(evaluationText); // ✅ should now be valid JSON
                } catch (Exception e) {
                    return Map.of(
                            "error", "Could not parse AI response as JSON",
                            "raw_response", evaluationText,
                            "fallback", fakeScores(essay)
                    );
                }
            } else {
                return Map.of(
                        "error", "Empty response from Gemini",
                        "fallback", fakeScores(essay)
                );
            }

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("503")) {
                errorMessage = "Gemini API service is temporarily unavailable. Using fallback evaluation.";
            } else if (errorMessage.contains("403")) {
                errorMessage = "API key authentication failed. Please check your API key.";
            } else if (errorMessage.contains("400")) {
                errorMessage = "Invalid API request. Please check the request format.";
            }

            return Map.of(
                    "error", errorMessage,
                    "fallback", fakeScores(essay)
            );
        }
    }

    /**
     * Extracts the main text from Gemini API response
     */
    private String extractText(Map response) {
        try {
            List candidates = (List) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map firstCandidate = (Map) candidates.get(0);
                Map content = (Map) firstCandidate.get("content");
                List parts = (List) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    Map firstPart = (Map) parts.get(0);
                    return (String) firstPart.get("text");
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Converts AI JSON string into a Map
     */
    private Map<String, Object> parseJsonString(String json) {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI JSON: " + e.getMessage());
        }
    }

    /**
     * Fallback scores if API fails
     */
    private Map<String, Object> fakeScores(String essay) {
        return Map.of(
                "content", 7,
                "structure", 7,
                "grammar", 8,
                "vocabulary", 7,
                "coherence", 8,
                "creativity", 6,
                "suggestions", List.of(
                        "Expand your ideas with examples",
                        "Improve vocabulary variety",
                        "Add a stronger conclusion"
                )
        );
    }
}
