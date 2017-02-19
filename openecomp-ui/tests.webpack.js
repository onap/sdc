/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

var testContext = require.context('./test', true, /.test\.js$/);
testContext.keys().forEach(testContext);

var utilsContext = require.context('./src/nfvo-utils', true, /\.js$/);
utilsContext.keys().forEach(utilsContext);

var componentsContext = require.context('./src/nfvo-components', true, /\.(js|jsx)$/);
componentsContext.keys().forEach(componentsContext);

var flowsCodeContext = require.context('./src/sdc-app/flows', true, /\.(js|jsx)$/);
flowsCodeContext.keys().forEach(flowsCodeContext);

var onBoardingCodeContext = require.context('./src/sdc-app/onboarding', true, /\.(js|jsx)$/);
onBoardingCodeContext.keys().forEach(onBoardingCodeContext);

