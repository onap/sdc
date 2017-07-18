@REM /*!
@REM  * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
@REM  *
@REM  * Licensed under the Apache License, Version 2.0 (the "License");
@REM  * you may not use this file except in compliance with the License.
@REM  * You may obtain a copy of the License at
@REM  *
@REM  * http://www.apache.org/licenses/LICENSE-2.0
@REM  *
@REM  * Unless required by applicable law or agreed to in writing, software
@REM  * distributed under the License is distributed on an "AS IS" BASIS,
@REM  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
@REM  * or implied. See the License for the specific language governing
@REM  * permissions and limitations under the License.
@REM  */

@echo off

SETLOCAL

set uiDir=%cd%
set currentDir=%cd%
if not ("%1" == "") set uiDir=%1

echo check npm version:
call npm -version
if errorlevel 1 (
    echo install node with npm from https://nodejs.org/en/download/
	goto done
)
echo npm is installed
echo one more check...
call npm list prompt
if errorlevel 1 (
    npm install prompt
)
echo ready to run
call node runLocalFE.js
