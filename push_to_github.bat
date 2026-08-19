@echo off
set "PATH=%LOCALAPPDATA%\MinGit\cmd;%PATH%"
echo ==========================================
echo Pushing HinchMart Backend to GitHub...
echo Repository: https://github.com/anushatechnologies/hinchbackend.git
echo ==========================================
git push -u origin main
echo.
echo Done!
pause
