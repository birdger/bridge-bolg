@echo off
REM ============================================================
REM  Start the whole personal site locally.
REM
REM  IMPORTANT: keep this file PURE ASCII. cmd.exe reads .bat bytes
REM  with the OEM codepage (GBK on zh-CN Windows), so any UTF-8
REM  non-ASCII text here - even inside echo - corrupts parsing.
REM  (Verified the hard way: Chinese echo lines made cmd fail with
REM   "系统找不到指定的文件" and none of the services started.)
REM
REM  Services started:
REM    1) daily board data service  -> http://127.0.0.1:3300
REM    2) Spring Boot backend + H2  -> http://localhost:8080
REM    3) Vite dev server           -> http://localhost:5173
REM  The blog's /board page reads board data through Vite's
REM  /board-api proxy, so all three must be up.
REM ============================================================
setlocal

set "JAVA=C:\Program Files\Java\jdk-11.0.6\bin\java.exe"
if not exist "%JAVA%" set "JAVA=java"

set "BE=G:\code\repos\project_8.22\backend"
set "FE=G:\code\repos\project_8.22\frontend"
set "WB=G:\code\github\workbench"

if not exist "%BE%\target\blog-backend-0.0.1-SNAPSHOT.jar" (
  echo [ERROR] backend jar not found. Build it first:
  echo     cd /d %BE%
  echo     F:\Environment\maven\apache-maven-3.6.1\bin\mvn.cmd clean package -DskipTests
  pause
  exit /b 1
)

echo [1/3] Starting board service on port 3300 ...
pushd "%WB%"
start "daily-board" /min cmd /c "set PYTHONIOENCODING=utf-8 && python app.py"
popd

echo [2/3] Starting backend on port 8080 ...
pushd "%BE%"
start "blog-backend" /min "%JAVA%" -jar target\blog-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
popd

echo [3/3] Starting frontend on port 5173 ...
pushd "%FE%"
start "blog-frontend" /min cmd /c "npm run dev"
popd

echo Waiting for services to come up ...
timeout /t 12 /nobreak >nul
start http://localhost:5173/

echo.
echo   Blog home    : http://localhost:5173/
echo   Board page   : http://localhost:5173/board
echo   Admin login  : http://localhost:5173/admin/login   (admin / admin123)
echo   Board alone  : http://127.0.0.1:3300/
echo   Backend API  : http://localhost:8080/api/public/posts
echo.
echo To stop everything, run stop-blog.bat
exit /b 0
