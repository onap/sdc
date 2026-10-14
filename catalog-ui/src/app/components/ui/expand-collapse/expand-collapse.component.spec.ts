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

import { Component, NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ExpandCollapseComponent } from './expand-collapse.component';

@Component({
    template: `
        <ng2-expand-collapse state="0">
            <span expandCollapseHeader tooltip="Properties">Properties</span>
            <content>section body</content>
        </ng2-expand-collapse>`
})
class HostComponent {}

describe('ExpandCollapseComponent', () => {
    let fixture: ComponentFixture<HostComponent>;
    const toggle = (): HTMLButtonElement => fixture.nativeElement.querySelector('.expand-collapse-title');
    const content = (): HTMLElement => fixture.nativeElement.querySelector('.expand-collapse-content');

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [HostComponent, ExpandCollapseComponent],
            schemas: [NO_ERRORS_SCHEMA]
        });
        fixture = TestBed.createComponent(HostComponent);
        fixture.detectChanges();
    });

    it('titles the section with a heading that is not the page h1', () => {
        expect(fixture.nativeElement.querySelectorAll('h1').length).toBe(0);
        expect(toggle().parentElement.tagName).toBe('H2');
    });

    // Selenium matches `ng2-expand-collapse [expandCollapseHeader]` and `.//*[@expandcollapseheader][contains(text(),…)]`.
    it('toggles from a native button that carries the projected header as its name', () => {
        expect(toggle().tagName).toBe('BUTTON');
        expect(toggle().getAttribute('type')).toBe('button');
        expect(toggle().querySelector('[expandCollapseHeader]').textContent).toBe('Properties');
        expect(toggle().textContent.trim()).toBe('Properties');
    });

    // A button may hold phrasing content only.
    it('holds no flow content or svg-icon host inside the button', () => {
        expect(toggle().querySelectorAll('div, header, svg-icon').length).toBe(0);
        const chevron = toggle().querySelector('.expand-collapse-title-icon');
        expect(chevron.tagName).toBe('SPAN');
        expect(chevron.getAttribute('aria-hidden')).toBe('true');
        expect(chevron.querySelector('svg path')).not.toBeNull();
    });

    it('reports and flips the expanded state', () => {
        expect(toggle().getAttribute('aria-expanded')).toBe('true');
        expect(toggle().getAttribute('aria-controls')).toBe(content().id);
        expect(content().id).toMatch(/^expand-collapse-content-\d+$/);
        toggle().click();
        fixture.detectChanges();
        expect(toggle().getAttribute('aria-expanded')).toBe('false');
        expect(content().classList.contains('collapsed')).toBe(true);
        toggle().click();
        fixture.detectChanges();
        expect(toggle().getAttribute('aria-expanded')).toBe('true');
    });
});
