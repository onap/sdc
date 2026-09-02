/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {LeftPaletteLoaderService} from './composition-left-palette.service';
import {SdcConfigToken} from '../config/sdc-config.config';
import {ComponentFactory} from '../utils/component-factory';
import {EventListenerService} from './event-listener.service';
import {ComponentType} from '../utils/constants';

const sdcConfig = {
    api: {
        root: 'http://localhost/',
        component_api_root: 'v1/catalog/',
        uicache_root: 'http://localhost/v1/uicache/',
        GET_uicache_left_palette: 'left-palette',
    }
} as any;

describe('LeftPaletteLoaderService', () => {
    let service: LeftPaletteLoaderService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                LeftPaletteLoaderService,
                {provide: SdcConfigToken, useValue: sdcConfig},
                {provide: ComponentFactory, useValue: {}},
                {provide: EventListenerService, useValue: {notifyObservers: () => {}}},
            ],
        });
        service = TestBed.get(LeftPaletteLoaderService);
        httpMock = TestBed.get(HttpTestingController);
    });

    afterEach(() => httpMock.verify());

    it('updateLeftPalette GETs the uicache left-palette URL with internalComponentType param', () => {
        service.loadLeftPanel({componentType: ComponentType.SERVICE} as any);
        const req = httpMock.expectOne(r => r.url.indexOf(sdcConfig.api.GET_uicache_left_palette) !== -1);
        expect(req.request.params.get('internalComponentType')).toBe(ComponentType.SERVICE);
        req.flush([]);
        httpMock.expectOne(r => r.url.indexOf('groupTypes') !== -1).flush([]);
        httpMock.expectOne(r => r.url.indexOf('policyTypes') !== -1).flush([]);
    });

    it('§SS: injected HttpClient is a NON-ENUMERABLE field', () => {
        expect(Object.keys(service)).not.toContain('http');
    });

    it('groupTypes GET uses baseUrl (root + component_api_root)', () => {
        service.loadLeftPanel({componentType: ComponentType.SERVICE} as any);
        httpMock.expectOne(r => r.url.indexOf(sdcConfig.api.GET_uicache_left_palette) !== -1).flush([]);
        const groupReq = httpMock.expectOne(r => r.url.indexOf('groupTypes') !== -1);
        expect(groupReq.request.url).toBe('http://localhost/v1/catalog/groupTypes');
        expect(groupReq.request.params.get('internalComponentType')).toBe(ComponentType.SERVICE);
        groupReq.flush([]);
        httpMock.expectOne(r => r.url.indexOf('policyTypes') !== -1).flush([]);
    });

    it('policyTypes GET uses baseUrl (root + component_api_root)', () => {
        service.loadLeftPanel({componentType: ComponentType.SERVICE} as any);
        httpMock.expectOne(r => r.url.indexOf(sdcConfig.api.GET_uicache_left_palette) !== -1).flush([]);
        httpMock.expectOne(r => r.url.indexOf('groupTypes') !== -1).flush([]);
        const policyReq = httpMock.expectOne(r => r.url.indexOf('policyTypes') !== -1);
        expect(policyReq.request.url).toBe('http://localhost/v1/catalog/policyTypes');
        expect(policyReq.request.params.get('internalComponentType')).toBe(ComponentType.SERVICE);
        policyReq.flush([]);
    });

    it('getLeftPanelComponentsForDisplay returns leftPanelComponents', () => {
        expect(service.getLeftPanelComponentsForDisplay(null)).toBeUndefined();
        service.leftPanelComponents = [];
        expect(service.getLeftPanelComponentsForDisplay(null)).toEqual([]);
    });
});
