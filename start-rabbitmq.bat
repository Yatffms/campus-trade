@echo off
rem campus-trade: start RabbitMQ (epmd first, then the broker)
set "ERLANG_HOME=D:\Rabbitmq\erlang"
set "ERTS=%ERLANG_HOME%\erts-14.2.5\bin"
set "PATH=%ERTS%;%ERLANG_HOME%\bin;D:\Rabbitmq\server\rabbitmq_server-3.13.7\sbin;%PATH%"
set "RABBITMQ_NODENAME=rabbit@localhost"
cd /d "D:\Rabbitmq\server\rabbitmq_server-3.13.7\sbin"

rem management console config (15672), written without talking to a node
if not exist "%APPDATA%\RabbitMQ" mkdir "%APPDATA%\RabbitMQ"
if not exist "%APPDATA%\RabbitMQ\enabled_plugins" echo [rabbitmq_management].> "%APPDATA%\RabbitMQ\enabled_plugins"

rem epmd must be up BEFORE the broker starts, otherwise: nodedistribution
tasklist /fi "imagename eq epmd.exe" | find /i "epmd.exe" >nul
if errorlevel 1 (
  echo [epmd] starting name server ...
  start "" /b "%ERTS%\epmd.exe" -daemon
  timeout /t 3 /nobreak >nul
) else (
  echo [epmd] already running
)

echo.
echo starting RabbitMQ ... keep this window open
echo console: http://localhost:15672   (guest / guest)
echo.
call rabbitmq-server.bat
echo.
echo [server exited, code = %errorlevel%]
pause