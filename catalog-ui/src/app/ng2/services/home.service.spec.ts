import 'rxjs/add/operator/map';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {HomeService} from './home.service';
import {SdcConfigToken} from '../config/sdc-config.config';
import {SharingService} from './sharing.service';
import {ComponentFactory} from 'app/utils/component-factory';
import {mockSdcConfig} from '../../../jest/mocks/sdc-config.mock';

describe('HomeService', () => {
    let service: HomeService;
    let httpMock: HttpTestingController;
    let sharingServiceMock: any;
    let componentFactoryMock: any;

    beforeEach(() => {
        sharingServiceMock = {
            addUuidValue: jest.fn()
        };

        componentFactoryMock = {
            createService: jest.fn((s) => ({...s, componentType: 'SERVICE'})),
            createResource: jest.fn((r) => ({...r, componentType: 'RESOURCE'}))
        };

        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                HomeService,
                {provide: SdcConfigToken, useValue: mockSdcConfig},
                {provide: SharingService, useValue: sharingServiceMock},
                {provide: ComponentFactory, useValue: componentFactoryMock}
            ]
        });

        service = TestBed.get(HomeService);
        httpMock = TestBed.get(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('getAllComponents', () => {
        it('should fetch and return combined services and resources', () => {
            const mockResponse = {
                services: [
                    {uniqueId: 's1', uuid: 'uuid-s1', name: 'Svc1', lastUpdateDate: 100}
                ],
                resources: [
                    {uniqueId: 'r1', uuid: 'uuid-r1', name: 'Res1', lastUpdateDate: 200}
                ]
            };

            service.getAllComponents().subscribe(components => {
                expect(components.length).toBe(2);
                expect(components[0]['lastUpdateDate']).toBe(200);
                expect(components[1]['lastUpdateDate']).toBe(100);
            });

            const req = httpMock.expectOne('/sdc2/rest/v1/followed');
            expect(req.request.method).toBe('GET');
            req.flush(mockResponse);
        });

        it('should call componentFactory for each item', () => {
            const mockResponse = {
                services: [{uniqueId: 's1', uuid: 'uuid-s1', lastUpdateDate: 1}],
                resources: [{uniqueId: 'r1', uuid: 'uuid-r1', lastUpdateDate: 2}]
            };

            service.getAllComponents().subscribe();

            const req = httpMock.expectOne('/sdc2/rest/v1/followed');
            req.flush(mockResponse);

            expect(componentFactoryMock.createService).toHaveBeenCalledTimes(1);
            expect(componentFactoryMock.createResource).toHaveBeenCalledTimes(1);
        });

        it('should add uuid values to sharing service', () => {
            const mockResponse = {
                services: [{uniqueId: 's1', uuid: 'uuid-s1', lastUpdateDate: 1}],
                resources: []
            };

            service.getAllComponents().subscribe();

            const req = httpMock.expectOne('/sdc2/rest/v1/followed');
            req.flush(mockResponse);

            expect(sharingServiceMock.addUuidValue).toHaveBeenCalledWith('s1', 'uuid-s1');
        });

        it('should handle empty arrays', () => {
            const mockResponse = {services: [], resources: []};

            service.getAllComponents().subscribe(components => {
                expect(components.length).toBe(0);
            });

            const req = httpMock.expectOne('/sdc2/rest/v1/followed');
            req.flush(mockResponse);
        });

        it('should handle null services or resources', () => {
            const mockResponse = {services: null, resources: null};

            service.getAllComponents().subscribe(components => {
                expect(components.length).toBe(0);
            });

            const req = httpMock.expectOne('/sdc2/rest/v1/followed');
            req.flush(mockResponse);
        });

        it('should sort by lastUpdateDate descending', () => {
            const mockResponse = {
                services: [
                    {uniqueId: 's1', uuid: 'u1', lastUpdateDate: 100},
                    {uniqueId: 's2', uuid: 'u2', lastUpdateDate: 300}
                ],
                resources: [
                    {uniqueId: 'r1', uuid: 'u3', lastUpdateDate: 200}
                ]
            };

            service.getAllComponents().subscribe(components => {
                expect(components[0]['lastUpdateDate']).toBe(300);
                expect(components[1]['lastUpdateDate']).toBe(200);
                expect(components[2]['lastUpdateDate']).toBe(100);
            });

            const req = httpMock.expectOne('/sdc2/rest/v1/followed');
            req.flush(mockResponse);
        });
    });
});
