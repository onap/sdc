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
import { ConfigureFn, configureTests } from "../../../jest/test-config.helper";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { HeadersInterceptor } from './headers-interceptor';
import { SdcConfigToken } from "../config/sdc-config.config";
import { Injector } from '@angular/core';
import { of } from 'rxjs';
import { TranslateService } from '../shared/translator/translate.service';
import { Cookie2Service } from '../services/cookie.service';
import { HttpHelperService } from '../services/http-hepler.service';
import { HttpErrorResponse, HttpEvent, HttpResponse, HttpRequest } from '@angular/common/http';
import { ModelService } from "../services/model.service";

describe('HeadersInterceptor service', () => {

    let headerService: HeadersInterceptor;
    let cookieServiceMock: Partial<Cookie2Service>;
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
                        { provide: Cookie2Service, useValue: cookieServiceMock },
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
        of(headerService.intercept(new HttpRequest<any>('GET', '/test'), mockHandler)).subscribe(response => {
            expect(response).toBeTruthy();
            done();
        });
    });

    it('should throw 500 error with correct message', done => {
        const mockHandler = {
            handle: jest.fn(() => of(
                new HttpErrorResponse({ status: 500, error: 'This is a error' })))
        };
        of(headerService.intercept(new HttpRequest<any>('GET', '/test'), mockHandler)).subscribe(error => {
            expect(error.source.value.name).toBe('HttpErrorResponse');
            expect(error.source.value.status).toBe(500);
            done();
        });
    });
});
