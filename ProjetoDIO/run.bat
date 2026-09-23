@echo off
chcp 65001 > nul
echo ========================================================
echo   Iniciando Voice Budget AI - Spring Boot & Spring AI
echo ========================================================
echo.
cd /d "%~dp0"

echo Verificando Java...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Java 21 nao foi encontrado no PATH. Instale o JDK 21.
    pause
    exit /b 1
)

echo.
echo Iniciando aplicacao Spring Boot...
echo Apos iniciar, acesse no navegador: http://localhost:8080
echo Console H2 disponivel em: http://localhost:8080/h2-console
echo.
call .\mvnw.cmd spring-boot:run
pause
