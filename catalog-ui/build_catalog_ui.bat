
rem nvmw use v0.12.4
echo "configure proxy for ATT"
call npm config set proxy http://one.proxy.att.com:8080

echo "npm cache clean"
call npm cache clean

echo "npm install"
call npm install

if %errorlevel% NEQ 0 GOTO BAD_EXIT

echo "npm run build:prod"
npm run build:prod

if %errorlevel% NEQ 0 GOTO BAD_EXIT

GOTO SMOOTH

:BAD_EXIT
echo BOO
exit /b 1


:SMOOTH
echo "OK."
exit /b 0
