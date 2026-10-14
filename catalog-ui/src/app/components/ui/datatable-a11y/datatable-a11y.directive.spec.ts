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

import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { DatatableA11yModule } from './datatable-a11y.module';

@Component({
    template: `
        <ngx-datatable sdcDatatableA11y="Requirements" columnMode="flex" [headerHeight]="40" [rowHeight]="35"
                       [rows]="rows" (sort)="sorted.push($event.newValue)">
            <ngx-datatable-column name="Name" prop="name" [flexGrow]="1"></ngx-datatable-column>
            <ngx-datatable-column name="Type" prop="type" [flexGrow]="1"></ngx-datatable-column>
            <ngx-datatable-column name="Notes" prop="notes" [flexGrow]="1" [sortable]="false"></ngx-datatable-column>
            <ngx-datatable-column [flexGrow]="1"></ngx-datatable-column>
        </ngx-datatable>`
})
class HostComponent {
    rows = [{name: 'b', type: 'x', notes: ''}, {name: 'a', type: 'y', notes: ''}];
    sorted: string[] = [];
}

describe('DatatableA11yDirective', () => {
    let fixture: ComponentFixture<HostComponent>;

    const labels = (): HTMLElement[] =>
        Array.from(fixture.nativeElement.querySelectorAll('.datatable-header-cell-label'));
    const body = (): HTMLElement => fixture.nativeElement.querySelector('datatable-body');
    const press = (el: HTMLElement, key: string, init: any = {}): KeyboardEvent => {
        const event = new KeyboardEvent('keydown', {key, bubbles: true, cancelable: true, ...init});
        el.dispatchEvent(event);
        fixture.detectChanges();
        return event;
    };
    // src/jest/jest-global-mocks.ts replaces getComputedStyle with a stub that has no overflow.
    const overflowStyle = (overflowY: string): void => {
        jest.spyOn(window, 'getComputedStyle').mockReturnValue({overflowY, overflowX: 'hidden'} as any);
    };
    const overflow = (el: HTMLElement, scrollHeight: number, clientHeight: number): void => {
        Object.defineProperty(el, 'scrollHeight', {configurable: true, get: () => scrollHeight});
        Object.defineProperty(el, 'clientHeight', {configurable: true, get: () => clientHeight});
    };

    afterEach(() => jest.restoreAllMocks());

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [NgxDatatableModule, DatatableA11yModule],
            declarations: [HostComponent]
        });
        fixture = TestBed.createComponent(HostComponent);
        fixture.detectChanges();
    });

    it('makes only sortable header labels with visible text keyboard buttons', () => {
        expect(labels().map((el) => el.getAttribute('role'))).toEqual(['button', 'button', null, null]);
        expect(labels().map((el) => el.getAttribute('tabindex'))).toEqual(['0', '0', null, null]);
    });

    it('sorts once on Enter and on Space, and swallows the key', () => {
        const name = labels()[0];
        const enter = press(name, 'Enter');
        expect(enter.defaultPrevented).toBe(true);
        expect(fixture.componentInstance.sorted).toEqual(['asc']);
        const space = press(name, ' ');
        expect(space.defaultPrevented).toBe(true);
        expect(fixture.componentInstance.sorted).toEqual(['asc', 'desc']);
    });

    it('ignores other keys, auto-repeat and keys on other elements', () => {
        const name = labels()[0];
        press(name, 'a');
        press(name, 'Enter', {repeat: true});
        press(labels()[2], 'Enter');
        press(body(), 'Enter');
        expect(fixture.componentInstance.sorted).toEqual([]);
    });

    it('puts the sort state into the accessible name, after the visible text', () => {
        const [name, type] = labels();
        expect(name.hasAttribute('aria-label')).toBe(false);
        press(name, 'Enter');
        expect(name.getAttribute('aria-label')).toBe('Name, sorted ascending');
        press(name, 'Enter');
        expect(name.getAttribute('aria-label')).toBe('Name, sorted descending');
        press(type, 'Enter');
        expect(name.hasAttribute('aria-label')).toBe(false);
        expect(type.getAttribute('aria-label')).toBe('Type, sorted ascending');
    });

    it('hides the duplicate sort button from assistive technology', () => {
        press(labels()[0], 'Enter');
        const sortBtn: HTMLElement = fixture.nativeElement.querySelector('.sort-btn');
        expect(sortBtn.getAttribute('aria-hidden')).toBe('true');
    });

    it('makes the body a named, focusable region only while it scrolls', () => {
        const el = body();
        expect(el.hasAttribute('tabindex')).toBe(false);
        expect(el.hasAttribute('role')).toBe(false);

        overflowStyle('auto');
        overflow(el, 900, 500);
        fixture.detectChanges();
        expect(el.getAttribute('tabindex')).toBe('0');
        expect(el.getAttribute('role')).toBe('region');
        expect(el.getAttribute('aria-label')).toBe('Requirements');

        overflow(el, 400, 500);
        fixture.detectChanges();
        expect(el.hasAttribute('tabindex')).toBe(false);
        expect(el.hasAttribute('role')).toBe(false);
        expect(el.hasAttribute('aria-label')).toBe(false);
    });

    it('does not make an overflowing body focusable when it cannot scroll', () => {
        const el = body();
        overflowStyle('hidden');
        overflow(el, 900, 500);
        fixture.detectChanges();
        expect(el.hasAttribute('tabindex')).toBe(false);
    });
});
