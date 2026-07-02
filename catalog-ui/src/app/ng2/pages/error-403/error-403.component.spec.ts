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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {Error403PageComponent} from './error-403.component';
import {CookieService} from 'app/services/cookie-service';
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';
import {TranslateServiceConfigToken} from 'app/ng2/shared/translator/translate.service.config';

const mockTranslateConfig = {
    filePrefix: '/assets/i18n/',
    fileSuffix: '.json',
    allowedLanguages: ['en_US'],
    defaultLanguage: 'en_US'
};

const mockCookieService: Partial<CookieService> = {
    getFirstName: jest.fn(() => 'Jane'),
    getLastName: jest.fn(() => 'Doe'),
    getUserId: jest.fn(() => 'jd123')
};

describe('Error403PageComponent', () => {

    // ---------------------------------------------------------------------------
    // Factory (no TestBed — same pattern as AdminDashboardComponent spec)
    // ---------------------------------------------------------------------------

    describe('mailto computation (unit)', () => {
        it('ngOnInit builds mailto from cookie data', () => {
            const comp = new Error403PageComponent(mockCookieService as CookieService);
            comp.ngOnInit();
            const expectedSubject = encodeURIComponent('SDC Access Request for Jane Doe (jd123)');
            expect(comp.mailto).toBe('dl-asdcaccessrequest@att.com?subject=' + expectedSubject);
        });
    });

    // ---------------------------------------------------------------------------
    // Template tests (TestBed)
    // ---------------------------------------------------------------------------

    describe('template rendering', () => {

        let fixture: ComponentFixture<Error403PageComponent>;
        let component: Error403PageComponent;

        beforeEach(() => {
            TestBed.configureTestingModule({
                imports: [TranslateModule, HttpClientTestingModule],
                declarations: [Error403PageComponent],
                providers: [
                    {provide: CookieService, useValue: mockCookieService},
                    {provide: TranslateServiceConfigToken, useValue: mockTranslateConfig}
                ]
            }).compileComponents();

            fixture = TestBed.createComponent(Error403PageComponent);
            component = fixture.componentInstance;
            fixture.detectChanges();
        });

        it('renders the container div with class sdc-error-403-container', () => {
            const el: HTMLElement = fixture.nativeElement;
            expect(el.querySelector('.sdc-error-403-container')).not.toBeNull();
        });

        it('renders the title div with class sdc-error-403-container-title', () => {
            const el: HTMLElement = fixture.nativeElement;
            expect(el.querySelector('.sdc-error-403-container-title')).not.toBeNull();
        });

        it('renders the description div with classes w-sdc-error-403-text and w-sdc-form', () => {
            const el: HTMLElement = fixture.nativeElement;
            const desc = el.querySelector('.w-sdc-error-403-text.w-sdc-form');
            expect(desc).not.toBeNull();
        });
    });
});
