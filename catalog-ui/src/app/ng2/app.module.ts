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

import {BrowserModule} from '@angular/platform-browser';
import {NgModule, APP_INITIALIZER} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {forwardRef} from '@angular/core';
import {AppComponent} from './app.component';
import {UpgradeAdapter} from '@angular/upgrade';
import {UpgradeModule} from '@angular/upgrade/static';
import {PropertiesAssignmentModule} from './pages/properties-assignment/properties-assignment.module';
import {
    DataTypesServiceProvider, SharingServiceProvider, CookieServiceProvider,
    StateParamsServiceFactory, CacheServiceProvider, EventListenerServiceProvider
} from "./utils/ng1-upgraded-provider";
import {ConfigService} from "./services/config.service";
import {HttpModule} from '@angular/http';
import {HttpService} from './services/http.service';
import {AuthenticationService} from './services/authentication.service';
import {Cookie2Service} from "./services/cookie.service";
import {ComponentServiceNg2} from "./services/component-services/component.service";
import {ServiceServiceNg2} from "./services/component-services/service.service";
import {ComponentInstanceServiceNg2} from "./services/component-instance-services/component-instance.service";
import { XHRBackend, RequestOptions } from '@angular/http';
import { SearchBarComponent } from './shared/search-bar/search-bar.component';
import { SearchWithAutoCompleteComponent } from './shared/search-with-autocomplete/search-with-autocomplete.component';

export const upgradeAdapter = new UpgradeAdapter(forwardRef(() => AppModule));

export function configServiceFactory(config:ConfigService) {
    return () => config.loadValidationConfiguration();
}


@NgModule({
    declarations: [
        AppComponent,
        SearchBarComponent,
        SearchWithAutoCompleteComponent
    ],
    imports: [
        BrowserModule,
        UpgradeModule,
        FormsModule,
        HttpModule,
        PropertiesAssignmentModule
    ],
    exports: [],
    entryComponents: [SearchWithAutoCompleteComponent],
    providers: [
        DataTypesServiceProvider,
        SharingServiceProvider,
        CookieServiceProvider,
        StateParamsServiceFactory,
        CacheServiceProvider,
        EventListenerServiceProvider,
        AuthenticationService,
        Cookie2Service,
        ConfigService,
        ComponentServiceNg2,
        ServiceServiceNg2,
        HttpService,
        ComponentInstanceServiceNg2,
        {
            provide: APP_INITIALIZER,
            useFactory: configServiceFactory,
            deps: [ConfigService],
            multi: true
        },
     ],
    bootstrap: [AppComponent]
})


export class AppModule {
   // ngDoBootstrap() {}
    constructor(public upgrade:UpgradeModule) {


    }
}
