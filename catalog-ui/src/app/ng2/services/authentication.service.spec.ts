import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {AuthenticationService} from './authentication.service';
import {Cookie2Service} from './cookie.service';
import {CacheService} from './cache.service';
import {SdcConfigToken} from '../config/sdc-config.config';
import {mockSdcConfig} from '../../../jest/mocks/sdc-config.mock';
import {createMockUser} from '../../../jest/mocks/user-data.mock';

describe('AuthenticationService', () => {
    let service: AuthenticationService;
    let httpMock: HttpTestingController;
    let cacheServiceMock: any;
    let cookieServiceMock: any;

    beforeEach(() => {
        cacheServiceMock = {
            get: jest.fn(),
            set: jest.fn()
        };

        cookieServiceMock = {
            getFirstName: jest.fn(() => 'Carlos'),
            getLastName: jest.fn(() => 'Santana'),
            getEmail: jest.fn(() => 'csantana@example.com'),
            getUserId: jest.fn(() => 'cs0008')
        };

        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                AuthenticationService,
                {provide: Cookie2Service, useValue: cookieServiceMock},
                {provide: CacheService, useValue: cacheServiceMock},
                {provide: SdcConfigToken, useValue: mockSdcConfig}
            ]
        });

        service = TestBed.get(AuthenticationService);
        httpMock = TestBed.get(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('authenticate', () => {
        it('should call the authorize endpoint', () => {
            const mockUser = createMockUser();
            service.authenticate().subscribe(user => {
                expect(user).toEqual(mockUser);
            });

            const req = httpMock.expectOne('/sdc2/rest/v1/user/authorize');
            expect(req.request.method).toBe('GET');
            req.flush(mockUser);
        });

        it('should include auth headers from cookie service', () => {
            service.authenticate().subscribe();

            const req = httpMock.expectOne('/sdc2/rest/v1/user/authorize');
            expect(req.request.headers.get('HTTP_CSP_FIRSTNAME')).toBe('Carlos');
            expect(req.request.headers.get('HTTP_CSP_LASTNAME')).toBe('Santana');
            expect(req.request.headers.get('HTTP_CSP_EMAIL')).toBe('csantana@example.com');
            expect(req.request.headers.get('HTTP_CSP_ATTUID')).toBe('cs0008');
            req.flush({});
        });
    });

    describe('getLoggedinUser / setLoggedinUser', () => {
        it('should return undefined when no user is set', () => {
            expect(service.getLoggedinUser()).toBeUndefined();
        });

        it('should return the user after setLoggedinUser', () => {
            const user = createMockUser();
            service.setLoggedinUser(user);
            expect(service.getLoggedinUser()).toBe(user);
        });

        it('should store user in cache service', () => {
            const user = createMockUser();
            service.setLoggedinUser(user);
            expect(cacheServiceMock.set).toHaveBeenCalledWith('user', user);
        });
    });
});
