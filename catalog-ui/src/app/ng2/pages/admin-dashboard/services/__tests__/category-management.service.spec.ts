import 'rxjs/add/operator/map';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {CategoryManagementService} from '../category-management.service';
import {SdcConfigToken} from 'app/ng2/config/sdc-config.config';

const mockSdcConfig = {
    api: {
        root: '/sdc2/rest',
        POST_category: '/v1/category/:types/:categoryId',
        POST_subcategory: '/v1/category/:types/:categoryId/subCategory/:subCategoryId'
    }
};

describe('CategoryManagementService', () => {
    let service: CategoryManagementService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                CategoryManagementService,
                {provide: SdcConfigToken, useValue: mockSdcConfig}
            ]
        });

        service = TestBed.get(CategoryManagementService);
        httpMock = TestBed.get(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('createCategory', () => {
        it('POSTs to /v1/category/services/', () => {
            service.createCategory('service', {name: 'Xyz'}).subscribe(r => expect(r.name).toBe('Xyz'));
            const req = httpMock.expectOne(r => r.url.endsWith('/v1/category/services/'));
            expect(req.request.method).toBe('POST');
            expect(req.request.body).toEqual({name: 'Xyz'});
            req.flush({name: 'Xyz', uniqueId: 'c1'});
        });

        it('appends "s" to the type to form the collection segment', () => {
            service.createCategory('resource', {name: 'R'}).subscribe();
            const req = httpMock.expectOne(r => r.url.endsWith('/v1/category/resources/'));
            expect(req.request.method).toBe('POST');
            req.flush({name: 'R', uniqueId: 'c2'});
        });
    });

    describe('createSubCategory', () => {
        it('POSTs to /v1/category/resources/CAT_ID/subCategory/', () => {
            service.createSubCategory('resource', 'CAT_ID', {name: 'Y'}).subscribe(r => expect(r.name).toBe('Y'));
            const req = httpMock.expectOne(r => r.url.endsWith('/v1/category/resources/CAT_ID/subCategory/'));
            expect(req.request.method).toBe('POST');
            expect(req.request.body).toEqual({name: 'Y'});
            req.flush({name: 'Y', uniqueId: 'sc1'});
        });

        it('uses the correct type and categoryId in the URL', () => {
            service.createSubCategory('service', 'MY_CAT', {name: 'Sub'}).subscribe();
            const req = httpMock.expectOne(r => r.url.endsWith('/v1/category/services/MY_CAT/subCategory/'));
            expect(req.request.method).toBe('POST');
            req.flush({name: 'Sub', uniqueId: 'sc2'});
        });
    });
});
