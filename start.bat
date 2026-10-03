@echo off
REM =============================================================================
REM TaskBoard 一键启动脚本（Windows）
REM 并行启动后端（Spring Boot）和前端（Vite Dev Server）
REM =============================================================================

setlocal enabledelayedexpansion

echo ========================================
echo   TaskBoard 一键启动
echo ========================================
echo.

REM 检查前置依赖
echo [检查] 验证运行环境...

where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未找到 Java，请先安装 JDK 25+
    echo 参考: .qoder/rules/00-project-charter.md
    pause
    exit /b 1
)

java -version 2>&1 | find "version" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] Java 版本不符合要求（需要 JDK 25+）
    pause
    exit /b 1
)

where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未找到 Node.js，请先安装 Node.js 18+
    echo 参考: .qoder/rules/00-project-charter.md
    pause
    exit /b 1
)

where npm >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [错误] 未找到 npm，请检查 Node.js 安装
    pause
    exit /b 1
)

echo [通过] Java、Node.js、npm 已就绪
echo.

REM 安装前端依赖（如尚未安装）
cd /d "%~dp0web"
if not exist "node_modules" (
    echo [准备] 安装前端依赖...
    call npm install
    if %ERRORLEVEL% NEQ 0 (
        echo [错误] 前端依赖安装失败
        pause
        exit /b 1
    )
)

cd /d "%~dp0"

REM 启动后端（后台进程）
echo [启动] 后端服务（Spring Boot, http://localhost:8081）...
start "TaskBoard Backend" cmd /k "%~dp0server\gradlew.bat bootRun"

REM 等待后端初始化
echo [等待] 后端启动中（约 10-15 秒）...
timeout /t 10 /nobreak >nul

REM 启动前端（后台进程）
echo [启动] 前端服务（Vite Dev, http://localhost:5173）...
start "TaskBoard Frontend" cmd /k "cd /d %~dp0web && npm run dev"

echo.
echo ========================================
echo   启动完成！
echo ========================================
echo.
echo   后端地址: http://localhost:8081
echo   前端地址: http://localhost:5173
echo   H2 Console: http://localhost:8081/h2-console
echo.
echo   健康检查: curl http://localhost:8081/api/health
echo.
echo   按任意键关闭此窗口（不会停止服务）...
pause >nul
