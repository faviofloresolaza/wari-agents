@echo off
setlocal
set "ROOT=%~dp0.."
call "%ROOT%\bin\build-runner.cmd"
if errorlevel 1 exit /b 1
if not exist "%ROOT%\runner\build\test-classes" mkdir "%ROOT%\runner\build\test-classes"
javac --release 21 -d "%ROOT%\runner\build\test-classes" "%ROOT%\runner\test\wari\agents\RunnerTest.java"
if errorlevel 1 exit /b 1
java -cp "%ROOT%\runner\build\test-classes" wari.agents.RunnerTest "%ROOT%\runner\wari-agents-runner.jar"
exit /b %ERRORLEVEL%
