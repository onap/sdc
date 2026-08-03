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

import {ng1appModule} from './app/app';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {enableProdMode} from '@angular/core';
import {Router} from '@angular/router';
import {AppModule} from './app/ng2/app.module';
import {UpgradeModule} from '@angular/upgrade/static';
import {IAppConfigurtaion} from "./app/models/app-config";

declare const __ENV__: string;
export declare let sdc2Config: IAppConfigurtaion;

if (__ENV__ === 'prod') {
  sdc2Config = require('./../configurations/prod.js');
  enableProdMode();
} else {
  sdc2Config = require('./../configurations/dev.js');
}

// Uglify fix because the cookie received from webseal change his value after some seconds.
let timeout: number = 0;
if (__ENV__ === 'dev') {
  timeout = 0;
}
window.setTimeout(() => {
  platformBrowserDynamic().bootstrapModule(AppModule).then(platformRef => {
    const upgrade = platformRef.injector.get(UpgradeModule) as UpgradeModule;
    upgrade.bootstrap(document.body, [ng1appModule.name], {strictDi: true});
    // ORDER IS LOAD-BEARING: the first route activation must happen AFTER the line above, never
    // before it. Routed components inject upgraded AngularJS services (HomeComponent →
    // ModalsHandler, whose provider is `deps: ['$injector']`), and that $injector only exists once
    // upgrade.bootstrap() has run. The router's default `initialNavigation: 'legacy_enabled'`
    // navigates from an APP_BOOTSTRAP_LISTENER — i.e. inside bootstrapModule(), before this
    // callback — so those providers threw "Trying to get the AngularJS injector before it being
    // set" and the whole app rendered an empty <app-root>. `initialNavigation: 'disabled'`
    // (app.routing.module.ts) suppresses that navigation and only installs the location listener,
    // leaving us to trigger it here by hand. ui-router had no equivalent hazard: it resolved states
    // from its own run block, which by construction ran inside the AngularJS bootstrap.
    (platformRef.injector.get(Router) as Router).initialNavigation();
  });
}, timeout);
