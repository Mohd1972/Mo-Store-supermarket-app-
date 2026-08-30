@echo off
rem Starts the SuperMarket web application.
rem Make sure MySQL is running (XAMPP) before executing.
cd /d "%~dp0"
echo Building project...
call mvnw.cmd -q -DskipTests package
if errorlevel 1 (
    echo Build failed.
    exit /b 1
)
echo Starting SuperMarket on http://localhost:9090 ...
java -jar target\supermarket-web-1.0.0.jar
