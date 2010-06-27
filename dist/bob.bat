@echo off

if "%OS%"=="Windows_NT" goto start
echo You need to run Windows 7 or newer
goto end

:start

@setlocal
if "%BOB_HOME%"=="" goto setDefaultBobHome

:stripBobHome
REM strip last slash, if exists
if not _%BOB_HOME:~-1%==_\ goto checkClasspath
set BOB_HOME=%BOB_HOME:~0,-1%
REM make sure it's clean
goto stripBobHome

:setDefaultBobHome
REM current script's directory
set BOB_HOME=%~dp0..

:checkClasspath
set USE_CLASSPATH=yes
rem CLASSPATH mut not be used if it is equal to "" (from ant; why not?)
if "%CLASSPATH%"=="""" set USE_CLASSPATH=no
if "%CLASSPATH%"=="" set USE_CLASSPATH=no



:end