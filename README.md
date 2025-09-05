# Simple AI Interview Assistant (Spring Boot + Gemini)

## Run Instructions
1. Install Java 17 and Maven.
2. Get a free Gemini API key from https://aistudio.google.com/
3. Set it in your environment:
   - PowerShell: setx GEMINI_API_KEY "your_api_key_here"
4. Run backend:
   ```
   mvn spring-boot:run
   ```
5. Test endpoint:
   ```
   curl -X POST http://localhost:8080/api/evaluate ^
        -H "Content-Type: application/json" ^
        -d "{"transcript":"I am preparing for interviews in Java and Spring Boot."}"
   ```
