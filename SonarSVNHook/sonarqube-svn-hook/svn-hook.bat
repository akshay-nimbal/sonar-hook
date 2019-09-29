@echo on
setLocal enableDelayedExpansion
dir
java -jar "%SONAR_HOOK_HOME%\SonarSVNHook.jar" %*
if %errorlevel% neq 0 exit /b %errorlevel%
ECHO %@var%
EXIT /B %ERRORLEVEL%
pause