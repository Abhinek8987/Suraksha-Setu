@echo off
echo =========================================
echo Pushing Suraksha-Setu to GitHub...
echo =========================================
git init
git add .
git commit -m "Finalizing Suraksha-Setu Safety Features and README"
git branch -M main
git remote add origin https://github.com/Abhinek8987/Suraksha-Setu.git
git push -u origin main
echo.
echo =========================================
echo Done! If you saw any errors, make sure you are logged into GitHub.
echo Press any key to exit...
pause
