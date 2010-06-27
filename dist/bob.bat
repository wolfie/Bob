@echo off

REM This startup script has been heavily influenced by Apache Ant's 
REM ant.bat, which is licensed under Apache License 2.0

if "%OS%"=="Windows_NT" goto start
echo You need to run Windows 7 or newer
goto omega

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
set BOB_HOME=%~dp0
goto stripBobHome

:checkClasspath
set USE_CLASSPATH=yes
rem CLASSPATH mut not be used if it is equal to "" (from ant; why not?)
if "%CLASSPATH%"=="""" set USE_CLASSPATH=no 
if "%CLASSPATH%"=="" set USE_CLASSPATH=no

set BOB_CMD_LINE_ARGS=
:setupArgs
if ""%1""=="""" goto doneStart
set BOB_CMD_LINE_ARGS=%BOB_CMD_LINE_ARGS% %1
shift
goto setupArgs

:doneStart

:stripClasspath
if not _%CLASSPATH:~-1%==_\ goto findBobHome
set CLASSPATH=%CLASSPATH:~0,-1%
goto stripClasspath

:findBobHome
if exist "%BOB_HOME%\lib\bob.jar" goto checkJava
if not exist "%ProgramFiles%\bob" goto checkSystemDrive
set BOB_HOME=%ProgramFiles%\bob
goto checkJava

:checkSystemDrive
if not exist "%SystemDrive%\bob\lib\bob.jar" goto checkCDrive
set ANT_HOME=%SystemDrive%\bob
goto checkJava

:checkCDrive
if not exist "C:\bob\lib\bob.jar" goto checkDDrive
set ANT_HOME=C:\bob
goto checkJava

:checkDDrive
if not exist "D:\bob\lib\bob.jar" goto noBobHome
set ANT_HOME=D:\bob
goto checkJava

:noBobHome
echo BOB_HOME is set incorrectly or Bob could not be located. Please set BOB_HOME.
goto end

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%"=="" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%"=="" set _JAVACMD=%JAVA_HOME%\bin\java.exe

:noJavaHome
if "%_JAVACMD%"=="" set _JAVACMD=java.exe

:runBob
if "%USE_CLASSPATH%"=="no" goto runBobNoClasspath
:runBobWithClasspath
"%_JAVACMD%" %BOB_OPTS% -jar "%BOB_HOME%\lib\bob.jar" %BOB_ARGS% -cp "%CLASSPATH%" %BOB_CMD_LINE_ARGS%
set BOB_ERROR=%ERRORLEVEL%
goto end

:runBobNoClasspath
"%_JAVACMD%" %BOB_OPTS% -jar "%BOB_HOME%\lib\bob.jar" %BOB_ARGS% %BOB_CMD_LINE_ARGS%
set BOB_ERROR=%ERRORLEVEL%
goto end

:end
REM https://issues.apache.org/bugzilla/show_bug.cgi?id=32069
if not "%_JAVACMD%"=="" set _JAVACMD=
if not "%_BOB_CMD_LINE_ARGS%"=="" set BOB_CMD_LINE_ARGS=

if "%BOB_ERROR%"=="0" goto mainEnd

:mainEnd
@endlocal

:omega