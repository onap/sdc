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
import {HttpService} from "./services/http.service";
import {HttpModule} from '@angular/http';
import {AuthenticationService} from './services/authentication.service';
import {Cookie2Service} from "./services/cookie.service";
import {ComponentServiceNg2} from "./services/component-services/component.service";
import {ServiceServiceNg2} from "./services/component-services/service.service";
import {ComponentInstanceServiceNg2} from "./services/component-instance-services/component-instance.service";

export const upgradeAdapter = new UpgradeAdapter(forwardRef(() => AppModule));

export function configServiceFactory(config:ConfigService) {
    return () => config.loadValidationConfiguration();
}

// export function httpServiceFactory(backend: XHRBackend, options: RequestOptions) {
//     return new HttpService(backend, options);
// }

@NgModule({
    declarations: [
        AppComponent
    ],
    imports: [
        BrowserModule,
        UpgradeModule,
        FormsModule,
        HttpModule,
        PropertiesAssignmentModule
    ],
    exports: [],
    entryComponents: [],
    providers: [
        HttpService,
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
        ComponentInstanceServiceNg2,
        {
            provide: APP_INITIALIZER,
            useFactory: configServiceFactory,
            deps: [ConfigService],
            multi: true
        }
     ],
    bootstrap: [AppComponent]
})


export class AppModule {
   // ngDoBootstrap() {}
    constructor(public upgrade:UpgradeModule) {


    }
}
