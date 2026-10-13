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

import { NO_ERRORS_SCHEMA, ViewContainerRef } from '@angular/core';
import { async, TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { AuthenticationService } from './services/authentication.service';
import { RouteMetadataService } from './services/route-metadata.service';

describe('AppComponent', () => {

    let routeMetadataService: any;

    beforeEach(async(() => {
        routeMetadataService = { start: jest.fn(), stop: jest.fn() };
        TestBed.configureTestingModule({
            imports: [],
            schemas: [NO_ERRORS_SCHEMA],
            declarations: [
                AppComponent
            ],
            providers: [
                { provide: AuthenticationService },
                { provide: ViewContainerRef },
                { provide: RouteMetadataService, useValue: routeMetadataService }
            ],
        }).compileComponents();
    }));

    it('should create the app', () => {
        const fixture = TestBed.createComponent(AppComponent);
        const app = fixture.debugElement.componentInstance;
        expect(app).toBeTruthy();
    });

    // The <body> class and the WCAG page title are applied ONLY while this subscription is live, and
    // nothing else in the app starts it — a lost ngOnInit is invisible to every other gate.
    it('starts the route metadata subscription', () => {
        const fixture = TestBed.createComponent(AppComponent);
        fixture.detectChanges();
        expect(routeMetadataService.start).toHaveBeenCalled();
    });

    it('skipToMain focuses #main-content without touching the hash', () => {
        const component = TestBed.createComponent(AppComponent).componentInstance;
        const main = document.createElement('main');
        main.id = 'main-content';
        document.body.appendChild(main);
        const hashBefore = window.location.hash;
        const ev = { preventDefault: jest.fn() } as any;

        component.skipToMain(ev);

        expect(ev.preventDefault).toHaveBeenCalled();
        expect(document.activeElement).toBe(main);
        expect(main.getAttribute('tabindex')).toBe('-1');
        expect(window.location.hash).toBe(hashBefore);
        document.body.removeChild(main);
    });

    it('skipToMain is a no-op when the page has no #main-content', () => {
        const component = TestBed.createComponent(AppComponent).componentInstance;
        const ev = { preventDefault: jest.fn() } as any;
        expect(() => component.skipToMain(ev)).not.toThrow();
        expect(ev.preventDefault).toHaveBeenCalled();
    });

});
