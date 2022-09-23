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

/**
 * Created by rc2122 on 4/6/2017.
 */
import {DataTypesService} from "../../services/data-types-service";
import {CookieService} from "../../services/cookie-service";
import {ComponentFactory} from "../../utils/component-factory"
import {EventListenerService} from "app/services/event-listener-service";
import {ModalsHandler} from "app/utils";
import {SharingService} from "../services/sharing.service";
import ICacheObject = angular.ICacheObject;

/** Services we need to upgrade from angular1 to angular2 - in the future we need to rewrite them all to angular2 **/

export function dataTypesServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('Sdc.Services.DataTypesService');
}

export function sharingServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('Sdc.Services.SharingService');
}

export function componentServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('Sdc.Services.ComponentFactory');
}

export function cookieServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('Sdc.Services.CookieService');
}

export function stateServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('$state');
}

export function stateParamsServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('$stateParams');
}

export function scopeServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('$scope');
}

export function eventListenerServiceServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('EventListenerService');
}

export function notificationServiceFactory(cacheObj: ICacheObject) {
    return cacheObj.get('Notification');
}

export function ModalsHandlerFactory(cacheObj: ICacheObject) {
    return cacheObj.get('ModalsHandler');
}


export const ComponentFactoryProvider = {
    provide: ComponentFactory,
    useFactory: componentServiceFactory,
    deps: ['$injector']
};

export const DataTypesServiceProvider = {
    provide: DataTypesService,
    useFactory: dataTypesServiceFactory,
    deps: ['$injector']
};

export const SharingServiceProvider = {
    provide: SharingService,
    useFactory: sharingServiceFactory,
    deps: ['$injector']
};


export const CookieServiceProvider = {
    provide: CookieService,
    useFactory: cookieServiceFactory,
    deps: ['$injector']
};

export const StateServiceFactory = {
    provide: '$state',
    useFactory: stateServiceFactory,
    deps: ['$injector']
};

export const ScopeServiceFactory = {
    provide: '$scope',
    useFactory: scopeServiceFactory,
    deps: ['$injector']
};

export const StateParamsServiceFactory = {
    provide: '$stateParams',
    useFactory: stateParamsServiceFactory,
    deps: ['$injector']
};

export const NotificationServiceProvider = {
    provide: 'Notification',
    useFactory: notificationServiceFactory,
    deps: ['$injector']
};

export const ModalsHandlerProvider = {
    provide: ModalsHandler,
    useFactory: ModalsHandlerFactory,
    deps: ['$injector']
};
