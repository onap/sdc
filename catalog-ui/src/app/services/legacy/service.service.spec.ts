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
import {LegacyServiceService} from './service.service';
import {LegacyComponentService} from './component.service';
import {SdcConfigToken} from '../../config/sdc-config.config';
import {SharingService} from '../sharing.service';
import {DataTypesService} from '../data-types.service';

const sdcConfig = {api: {root: 'http://localhost/', component_api_root: 'v1/catalog/'}} as any;

describe('LegacyServiceService', () => {
    let service: LegacyServiceService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                LegacyServiceService,
                LegacyComponentService,
                {provide: SdcConfigToken, useValue: sdcConfig},
                {provide: SharingService, useValue: {addUuidValue: () => {}}},
                {provide: DataTypesService, useValue: {loadDataTypesCache: () => {}}},
            ],
        });
        service = TestBed.get(LegacyServiceService);
        httpMock = TestBed.get(HttpTestingController);
    });
    afterEach(() => httpMock.verify());

    // Regression guard (failure-catalog §SS): LegacyServiceService is held as an enumerable field by
    // Service model instances, whose toJSON() deep-copies itself. If any injected Angular dep were
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

    it('getDistributionsList GETs services/{uuid}/distribution', () => {
        service.getDistributionsList('u1');
        const req = httpMock.expectOne('http://localhost/v1/catalog/services/u1/distribution');
        expect(req.request.method).toBe('GET');
        req.flush({distributionStatusOfServiceList: []});
    });

    it('getDistributionComponents GETs services/distribution/{distributionId}', () => {
        service.getDistributionComponents('d1');
        const req = httpMock.expectOne('http://localhost/v1/catalog/services/distribution/d1');
        expect(req.request.method).toBe('GET');
        req.flush({distributionStatusList: []});
    });

    it('markAsDeployed POSTs to services/{serviceId}/distribution/{distributionId}/markDeployed', () => {
        service.markAsDeployed('s1', 'dist1');
        const req = httpMock.expectOne('http://localhost/v1/catalog/services/s1/distribution/dist1/markDeployed');
        expect(req.request.method).toBe('POST');
        req.flush({});
    });

    it('updateGroupInstanceProperties PUTs the nested groupInstance path', () => {
        service.updateGroupInstanceProperties('s1', 'ri1', 'gi1', []);
        const req = httpMock.expectOne('http://localhost/v1/catalog/services/s1/resourceInstance/ri1/groupInstance/gi1');
        expect(req.request.method).toBe('PUT');
        req.flush([]);
    });

    it('getComponent (inherited) targets the services/ segment', () => {
        service.getComponent('svc1');
        httpMock.expectOne('http://localhost/v1/catalog/services/svc1').flush({uniqueId: 'svc1'});
    });

    it('createComponentObject is defined as an overriding method on LegacyServiceService', () => {
        // We verify the method override is present; full end-to-end coverage comes from Selenium.
        expect(service.createComponentObject).toBeDefined();
        expect(Object.prototype.hasOwnProperty.call(service, 'createComponentObject')).toBe(true);
    });

    it('distribution property defaults to "distribution"', () => {
        expect(service.distribution).toBe('distribution');
    });
});
