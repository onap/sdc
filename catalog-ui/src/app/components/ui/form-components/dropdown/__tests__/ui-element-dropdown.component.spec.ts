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
import * as fs from 'fs';
import * as path from 'path';
import { TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { UiElementDropDownComponent } from '../ui-element-dropdown.component';

describe('UiElementDropDownComponent', () => {
    beforeEach(() => {
        TestBed.configureTestingModule({ imports: [FormsModule], declarations: [UiElementDropDownComponent] });
    });

    const select = (ariaLabel?: string): HTMLSelectElement => {
        const fixture = TestBed.createComponent(UiElementDropDownComponent);
        fixture.componentInstance.values = [];
        fixture.componentInstance.ariaLabel = ariaLabel;
        fixture.detectChanges();
        return fixture.nativeElement.querySelector('select');
    };

    it('names its select from ariaLabel', () => {
        expect(select('Service Flows').getAttribute('aria-label')).toBe('Service Flows');
    });

    it('renders no empty aria-label without one', () => {
        expect(select().hasAttribute('aria-label')).toBe(false);
    });

    it('dims the decorative arrow with the readonly select', () => {
        const fixture = TestBed.createComponent(UiElementDropDownComponent);
        fixture.componentInstance.values = [];
        fixture.componentInstance.readonly = true;
        fixture.detectChanges();
        expect(fixture.nativeElement.querySelector('select').classList.contains('disabled')).toBe(true);
        expect(fixture.nativeElement.querySelector('.dropdown-arrow').classList.contains('disabled')).toBe(true);
    });

    it('renders a decorative SDC arrow without intercepting select clicks', async () => {
        const control = select('Boolean value');
        const arrow = control.parentElement.querySelector('.dropdown-arrow');
        expect(arrow).not.toBeNull();
        expect(arrow.getAttribute('aria-hidden')).toBe('true');
        expect(arrow.classList.contains('sprite-new')).toBe(true);
        expect(arrow.classList.contains('arrow-up')).toBe(true);

        const filename = path.join(__dirname, '../ui-element-dropdown.component.less');
        const result = await require('less').render(fs.readFileSync(filename, 'utf8'), { filename });
        const style = document.createElement('style');
        style.textContent = result.css.replace(/\/deep\//g, '');
        document.head.appendChild(style);
        try {
            const rules = Array.from((style.sheet as CSSStyleSheet).cssRules) as CSSStyleRule[];
            const selectStyle = rules.find(rule => rule.selectorText === 'ui-element-dropdown select').style;
            const arrowStyle = rules.find(rule => rule.selectorText === 'ui-element-dropdown .dropdown-arrow').style;
            expect(selectStyle.getPropertyValue('appearance')).toBe('none');
            expect(selectStyle.padding).toBe('0 28px 0 4px');
            expect(arrowStyle.right).toBe('10px');
            expect(arrowStyle.top).toBe('50%');
            expect(arrowStyle.getPropertyValue('pointer-events')).toBe('none');
            expect(arrowStyle.transform).toBe('translateY(-50%) rotate(180deg)');
        } finally {
            document.head.removeChild(style);
        }
    });
});

describe('service-path-selector template', () => {
    it('preserves the shared dropdown arrow clearance', async () => {
        const filename = path.join(__dirname, '../../../../../pages/composition/graph/service-path-selector/service-path-selector.component.less');
        const result = await require('less').render(fs.readFileSync(filename, 'utf8'), { filename });
        const style = document.createElement('style');
        style.textContent = result.css.replace(/\/deep\//g, '');
        document.head.appendChild(style);
        try {
            const rules = Array.from((style.sheet as CSSStyleSheet).cssRules) as CSSStyleRule[];
            const selectStyle = rules.find(rule => rule.selectorText.replace(/\s+/g, ' ').trim() === '.service-path-selector .path-dropdown select').style;
            expect(selectStyle.padding).toBe('4px 28px 4px 10px');
        } finally {
            document.head.removeChild(style);
        }
    });

    it('names the Service Flows dropdown after its visible label', () => {
        const dir = path.join(__dirname, '../../../../../pages/composition/graph/service-path-selector');
        const html = fs.readFileSync(path.join(dir, 'service-path-selector.component.html'), 'utf8');
        const doc = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html');
        const label = doc.querySelector('.service-path-selector > label').textContent.replace(/:\s*$/, '');
        expect(doc.querySelector('ui-element-dropdown').getAttribute('arialabel')).toBe(label);
    });
});
