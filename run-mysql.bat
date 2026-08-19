@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo ==========================================================
echo Starting HinchMart Backend with MySQL Database...
echo Make sure MySQL Server is running and password matches .env
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo ==========================================================
"C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" spring-boot:run
