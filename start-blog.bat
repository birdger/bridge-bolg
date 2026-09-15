@echo off
REM ============================================================
REM  Start the whole personal site locally (4 services).
REM
REM  IMPORTANT: keep this file PURE ASCII. cmd.exe reads .bat
REM  bytes with the OEM codepage (GBK on zh-CN Windows), so any
REM  UTF-8 non-ASCII text here - even inside echo or a comment -
REM  corrupts parsing (verified: a Chinese REM line made cmd try
REM  to run 'git-bash' as a command and nothing started).
REM
REM    1) AIO knowledge base  node    -> http://127.0.0.1:3200
REM    2) Daily board (data)  python  -> http://127.0.0.1:3300
REM    3) Blog backend        java    -> http://localhost:8080
REM    4) Blog frontend       vite    -> http://localhost:5173
REM
REM  Pages:  /  blog home   /board  workbench   /kb  knowledge base
REM          /admin/login    (admin / admin123)
REM  Stop everything with stop-blog.bat
REM ============================================================
setlocal
set "KB=G:\code\knowledge\aio-kb"
set "WB=G:\code\github\workbench"
set "BE=G:\code\repos\bridge-bolg\backend"
set "FE=G:\code\repos\bridge-bolg\frontend"

set "NODE=C:\nvm4w\nodejs\node.exe"
if not exist "%NODE%" set "NODE=node"

set "JAVA=C:\Program Files\Java\jdk-11.0.6\bin\java.exe"
if not exist "%JAVA%" set "JAVA=java"

echo.
echo   Starting the personal site ...
echo.

if not exist "%BE%\target\blog-backend-0.0.1-SNAPSHOT.jar" (
  echo   [warn] backend jar not found. Build it once with:
  echo          cd /d "%BE%" ^&^& mvn clean package -DskipTests
  echo.
)

echo   [1/4] knowledge base  :3200
pushd "%KB%"
start "kb" /min "%NODE%" server.js
popd

echo   [2/4] daily board     :3300
start "board" /min cmd /c "cd /d %WB% && set PYTHONIOENCODING=utf-8 && python app.py"

echo   [3/4] blog backend    :8080
pushd "%BE%"
start "backend" /min "%JAVA%" -jar target\blog-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
popd

echo   [4/4] blog frontend   :5173
start "frontend" /min cmd /c "cd /d %FE% && npm run dev"

echo.
echo   Waiting for services to come up ...
REM ping instead of timeout: timeout.exe fails when stdin is redirected and
REM GNU timeout (git-bash) shadows the Windows one. 13 pings ~= 12 seconds.
ping -n 13 127.0.0.1 >nul

echo.
echo   ============================================================
echo     Blog home   : http://localhost:5173/
echo     Workbench   : http://localhost:5173/board
echo     Knowledge   : http://localhost:5173/kb
echo     Admin login : http://localhost:5173/admin/login   admin / admin123
echo     Board alone : http://127.0.0.1:3300/
echo     Knowledge   : http://127.0.0.1:3200/   (editor + graph + AI)
echo     SiYuan      : http://127.0.0.1:6806/   (separate app)
echo   ============================================================
echo.
start http://localhost:5173/
exit /b 0
