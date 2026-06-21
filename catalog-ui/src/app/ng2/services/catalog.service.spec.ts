import 'rxjs/add/operator/map';
import 'rxjs/add/observable/of';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {CatalogService} from './catalog.service';
import {SdcConfigToken} from '../config/sdc-config.config';
import {SharingService} from './sharing.service';
import {ComponentFactory} from 'app/utils/component-factory';
import {DataTypesService} from '../../services/data-types-service';
import {mockSdcConfig} from '../../../jest/mocks/sdc-config.mock';
import {Observable} from 'rxjs/Observable';

describe('CatalogService', () => {
    let service: CatalogService;
    let httpMock: HttpTestingController;
    let sharingServiceMock: any;
    let componentFactoryMock: any;
    let dataTypesServiceMock: any;

    beforeEach(() => {
        sharingServiceMock = {
            addUuidValue: jest.fn()
        };

        componentFactoryMock = {
            createService: jest.fn((s) => ({...s, componentType: 'SERVICE'})),
            createResource: jest.fn((r) => ({...r, componentType: 'RESOURCE'}))
        };

        dataTypesServiceMock = {
            getDataTypesFromAllModelExcludePrimitives: jest.fn(() => {
                return Observable.of([]);
            })
        };

        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                CatalogService,
                {provide: SdcConfigToken, useValue: mockSdcConfig},
                {provide: SharingService, useValue: sharingServiceMock},
                {provide: ComponentFactory, useValue: componentFactoryMock},
                {provide: DataTypesService, useValue: dataTypesServiceMock}
            ]
        });

        service = TestBed.get(CatalogService);
        httpMock = TestBed.get(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('getCatalog', () => {
        it('should call the catalog endpoint with exclude params', () => {
            service.getCatalog().subscribe();

            const req = httpMock.expectOne(r =>
                r.url === '/sdc-ui-cache/rest/v1/catalog' &&
                r.params.getAll('excludeTypes').indexOf('VFCMT') !== -1 &&
                r.params.getAll('excludeTypes').indexOf('Configuration') !== -1
            );
            expect(req.request.method).toBe('GET');
            req.flush({resources: [], services: []});
        });

        it('should process resources and services from response', () => {
            const mockResponse = {
                resources: [{uniqueId: 'r1', uuid: 'u1', name: 'Res1'}],
                services: [{uniqueId: 's1', uuid: 'u2', name: 'Svc1'}]
            };

            service.getCatalog().subscribe(components => {
                expect(components.length).toBe(2);
            });

            const req = httpMock.expectOne(r => r.url === '/sdc-ui-cache/rest/v1/catalog');
            req.flush(mockResponse);
        });

        it('should add items to sharing service', () => {
            const mockResponse = {
                resources: [{uniqueId: 'r1', uuid: 'u1'}],
                services: []
            };

            service.getCatalog().subscribe();

            const req = httpMock.expectOne(r => r.url === '/sdc-ui-cache/rest/v1/catalog');
            req.flush(mockResponse);

            expect(sharingServiceMock.addUuidValue).toHaveBeenCalledWith('r1', 'u1');
        });
    });

    describe('getArchiveCatalog', () => {
        it('should call the archive endpoint', () => {
            service.getArchiveCatalog().subscribe();

            const req = httpMock.expectOne('/sdc2/rest/v1/catalog/archive/');
            expect(req.request.method).toBe('GET');
            req.flush({resources: [], services: []});
        });

        it('should process archived components', () => {
            const mockResponse = {
                resources: [{uniqueId: 'r1', uuid: 'u1', name: 'Archived'}],
                services: []
            };

            service.getArchiveCatalog().subscribe(components => {
                expect(components.length).toBe(1);
            });

            const req = httpMock.expectOne('/sdc2/rest/v1/catalog/archive/');
            req.flush(mockResponse);
        });
    });
});
