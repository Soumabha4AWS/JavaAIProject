@echo off
REM Build and run script for LangChain Chunking & Vector Store Demo
REM Usage: run.bat
REM        run.bat clean
REM        run.bat package

setlocal enabledelayedexpansion

echo.
echo ============================================================================
echo LangChain4j Chunking & Vector Store Demo - Build Script
echo ============================================================================
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from: https://maven.apache.org/download.cgi
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 17+ from: https://www.oracle.com/java/technologies/
    exit /b 1
)

echo [✓] Maven and Java found
echo.

REM Parse arguments
set TARGET=run
if "%1"=="clean" set TARGET=clean
if "%1"=="package" set TARGET=package
if "%1"=="test" set TARGET=test
if "%1"=="help" goto :show_help

REM Check for API key
if "%TARGET%"=="run" (
    if "%OPENAI_API_KEY%"=="" (
        echo WARNING: OPENAI_API_KEY environment variable is not set
        echo.
        echo To set it, run one of:
        echo   set OPENAI_API_KEY=sk-proj-your-key
        echo   $env:OPENAI_API_KEY="sk-proj-your-key"  (PowerShell)
        echo.
        echo Continuing anyway - search will be skipped...
        echo.
    ) else (
        echo [✓] OPENAI_API_KEY is set
        echo.
    )
)

REM Execute Maven commands
echo Running: mvn %TARGET%
echo.

if "%TARGET%"=="clean" (
    call mvn clean
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
    echo.
    echo [✓] Clean completed
) else if "%TARGET%"=="package" (
    call mvn clean package
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
    echo.
    echo [✓] Build completed
    echo.
    echo JAR file: target\chunking-vector-store-1.0.0-shaded.jar
    echo Run with: java -jar target\chunking-vector-store-1.0.0-shaded.jar
) else if "%TARGET%"=="test" (
    call mvn test
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
    echo.
    echo [✓] Tests completed
) else (
    call mvn compile
    if %ERRORLEVEL% NEQ 0 exit /b %ERRORLEVEL%
    echo.
    echo [✓] Compilation completed
    echo.
    echo Running application...
    echo.
    call mvn exec:java@ChunkingVectorStoreDemo
)

echo.
echo ============================================================================
echo Build script completed
echo ============================================================================
echo.
goto :end

:show_help
echo Usage: run.bat [COMMAND]
echo.
echo Commands:
echo   (default)    - Compile and run the application
echo   clean        - Clean build artifacts
echo   package      - Build JAR package
echo   test         - Run unit tests
echo   help         - Show this help message
echo.
echo Environment Variables:
echo   OPENAI_API_KEY       - Required for vector search operations
echo   MAVEN_OPTS           - JVM options for Maven (e.g., -Xmx2g)
echo.
echo Examples:
echo   run.bat
echo   run.bat package
echo   set OPENAI_API_KEY=sk-proj-... ^& run.bat
echo.

:end
endlocal
