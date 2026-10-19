/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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
import {Component} from '@angular/core';
import {Subject} from 'rxjs';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateService} from '../translator/translate.service';
import {GlossaryTermDirective} from './glossary-term.directive';
import {GlossaryService} from './glossary.service';
import {translateServiceStub} from './glossary.testing';

@Component({
    template: `
        <span id="plain" [sdcTerm]="term">VF</span>
        <span id="plain-no-underline" sdcTerm="VF" [sdcTermUnderline]="false">Virtual Function</span>
        <button id="btn" sdcTerm="VFC">Import VFC</button>
        <div id="wrap" sdcTerm="CR"><button id="inner">Add CR</button></div>
        <div id="opener" sdcTerm="DCAE">Import DCAE<input id="file" type="file"></div>
        <span id="pre" aria-describedby="other" sdcTerm="PNF">PNF</span>
        <span id="unknown" sdcTerm="XYZ">XYZ</span>
        <span id="empty" [sdcTerm]="''">-</span>`
})
class HostComponent {
    term = 'VF';
}

describe('GlossaryTermDirective', () => {
    let fixture: ComponentFixture<HostComponent>;
    let languageChanged: Subject<string>;
    const el = (id: string): HTMLElement => fixture.nativeElement.querySelector('#' + id);
    const tooltip = (): HTMLElement => document.body.querySelector('.sdc-glossary-tooltip') as HTMLElement;
    const fire = (target: EventTarget, type: string) => target.dispatchEvent(new Event(type, {bubbles: true}));

    beforeEach(() => {
        jest.useFakeTimers();
        TestBed.configureTestingModule({
            declarations: [HostComponent, GlossaryTermDirective],
            providers: [GlossaryService, {provide: TranslateService, useFactory: () => {
                languageChanged = new Subject<string>();
                return {...translateServiceStub(), languageChangedObservable: languageChanged};
            }}]
        });
        fixture = TestBed.createComponent(HostComponent);
        fixture.detectChanges();
    });

    afterEach(() => {
        fixture.destroy();
        jest.useRealTimers();
    });

    it('leaves the host text alone and adds no tab stop', () => {
        expect(el('plain').textContent).toBe('VF');
        expect(el('plain').hasAttribute('tabindex')).toBe(false);
        expect(el('wrap').hasAttribute('tabindex')).toBe(false);
    });

    it('describes the host, or its first focusable descendant', () => {
        expect(el('plain').getAttribute('aria-describedby')).toBe('sdc-glossary-VF');
        expect(el('btn').getAttribute('aria-describedby')).toBe('sdc-glossary-VFC');
        expect(el('inner').getAttribute('aria-describedby')).toBe('sdc-glossary-CR');
        expect(el('wrap').hasAttribute('aria-describedby')).toBe(false);
        expect(el('file').getAttribute('aria-describedby')).toBe('sdc-glossary-DCAE');
    });

    it('appends to an existing aria-describedby and removes only its own id', () => {
        expect(el('pre').getAttribute('aria-describedby')).toBe('other sdc-glossary-PNF');
        const pre = el('pre');
        fixture.destroy();
        expect(pre.getAttribute('aria-describedby')).toBe('other');
    });

    it('does nothing for unknown or empty terms', () => {
        ['unknown', 'empty'].forEach((id) => {
            expect(el(id).hasAttribute('aria-describedby')).toBe(false);
            expect(el(id).classList.contains('sdc-term')).toBe(false);
            fire(el(id), 'mouseenter');
            jest.runAllTimers();
            expect(tooltip()).toBeNull();
        });
    });

    it('underlines unless told not to', () => {
        expect(el('plain').classList.contains('sdc-term')).toBe(true);
        expect(el('plain-no-underline').classList.contains('sdc-term')).toBe(false);
    });

    it('shows the tooltip on hover after a delay and hides it on mouseleave', () => {
        fire(el('plain'), 'mouseenter');
        expect(tooltip()).toBeNull();
        jest.advanceTimersByTime(300);
        expect(tooltip().textContent).toBe(TestBed.get(GlossaryService).tooltipText('VF'));
        expect(tooltip().getAttribute('role')).toBe('tooltip');
        expect(tooltip().parentElement).toBe(document.body);
        fire(el('plain'), 'mouseleave');
        expect(tooltip()).toBeNull();
    });

    it('cancels a pending hover when the pointer leaves first', () => {
        fire(el('plain'), 'mouseenter');
        fire(el('plain'), 'mouseleave');
        jest.runAllTimers();
        expect(tooltip()).toBeNull();
    });

    it('shows immediately on focus of a focusable descendant and hides on focusout', () => {
        fire(el('inner'), 'focusin');
        expect(tooltip().textContent).toContain('Complex Resource (CR)');
        fire(el('inner'), 'focusout');
        expect(tooltip()).toBeNull();
    });

    it('shows a tooltip focused before the language file arrived once it does', () => {
        const stub = TestBed.get(TranslateService);
        const translate = stub.translate;
        stub.translate = () => '';
        fire(el('btn'), 'focusin');
        expect(tooltip()).toBeNull();
        stub.translate = translate;
        languageChanged.next('en_US');
        expect(tooltip().textContent).toContain('Virtual Function Component (VFC)');
    });

    it('stays hidden when the language arrives after focus has moved on', () => {
        const stub = TestBed.get(TranslateService);
        const translate = stub.translate;
        stub.translate = () => '';
        fire(el('btn'), 'focusin');
        fire(el('btn'), 'focusout');
        stub.translate = translate;
        languageChanged.next('en_US');
        expect(tooltip()).toBeNull();
    });

    it('closes on Escape', () => {
        fire(el('btn'), 'focusin');
        document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape'}));
        expect(tooltip()).toBeNull();
    });

    it('removes an open tooltip when the host is destroyed', () => {
        fire(el('btn'), 'focusin');
        expect(tooltip()).not.toBeNull();
        fixture.destroy();
        expect(tooltip()).toBeNull();
    });

    it('follows a changed term', () => {
        fixture.componentInstance.term = 'PNF';
        fixture.detectChanges();
        expect(el('plain').getAttribute('aria-describedby')).toBe('sdc-glossary-PNF');
        fixture.componentInstance.term = 'XYZ';
        fixture.detectChanges();
        expect(el('plain').hasAttribute('aria-describedby')).toBe(false);
    });

    describe('placement', () => {
        const offsetHeight = Object.getOwnPropertyDescriptor(HTMLElement.prototype, 'offsetHeight');
        const placeAt = (top: number): HTMLElement => {
            const host = el('btn');
            jest.spyOn(host, 'getBoundingClientRect')
                .mockReturnValue({top: top, bottom: top + 20, left: 10, right: 60, width: 50, height: 20} as ClientRect);
            fire(host, 'focusin');
            return tooltip();
        };

        beforeEach(() => {
            Object.defineProperty(document.documentElement, 'clientHeight', {value: 768, configurable: true});
            Object.defineProperty(HTMLElement.prototype, 'offsetHeight', {value: 60, configurable: true});
        });

        afterEach(() => {
            Object.defineProperty(HTMLElement.prototype, 'offsetHeight', offsetHeight);
            delete (document.documentElement as any).clientHeight;
        });

        it('opens below the host when there is room', () => {
            expect(placeAt(100).style.top).toBe((120 + 4) + 'px');
        });

        it('opens above the host when there is no room below it', () => {
            expect(placeAt(700).style.top).toBe((700 - 60 - 4) + 'px');
        });
    });
});
