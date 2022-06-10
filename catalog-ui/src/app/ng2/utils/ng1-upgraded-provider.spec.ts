/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2022 Nordix Foundation. All rights reserved.
*  ================================================================================
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  SPDX-License-Identifier: Apache-2.0
*  ============LICENSE_END=========================================================
*/

import { async } from "@angular/core/testing";

const Utils = require('./ng1-upgraded-provider');

describe('ng1-upgraded-provider', () => {

    let cacheObjMock: Partial<angular.ICacheObject>;
    beforeEach(
        async(() => {
            const responseData = {
                'Sdc.Services.DataTypesService': 'dataTypesService',
                'Sdc.Services.SharingService': 'sharingService',
                'Sdc.Services.ComponentFactory': 'componentFactory',
                'Sdc.Services.CookieService': 'cookieService',
                '$state': 'state',
                '$stateParams': 'stateParams',
                '$scope': 'scope',
                'EventListenerService': 'eventListenerService',
                'Notification': 'notification',
                'ModalsHandler': 'modalsHandler'
            };
            cacheObjMock = {
                get: jest.fn().mockImplementation((input: string) => responseData[input])
            };
        })
    );

    it('should return dataTypesService', () => {
        expect(Utils.dataTypesServiceFactory(cacheObjMock)).toEqual('dataTypesService');
    });

    it('should return sharingService', () => {
        expect(Utils.sharingServiceFactory(cacheObjMock)).toEqual('sharingService');
    });

    it('should return componentFactory', () => {
        expect(Utils.componentServiceFactory(cacheObjMock)).toEqual('componentFactory');
    });

    it('should return cookieService', () => {
        expect(Utils.cookieServiceFactory(cacheObjMock)).toEqual('cookieService');
    });

    it('should return state', () => {
        expect(Utils.stateServiceFactory(cacheObjMock)).toEqual('state');
    });

    it('should return stateParams', () => {
        expect(Utils.stateParamsServiceFactory(cacheObjMock)).toEqual('stateParams');
    });

    it('should return scope', () => {
        expect(Utils.scopeServiceFactory(cacheObjMock)).toEqual('scope');
    });

    it('should return eventListenerService', () => {
        expect(Utils.eventListenerServiceServiceFactory(cacheObjMock)).toEqual('eventListenerService');
    });

    it('should return notification service', () => {
        expect(Utils.notificationServiceFactory(cacheObjMock)).toEqual('notification');
    });

    it('should return modalsHandler', () => {
        expect(Utils.ModalsHandlerFactory(cacheObjMock)).toEqual('modalsHandler');
    });

});
