/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

'use strict';

import 'reflect-metadata';
import { IAppConfigurtaion, IAppMenu } from './models';
import './modules/directive-module.ts';
import './modules/service-module';
import './modules/utils.ts';

/**
 * All that is left of the AngularJS application: the module the hybrid bootstrap
 * (`main.ts:46`) hands to `UpgradeModule.bootstrap`, plus the two configuration constants that the
 * surviving AngularJS-DI'd classes still inject by name. Routing, authorisation and the
 * body-class/page-title side effects all moved to `@angular/router` — see `ng2/app.routes.ts`,
 * `ng2/guards/` and `ng2/services/route-metadata.service.ts`.
 *
 * The three module imports above are side-effect imports: each one registers an `angular.module`
 * that `dependentModules` names below, so dropping them breaks DI at bootstrap.
 */

const moduleName: string = 'sdcApp';
const directivesModuleName: string = 'Sdc.Directives';
const servicesModuleName: string = 'Sdc.Services';
const utilsModuleName: string = 'Sdc.Utils';

// Load configuration according to environment.
declare var __ENV__: string;
let sdcConfig: IAppConfigurtaion;
let sdcMenu: IAppMenu;
if (__ENV__ === 'dev') {
  sdcConfig = require('./../../configurations/dev.js');
} else if (__ENV__ === 'prod') {
  sdcConfig = require('./../../configurations/prod.js');
} else {
  console.log('ERROR: Environment configuration not found!');
}
sdcMenu = require('./../../configurations/menu.js');

const dependentModules: string[] = [
  directivesModuleName,
  servicesModuleName,
  utilsModuleName
];

export const ng1appModule: ng.IModule = angular.module(moduleName, dependentModules);

ng1appModule.constant('sdcConfig', sdcConfig);
ng1appModule.constant('sdcMenu', sdcMenu);
