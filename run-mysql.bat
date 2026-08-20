@echo off
if not exist ".env" (
    echo Missing .env. Copy .env.example to .env and set DB_PASSWORD.
    exit /b 1
)
for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    if not "%%A"=="" if not "%%A:~0,1"=="#" set "%%A=%%B"
)
echo ==========================================================
echo Starting HinchMart Backend with MySQL Database...
echo Using database settings from .env
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo ==========================================================
mvn -Dmaven.repo.local="%USERPROFILE%\.m2\repository" spring-boot:run
