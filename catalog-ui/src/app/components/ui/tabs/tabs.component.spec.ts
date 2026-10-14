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

import { Component, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Tab } from './tab/tab.component';
import { Tabs } from './tabs.component';

@Component({
    template: `
        <tabs tabStyle="round-tabs" (tabChanged)="changed.push($event.title)">
            <tab tabTitle="Properties">properties body</tab>
            <tab tabTitle="Hidden" [show]="false">hidden body</tab>
            <tab tabTitle="Inputs" [indication]="indication">inputs body</tab>
            <tab tabTitle="Policies">policies body</tab>
        </tabs>`
})
class HostComponent {
    @ViewChild(Tabs) tabs: Tabs;
    changed: string[] = [];
    indication: number = 3;
}

describe('Tabs', () => {
    let fixture: ComponentFixture<HostComponent>;
    let host: HostComponent;

    const tabEls = (): HTMLElement[] => Array.from(fixture.nativeElement.querySelectorAll('.tabs > .tab'));
    const tab = (title: string): HTMLElement => fixture.nativeElement.querySelector(`.tab[data-tests-id="${title}"]`);
    const press = (el: HTMLElement, key: string, init: any = {}): KeyboardEvent => {
        const event = new KeyboardEvent('keydown', {key, bubbles: true, cancelable: true, ...init});
        el.dispatchEvent(event);
        fixture.detectChanges();
        return event;
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [NoopAnimationsModule],
            declarations: [HostComponent, Tabs, Tab]
        });
        fixture = TestBed.createComponent(HostComponent);
        host = fixture.componentInstance;
        fixture.detectChanges();
        host.changed = [];
    });

    it('renders a tablist of native tab buttons, skipping the hidden one', () => {
        expect(fixture.nativeElement.querySelector('.tabs').getAttribute('role')).toBe('tablist');
        expect(tabEls().map((el) => el.tagName)).toEqual(['BUTTON', 'BUTTON', 'BUTTON']);
        expect(tabEls().map((el) => el.getAttribute('type'))).toEqual(['button', 'button', 'button']);
        expect(tabEls().map((el) => el.getAttribute('role'))).toEqual(['tab', 'tab', 'tab']);
        expect(tabEls().map((el) => el.getAttribute('data-tests-id'))).toEqual(['Properties', 'Inputs', 'Policies']);
    });

    it('marks only the active tab selected and in the tab order', () => {
        expect(tabEls().map((el) => el.getAttribute('aria-selected'))).toEqual(['true', 'false', 'false']);
        expect(tabEls().map((el) => el.getAttribute('tabindex'))).toEqual(['0', '-1', '-1']);
    });

    it('links each tab to its panel', () => {
        const panel: HTMLElement = fixture.nativeElement.querySelector('[role="tabpanel"]');
        const properties = tab('Properties');
        expect(properties.id).toMatch(/^ui-tab-\d+$/);
        expect(properties.getAttribute('aria-controls')).toBe(panel.id);
        expect(panel.getAttribute('aria-labelledby')).toBe(properties.id);
        expect(panel.textContent).toContain('properties body');
        const ids = tabEls().map((el) => el.id);
        expect(ids.filter((id, i) => ids.indexOf(id) === i).length).toBe(3);
    });

    // Manual activation: PropertiesAssignmentComponent.tabChanged opens an unsaved-changes modal,
    // so an arrow press that selected would pop it on every keystroke.
    it('moves focus with ArrowRight, past the hidden tab, without selecting', () => {
        const properties = tab('Properties');
        properties.focus();
        const event = press(properties, 'ArrowRight');
        expect(document.activeElement).toBe(tab('Inputs'));
        expect(event.defaultPrevented).toBe(true);
        expect(host.changed).toEqual([]);
        expect(tab('Properties').getAttribute('aria-selected')).toBe('true');
    });

    it('wraps with ArrowLeft and ArrowRight, and jumps with Home and End', () => {
        press(tab('Properties'), 'ArrowLeft');
        expect(document.activeElement).toBe(tab('Policies'));
        press(tab('Policies'), 'ArrowRight');
        expect(document.activeElement).toBe(tab('Properties'));
        press(tab('Properties'), 'End');
        expect(document.activeElement).toBe(tab('Policies'));
        press(tab('Policies'), 'Home');
        expect(document.activeElement).toBe(tab('Properties'));
        expect(host.changed).toEqual([]);
    });

    // Enter and Space activate a native button by synthesising a click, which jsdom does not do.
    it('leaves Enter and Space to the native button', () => {
        const enter = press(tab('Inputs'), 'Enter');
        const space = press(tab('Inputs'), ' ');
        expect(enter.defaultPrevented).toBe(false);
        expect(space.defaultPrevented).toBe(false);
        expect(host.changed).toEqual([]);
    });

    // A held Enter repeats the native click, and Enter on the active tab would re-run the
    // consumers' unsaved-changes guard.
    it('suppresses the native activation for a held key and for the active tab', () => {
        const held = press(tab('Inputs'), 'Enter', {repeat: true});
        const heldSpace = press(tab('Inputs'), ' ', {repeat: true});
        const active = press(tab('Properties'), 'Enter');
        const activeSpace = press(tab('Properties'), ' ');
        expect([held, heldSpace, active, activeSpace].map((e) => e.defaultPrevented)).toEqual([true, true, true, true]);
        expect(host.changed).toEqual([]);
    });

    it('selects on click and moves the roving tab stop', () => {
        tab('Inputs').click();
        fixture.detectChanges();
        expect(host.changed).toEqual(['Inputs']);
        expect(tab('Inputs').getAttribute('aria-selected')).toBe('true');
        expect(tab('Inputs').classList.contains('active')).toBe(true);
        expect(tabEls().map((el) => el.getAttribute('tabindex'))).toEqual(['-1', '0', '-1']);
    });

    it('does not move focus on a modifier chord', () => {
        tab('Inputs').focus();
        press(tab('Inputs'), 'ArrowRight', {ctrlKey: true});
        press(tab('Inputs'), 'ArrowRight', {altKey: true});
        expect(document.activeElement).toBe(tab('Inputs'));
    });

    it('ignores keys that bubble up from inside a tab', () => {
        tab('Inputs').focus();
        const indication = tab('Inputs').querySelector('.tab-indication') as HTMLElement;
        const event = press(indication, 'ArrowRight');
        expect(event.defaultPrevented).toBe(false);
        expect(document.activeElement).toBe(tab('Inputs'));
    });

    // Selenium matches `contains(text(),'<title>')`, which reads direct text nodes only, and
    // `//button[@data-tests-id='Inputs']/span[contains(@class,'tab-indication')]`.
    it('keeps the title a bare text node and the indication a direct child span', () => {
        const inputs = tab('Inputs');
        expect(inputs.childNodes[0].nodeType).toBe(Node.TEXT_NODE);
        expect(inputs.childNodes[0].textContent.trim()).toBe('Inputs');
        const indication = inputs.querySelector('.tab-indication');
        expect(indication.parentElement).toBe(inputs);
        expect(indication.tagName).toBe('SPAN');
        expect(inputs.className).toContain('tab');
    });
});
