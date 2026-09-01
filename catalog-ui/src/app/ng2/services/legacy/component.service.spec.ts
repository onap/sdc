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
import {ComponentService} from './component.service';
import {SdcConfigToken} from '../../config/sdc-config.config';
import {SharingService} from '../sharing.service';
import {DataTypesService} from '../data-types.service';

const sdcConfig = {api: {root: 'http://localhost/', component_api_root: 'v1/catalog/'}} as any;

describe('ComponentService', () => {
    let service: ComponentService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                ComponentService,
                {provide: SdcConfigToken, useValue: sdcConfig},
                {provide: SharingService, useValue: {addUuidValue: () => {}}},
                {provide: DataTypesService, useValue: {loadDataTypesCache: () => {}}},
            ],
        });
        service = TestBed.get(ComponentService);
        httpMock = TestBed.get(HttpTestingController);
    });
    afterEach(() => httpMock.verify());

    it('§SS: injected HttpClient is a NON-ENUMERABLE field (the deep copy must not traverse it)', () => {
        expect(Object.keys(service)).not.toContain('http');
        expect(Object.keys(service)).not.toContain('sharingService');
        expect(Object.keys(service)).not.toContain('dataTypeService');
    });

    it('getComponent issues GET {baseUrl}{id} and resolves via createComponentObject', async () => {
        const p = service.getComponent('abc');
        const req = httpMock.expectOne('http://localhost/v1/catalog/abc');
        expect(req.request.method).toBe('GET');
        req.flush({uniqueId: 'abc'});
        const result = await p;
        expect(result.uniqueId).toBe('abc');
    });

    it('validateName issues GET with subtype query param', () => {
        service.validateName('n1', 'VF');
        const req = httpMock.expectOne(r => r.url === 'http://localhost/v1/catalog/validate-name/n1');
        expect(req.request.params.get('subtype')).toBe('VF');
        req.flush({isValid: true});
    });

    it('deleteArtifact issues DELETE with operation param', () => {
        service.deleteArtifact('c1', 'a1', 'label1');
        const req = httpMock.expectOne(r => r.url === 'http://localhost/v1/catalog/c1/artifacts/a1');
        expect(req.request.method).toBe('DELETE');
        expect(req.request.params.get('operation')).toBe('label1');
        req.flush({});
    });
});
