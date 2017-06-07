//import './app/app.ts';
import {ng1appModule} from './app/app';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {enableProdMode} from '@angular/core';
import {AppModule} from './app/ng2/app.module';
import {UpgradeModule} from '@angular/upgrade/static';
import {IAppConfigurtaion} from "./app/models/app-config";

declare var __ENV__: string;
export declare var sdc2Config: IAppConfigurtaion;

if (__ENV__==='prod') {
    sdc2Config = require('./../configurations/prod.js');
    enableProdMode();
} else {
    sdc2Config = require('./../configurations/dev.js');
}

// Ugliy fix because the cookie recieved from webseal change his value after some seconds.
declare var __ENV__: string;
let timeout:number = 5000;
if (__ENV__==='dev'){
    timeout=0;
} 
window.setTimeout(()=>{
    platformBrowserDynamic().bootstrapModule(AppModule).then(platformRef => {
        const upgrade = platformRef.injector.get(UpgradeModule) as UpgradeModule;
        upgrade.bootstrap(document.body, [ng1appModule.name], {strictDi: true});
    });
},timeout);
