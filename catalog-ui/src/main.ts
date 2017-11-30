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

//import './app/app.ts';
import {ng1appModule} from './app/app';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {enableProdMode} from '@angular/core';
import {AppModule} from './app/ng2/app.module';
import {UpgradeModule} from '@angular/upgrade/static';
import {IAppConfigurtaion} from "./app/models/app-config";

declare const __ENV__: string;
export declare let sdc2Config: IAppConfigurtaion;

if (__ENV__==='prod') {
    sdc2Config = require('./../configurations/prod.js');
    enableProdMode();
} else {
    sdc2Config = require('./../configurations/dev.js');
}

// Ugliy fix because the cookie recieved from webseal change his value after some seconds.
let timeout:number = 0;
if (__ENV__==='dev'){
    timeout=0;
}
window.setTimeout(()=>{
    platformBrowserDynamic().bootstrapModule(AppModule).then(platformRef => {
        const upgrade = platformRef.injector.get(UpgradeModule) as UpgradeModule;
        upgrade.bootstrap(document.body, [ng1appModule.name], {strictDi: true});
    });
},timeout);
