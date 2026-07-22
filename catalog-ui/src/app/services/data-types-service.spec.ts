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

/**
 * Characterization spec pinning DataTypesService's behaviour across the AngularJS -> Angular
 * migration (SDC-4829 Phase 11 CR3). The service used to be an AngularJS `.service()` backed by
 * `$http` (whose promises resolve to a `{data}` wrapper); it is now a plain @Injectable backed by
 * Angular's HttpClient (whose observables/promises resolve to the response body directly). These
 * tests lock:
 *   - the exact URL + query-param construction of every HTTP call (the contract most likely to
 *     break silently on the HttpClient port),
 *   - the new unwrapped return contract of fetchDataTypesByModel / downloadDataType (was `.data`),
 *   - the `tosca.datatypes.Root` cache pruning, and
 *   - the pure derived-type logic (getFirsLevelOfDataTypeProperties, isDataTypeFor*).
 */
import 'rxjs/add/operator/map';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {DataTypesService} from './data-types-service';
import {SdcConfigToken} from '../ng2/config/sdc-config.config';
import {mockSdcConfig} from '../../jest/mocks/sdc-config.mock';
import {PROPERTY_DATA} from '../utils/constants';

describe('DataTypesService', () => {
    let service: DataTypesService;
    let httpMock: HttpTestingController;

    // baseUrl = mockSdcConfig.api.root + component_api_root = '/sdc2/rest' + '/v1/catalog/'
    const baseUrl = '/sdc2/rest/v1/catalog/';

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                DataTypesService,
                {provide: SdcConfigToken, useValue: mockSdcConfig}
            ]
        });
        service = TestBed.get(DataTypesService);
        httpMock = TestBed.get(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    // Regression guard for the ng:cpws hang (failure-catalog §SS): this service is held as an enumerable
    // field by the still-ng1 Restangular Resource/Service services, which are held by the Component/Resource
    // model classes whose toJSON() does angular.copy(this). If the injected HttpClient were an ENUMERABLE
    // own property, angular.copy would deep-traverse it, reach a Scope (via the ngUpgrade root injector) and
    // throw ng:cpws — aborting create/import and hanging the loader. It MUST stay non-enumerable.
    it('keeps the injected HttpClient as a non-enumerable field (angular.copy must not traverse it)', () => {
        const desc = Object.getOwnPropertyDescriptor(service, 'http');
        expect(desc).toBeDefined();
        expect(desc.enumerable).toBe(false);
        expect(Object.keys(service)).not.toContain('http');
    });

    describe('fetchDataTypesByModel', () => {
        it('GETs dataTypes with no query param when modelName is falsy and resolves the body directly', async () => {
            const body = {'tosca.datatypes.Root': {name: 'Root'}, 'org.custom.T': {name: 'org.custom.T'}};
            const promise = service.fetchDataTypesByModel(null);

            const req = httpMock.expectOne(baseUrl + 'dataTypes');
            expect(req.request.method).toBe('GET');
            expect(req.request.params.has('model')).toBe(false);
            req.flush(body);

            // New contract: the promise resolves to the body itself (previously response.data).
            await expect(promise).resolves.toBe(body);
        });

        it('appends the model query param when modelName is provided', () => {
            const promise = service.fetchDataTypesByModel('MyModel');
            const req = httpMock.expectOne((r) => r.url === baseUrl + 'dataTypes' && r.params.get('model') === 'MyModel');
            expect(req.request.method).toBe('GET');
            req.flush({});
            return promise;
        });
    });

    describe('loadDataTypesCache', () => {
        it('caches the fetched data types and deletes tosca.datatypes.Root', async () => {
            const body = {'tosca.datatypes.Root': {name: 'Root'}, 'org.custom.T': {name: 'org.custom.T'}};
            const promise = service.loadDataTypesCache(null);

            const req = httpMock.expectOne(baseUrl + 'dataTypes');
            req.flush(body);
            await promise;

            expect(service.dataTypes['tosca.datatypes.Root']).toBeUndefined();
            expect(service.dataTypes['org.custom.T']).toBeDefined();
            expect(service.getAllDataTypes()).toBe(service.dataTypes);
        });
    });

    describe('findAllDataTypesByModel', () => {
        it('resolves a Map keyed by type name with the ROOT_DATA_TYPE removed', async () => {
            const rootKey = PROPERTY_DATA.ROOT_DATA_TYPE;
            const body = {[rootKey]: {name: rootKey}, 'org.custom.T': {name: 'org.custom.T'}};
            const promise = service.findAllDataTypesByModel('MyModel');

            const req = httpMock.expectOne((r) => r.url === baseUrl + 'dataTypes' && r.params.get('model') === 'MyModel');
            req.flush(body);

            const map = await promise;
            expect(map.has(rootKey)).toBe(false);
            expect(map.get('org.custom.T').name).toBe('org.custom.T');
        });
    });

    describe('findAllDataTypesByModelIncludingRoot', () => {
        it('resolves a Map that KEEPS the root data type', async () => {
            const rootKey = PROPERTY_DATA.ROOT_DATA_TYPE;
            const body = {[rootKey]: {name: rootKey}, 'org.custom.T': {name: 'org.custom.T'}};
            const promise = service.findAllDataTypesByModelIncludingRoot(null);

            const req = httpMock.expectOne(baseUrl + 'dataTypes');
            req.flush(body);

            const map = await promise;
            expect(map.has(rootKey)).toBe(true);
            expect(map.size).toBe(2);
        });
    });

    describe('getDataTypesFromAllModelExcludePrimitives', () => {
        it('GETs allDataTypes?excludePrimitives=true and flattens the list-of-maps into DataTypeModel[]', (done) => {
            service.getDataTypesFromAllModelExcludePrimitives().subscribe((result) => {
                expect(result.length).toBe(2);
                expect(result.map((dt) => dt.name).sort()).toEqual(['org.a.A', 'org.b.B']);
                done();
            });

            const req = httpMock.expectOne(baseUrl + 'allDataTypes?excludePrimitives=true');
            expect(req.request.method).toBe('GET');
            req.flush([{'org.a.A': {name: 'org.a.A'}}, {'org.b.B': {name: 'org.b.B'}}]);
        });
    });

    describe('downloadDataType', () => {
        it('GETs downloadDataType with the dataTypeId query and resolves the body directly', async () => {
            const file = {artifactName: 'dt.yml', base64Contents: 'abc'};
            const promise = service.downloadDataType('dt-123');

            const req = httpMock.expectOne(baseUrl + 'downloadDataType?dataTypeId=dt-123');
            expect(req.request.method).toBe('GET');
            req.flush(file);

            // New contract: resolves to IFileDownload directly (previously response.data).
            await expect(promise).resolves.toEqual(file);
        });

        it('omits the query string when no dataTypeId is given', () => {
            const promise = service.downloadDataType(undefined);
            const req = httpMock.expectOne(baseUrl + 'downloadDataType');
            req.flush({});
            return promise;
        });
    });

    describe('pure derived-type logic (no HTTP)', () => {
        beforeEach(() => {
            // Seed the cache directly, mirroring the shape the BE returns.
            service.dataTypes = {
                'tosca.datatypes.Root': {derivedFromName: 'tosca.datatypes.Root', properties: []},
                'my.simple': {derivedFromName: 'string'},
                'my.complex': {
                    derivedFromName: 'tosca.datatypes.Root',
                    properties: [{name: 'p1', type: 'string'}]
                },
                'my.child': {
                    derivedFromName: 'my.complex',
                    properties: [{name: 'p2', type: 'integer'}]
                }
            } as any;
        });

        it('getFirsLevelOfDataTypeProperties concatenates parent properties before own', () => {
            const props = service.getFirsLevelOfDataTypeProperties('my.child');
            expect(props.map((p) => p.name)).toEqual(['p1', 'p2']);
        });

        it('isDataTypeForPropertyType returns false for a primitive TYPES entry', () => {
            const primitive = PROPERTY_DATA.TYPES[0];
            const property: any = {type: primitive};
            expect(service.isDataTypeForPropertyType(property)).toBe(false);
        });

        it('isDataTypeForPropertyType returns false and sets simpleType for a type derived from a simple type', () => {
            const property: any = {type: 'my.simple'};
            expect(service.isDataTypeForPropertyType(property)).toBe(false);
            expect(property.simpleType).toBe('string');
        });

        it('isDataTypeForPropertyType returns true for a genuine (complex) data type', () => {
            const property: any = {type: 'my.complex'};
            expect(service.isDataTypeForPropertyType(property)).toBe(true);
            expect(property.simpleType).toBe('');
        });

        it('isDataTypeForDataTypePropertyType treats scalar-unit.size as not-a-datatype', () => {
            const property: any = {type: 'scalar-unit.size'};
            expect(service.isDataTypeForDataTypePropertyType(property)).toBe(false);
        });
    });
});
