/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2022 Nordix Foundation. All rights reserved.
*  ================================================================================
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  SPDX-License-Identifier: Apache-2.0
*  ============LICENSE_END=========================================================
*/

import { async, TestBed } from "@angular/core/testing";
import { ConfigureFn, configureTests } from "../../jest/test-config.helper";
import { NO_ERRORS_SCHEMA, Testability } from "@angular/core";
import { HeadersInterceptor } from './headers-interceptor';
import { SdcConfigToken } from "../config/sdc-config.config";
import { Injector } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/throw';
import { of } from 'rxjs';
import { TranslateService } from '../shared/translator/translate.service';
import { CookieService } from '../services/cookie.service';
import { HttpHelperService } from '../services/http-helper.service';
import { HttpErrorResponse, HttpEvent, HttpResponse, HttpRequest } from '@angular/common/http';
import { ModelService } from "../services/model.service";

describe('HeadersInterceptor service', () => {

    let headerService: HeadersInterceptor;
    let cookieServiceMock: Partial<CookieService>;
    let httpServiceMock: Partial<HttpHelperService>;

    beforeEach(
        async(() => {
            cookieServiceMock = {
                getCookieByName: jest.fn(),
                getUserIdSuffix: jest.fn().mockImplementation(() => 'urlSuffix'),
                getUserId: jest.fn().mockImplementation(() => '009'),
                getFirstName: jest.fn().mockImplementation(() => 'First Name'),
                getLastName: jest.fn().mockImplementation(() => 'Last Name'),
                getEmail: jest.fn().mockImplementation(() => 'names@test.com'),
            };
            httpServiceMock = {
                getUuidValue: jest.fn().mockImplementation(() => '001'),
                replaceUrlParams: jest.fn(),
                getHeaderMd5: jest.fn()
            };

            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [],
                    imports: [],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [HeadersInterceptor,
                        { provide: SdcConfigToken },
                        { provide: Injector },
                        { provide: CookieService, useValue: cookieServiceMock },
                        { provide: HttpHelperService, useValue: httpServiceMock },
                        { provide: TranslateService },
                        { provide: ModelService }
                    ],
                });
            };
            configureTests(configure).then(testBed => {
                headerService = TestBed.get(HeadersInterceptor);
            });
        })
    );

    it('HeadersInterceptor should be created', () => {
        expect(headerService).toBeTruthy();
    });

    it('should set correct request headers', done => {
        const mockHandler = {
            handle: jest.fn(() => of(
                new HttpResponse({ status: 200 })))
        };
        headerService.intercept(new HttpRequest<any>('GET', '/test'), mockHandler).subscribe(response => {
            expect(response).toBeTruthy();
            const forwardedRequest = mockHandler.handle.mock.calls[0][0] as HttpRequest<any>;
            expect(forwardedRequest.headers.get('urlSuffix')).toBe('009');
            done();
        });
    });

    it('should throw 500 error with correct message', done => {
        const mockHandler = {
            handle: jest.fn(() => of(
                new HttpErrorResponse({ status: 500, error: 'This is a error' })))
        };
        headerService.intercept(new HttpRequest<any>('GET', '/test'), mockHandler).subscribe((event: any) => {
            expect(event.name).toBe('HttpErrorResponse');
            expect(event.status).toBe(500);
            done();
        });
    });

    describe('Testability reporting', () => {

        let testabilityMock: { increasePendingRequestCount: jest.Mock, decreasePendingRequestCount: jest.Mock };
        let interceptor: HeadersInterceptor;

        beforeEach(() => {
            testabilityMock = {
                increasePendingRequestCount: jest.fn(),
                decreasePendingRequestCount: jest.fn()
            };
            const injectorMock = {
                get: (token: any, notFoundValue?: any) => token === Testability ? testabilityMock : notFoundValue
            } as Injector;
            interceptor = new HeadersInterceptor(injectorMock, cookieServiceMock as CookieService,
                httpServiceMock as HttpHelperService);
        });

        it('should not report a request before it is subscribed to', () => {
            const mockHandler = { handle: jest.fn(() => of(new HttpResponse({ status: 200 }))) };

            interceptor.intercept(new HttpRequest<any>('GET', '/test'), mockHandler);

            expect(testabilityMock.increasePendingRequestCount).not.toHaveBeenCalled();
            expect(mockHandler.handle).not.toHaveBeenCalled();
        });

        it('should report a request as pending until it completes', done => {
            const mockHandler = { handle: jest.fn(() => of(new HttpResponse({ status: 200 }))) };

            interceptor.intercept(new HttpRequest<any>('GET', '/test'), mockHandler).subscribe(() => {
                // Still counted as in flight while the response is being delivered.
                expect(testabilityMock.increasePendingRequestCount).toHaveBeenCalledTimes(1);
                expect(testabilityMock.decreasePendingRequestCount).not.toHaveBeenCalled();
            }, undefined, () => {
                // The teardown that clears the count runs after the complete handler.
                setTimeout(() => {
                    expect(testabilityMock.decreasePendingRequestCount).toHaveBeenCalledTimes(1);
                    done();
                }, 0);
            });
        });

        it('should stop reporting a request as pending when it fails', done => {
            const mockHandler = {
                handle: jest.fn(() => Observable.throw(new HttpErrorResponse({ status: 500 })))
            };

            interceptor.intercept(new HttpRequest<any>('GET', '/test'), mockHandler).subscribe(undefined, () => {
                setTimeout(() => {
                    expect(testabilityMock.increasePendingRequestCount).toHaveBeenCalledTimes(1);
                    expect(testabilityMock.decreasePendingRequestCount).toHaveBeenCalledTimes(1);
                    done();
                }, 0);
            });
        });

        it('should still issue the request when Testability is unavailable', done => {
            const mockHandler = { handle: jest.fn(() => of(new HttpResponse({ status: 200 }))) };
            const emptyInjector = { get: (token: any, notFoundValue?: any) => notFoundValue } as Injector;
            const plainInterceptor = new HeadersInterceptor(emptyInjector, cookieServiceMock as CookieService,
                httpServiceMock as HttpHelperService);

            plainInterceptor.intercept(new HttpRequest<any>('GET', '/test'), mockHandler).subscribe(response => {
                expect(response).toBeTruthy();
                done();
            });
        });
    });
});
