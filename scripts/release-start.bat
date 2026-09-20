@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
if not defined SERVER_PORT set SERVER_PORT=8085
if not defined SPRING_PROFILES_ACTIVE set SPRING_PROFILES_ACTIVE=h2
where java >nul 2>nul
if errorlevel 1 (
  echo [ERROR] 未检测到 Java。便携包 / 服务端部署需要 JDK 17：
  echo   https://adoptium.net/
  echo 不想安装 Java 时，请使用 GitHub Release 中带运行时的 windows 原生包。
  pause
  exit /b 1
)

echo 正在启动 SAP ERP 适配
echo 浏览器打开 http://127.0.0.1:%SERVER_PORT%
echo 服务端也可直接：java -jar sap-backend-1.0.0.jar --server.port=%SERVER_PORT%

if not "%SKIP_BROWSER%"=="1" (
  start "" cmd /c "timeout /t 8 /nobreak >nul && start http://127.0.0.1:%SERVER_PORT%"
)

java %JAVA_OPTS% -jar sap-backend-1.0.0.jar --server.port=%SERVER_PORT% --spring.profiles.active=%SPRING_PROFILES_ACTIVE%
if errorlevel 1 pause
