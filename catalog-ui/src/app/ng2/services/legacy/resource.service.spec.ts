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
import {ResourceService} from './resource.service';
import {ComponentService} from './component.service';
import {SdcConfigToken} from '../../config/sdc-config.config';
import {SharingService} from '../sharing.service';
import {DataTypesService} from '../data-types.service';

const sdcConfig = {api: {root: 'http://localhost/', component_api_root: 'v1/catalog/'}} as any;

describe('ResourceService', () => {
    let service: ResourceService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                ResourceService,
                ComponentService,
                {provide: SdcConfigToken, useValue: sdcConfig},
                {provide: SharingService, useValue: {addUuidValue: () => {}}},
                {provide: DataTypesService, useValue: {loadDataTypesCache: () => {}}},
            ],
        });
        service = TestBed.get(ResourceService);
        httpMock = TestBed.get(HttpTestingController);
    });
    afterEach(() => httpMock.verify());

    // Regression guard (failure-catalog §SS): ResourceService is held as an enumerable field by
    // Resource model instances, whose toJSON() deep-copies itself. If any injected Angular dep were
    // an ENUMERABLE own property, the copy would traverse its whole object graph into the request
    // body. All three injected deps MUST be non-enumerable.
    it('§SS: all injected Angular deps are non-enumerable own properties (the deep copy must not traverse them)', () => {
        for (const field of ['http', 'sharingService', 'dataTypeService']) {
            const desc = Object.getOwnPropertyDescriptor(service, field);
            expect(desc).toBeDefined();
            expect(desc.enumerable).toBe(false);
            expect(Object.keys(service)).not.toContain(field);
        }
    });

    it('updateResourceGroupProperties PUTs to resources/{id}/groups/{gid}/properties', () => {
        service.updateResourceGroupProperties('id1', 'g1', []);
        const req = httpMock.expectOne('http://localhost/v1/catalog/resources/id1/groups/g1/properties');
        expect(req.request.method).toBe('PUT');
        req.flush([]);
    });

    it('getComponent (inherited) targets the resources/ segment', () => {
        service.getComponent('r1');
        httpMock.expectOne('http://localhost/v1/catalog/resources/r1').flush({uniqueId: 'r1'});
    });

    it('createComponentObject is defined as an overriding method on ResourceService', () => {
        // We verify the method override is present; full end-to-end coverage comes from Selenium.
        expect(service.createComponentObject).toBeDefined();
        expect(Object.prototype.hasOwnProperty.call(service, 'createComponentObject')).toBe(true);
    });
});
