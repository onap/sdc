import 'rxjs/add/operator/map';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {HttpBackend} from '@angular/common/http';
import {DataTypeService} from './data-type.service';
import {DataTypesService} from '../../services/data-types-service';
import {AuthenticationService} from './authentication.service';
import {SdcConfigToken} from '../config/sdc-config.config';
import {mockSdcConfig} from '../../../jest/mocks/sdc-config.mock';

describe('DataTypeService', () => {
    let service: DataTypeService;
    let httpMock: HttpTestingController;
    let dataTypesServiceMock: any;
    let authServiceMock: any;

    const mockDataTypes = {
        'string': {name: 'string', properties: []},
        'integer': {name: 'integer', properties: []},
        'org.custom.Type1': {name: 'org.custom.Type1', properties: [{name: 'prop1', type: 'string'}]}
    };

    const mockDataTypesModel = {
        'string': {name: 'string', properties: []},
        'org.model.Type': {name: 'org.model.Type', properties: []}
    };

    beforeEach(() => {
        dataTypesServiceMock = {
            getAllDataTypes: jest.fn(() => mockDataTypes),
            getAllDataTypesFromModel: jest.fn(() => mockDataTypesModel),
            findAllDataTypesByModel: jest.fn(() => Promise.resolve(new Map()))
        };

        authServiceMock = {
            getLoggedinUser: jest.fn(() => ({userId: 'cs0008'}))
        };

        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                DataTypeService,
                {provide: DataTypesService, useValue: dataTypesServiceMock},
                {provide: AuthenticationService, useValue: authServiceMock},
                {provide: SdcConfigToken, useValue: mockSdcConfig}
            ]
        });

        service = TestBed.get(DataTypeService);
        httpMock = TestBed.get(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('getDataTypeByTypeName', () => {
        it('should return a data type by name', () => {
            const result = service.getDataTypeByTypeName('string');
            expect(result).toEqual({name: 'string', properties: []});
        });

        it('should return undefined for unknown type', () => {
            const result = service.getDataTypeByTypeName('nonexistent');
            expect(result).toBeUndefined();
        });

        it('should return complex data type', () => {
            const result = service.getDataTypeByTypeName('org.custom.Type1');
            expect(result.name).toBe('org.custom.Type1');
            expect(result.properties.length).toBe(1);
        });
    });

    describe('getDataTypeByModelAndTypeName', () => {
        it('should fetch data types for a specific model', () => {
            const result = service.getDataTypeByModelAndTypeName('myModel', 'org.model.Type');
            expect(dataTypesServiceMock.getAllDataTypesFromModel).toHaveBeenCalledWith('myModel');
            expect(result).toEqual({name: 'org.model.Type', properties: []});
        });

        it('should return undefined for missing type in model', () => {
            const result = service.getDataTypeByModelAndTypeName('myModel', 'missing');
            expect(result).toBeUndefined();
        });
    });

    describe('getDataTypeByModel', () => {
        it('should return all data types for a model', () => {
            const result = service.getDataTypeByModel('myModel');
            expect(dataTypesServiceMock.getAllDataTypesFromModel).toHaveBeenCalledWith('myModel');
            expect(result).toBe(mockDataTypesModel);
        });
    });

    describe('findById', () => {
        it('should call the correct URL', () => {
            service.findById('dt-123').subscribe(result => {
                expect(result).toEqual({name: 'found', properties: []});
            });

            const req = httpMock.expectOne('/sdc2/rest/v1/catalog/data-types/dt-123');
            expect(req.request.method).toBe('GET');
            req.flush({name: 'found', properties: []});
        });
    });

    describe('findAllProperties', () => {
        it('should call the correct URL', () => {
            service.findAllProperties('dt-123').subscribe(result => {
                expect(result).toEqual([{name: 'prop1'}]);
            });

            const req = httpMock.expectOne('/sdc2/rest/v1/catalog/data-types/dt-123/properties');
            expect(req.request.method).toBe('GET');
            req.flush([{name: 'prop1'}]);
        });
    });

    describe('createProperty', () => {
        it('should POST to properties endpoint', () => {
            const prop = {name: 'newProp', type: 'string'} as any;
            service.createProperty('dt-123', prop).subscribe();

            const req = httpMock.expectOne('/sdc2/rest/v1/catalog/data-types/dt-123/properties');
            expect(req.request.method).toBe('POST');
            expect(req.request.body).toEqual(prop);
            req.flush(prop);
        });
    });

    describe('updateProperty', () => {
        it('should PUT to properties endpoint', () => {
            const prop = {name: 'existingProp', type: 'integer'} as any;
            service.updateProperty('dt-123', prop).subscribe();

            const req = httpMock.expectOne('/sdc2/rest/v1/catalog/data-types/dt-123/properties');
            expect(req.request.method).toBe('PUT');
            req.flush(prop);
        });
    });

    describe('deleteProperty', () => {
        it('should DELETE with USER_ID header', () => {
            service.deleteProperty('dt-123', 'prop-456').subscribe();

            const req = httpMock.expectOne('/sdc2/rest/v1/catalog/data-types/dt-123/prop-456');
            expect(req.request.method).toBe('DELETE');
            expect(req.request.headers.get('USER_ID')).toBe('cs0008');
            req.flush({});
        });
    });

    describe('deleteDataType', () => {
        it('should DELETE with USER_ID header', () => {
            service.deleteDataType('dt-123').subscribe();

            const req = httpMock.expectOne('/sdc2/rest/v1/catalog/data-types/dt-123');
            expect(req.request.method).toBe('DELETE');
            expect(req.request.headers.get('USER_ID')).toBe('cs0008');
            req.flush({});
        });
    });
});
