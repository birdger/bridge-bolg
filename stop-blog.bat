@echo off
REM Stop the local site: board service (3300), backend (8080), Vite (5173).
setlocal
for %%P in (3300 8080 5173) do (
  echo Stopping whatever listens on port %%P ...
  for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%P" ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1 && echo   killed PID %%a
  )
)
echo Done.
exit /b 0
