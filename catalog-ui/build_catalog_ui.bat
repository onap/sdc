
rem nvmw use v0.12.4

echo "npm install"
call npm install

if %errorlevel% NEQ 0 GOTO BAD_EXIT

echo "bower install"
call bower install
if %errorlevel% NEQ 0 GOTO BAD_EXIT


echo "build --v"
grunt build --v

if %errorlevel% NEQ 0 GOTO BAD_EXIT

GOTO SMOOTH


:BAD_EXIT
echo BOO
exit/ b 1


:SMOOTH
echo "OK."
exit /b 0
