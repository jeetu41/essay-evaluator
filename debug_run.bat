@echo off
echo Starting the application...
java -version
cd /d %~dp0
call mvn clean install
if %errorlevel% neq 0 (
    echo Maven build failed. Check for errors above.
    pause
    exit /b %errorlevel%
)

java -jar target/simple-ai-interview-0.0.1-SNAPSHOT.jar
pause
