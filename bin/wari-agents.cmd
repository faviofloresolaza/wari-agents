@echo off
setlocal
set "ROOT=%~dp0.."
java -jar "%ROOT%\runner\wari-agents-runner.jar" %*
exit /b %ERRORLEVEL%
