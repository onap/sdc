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
});

describe('service-path-selector template', () => {
    it('names the Service Flows dropdown after its visible label', () => {
        const dir = path.join(__dirname, '../../../../../pages/composition/graph/service-path-selector');
        const html = fs.readFileSync(path.join(dir, 'service-path-selector.component.html'), 'utf8');
        const doc = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html');
        const label = doc.querySelector('.service-path-selector > label').textContent.replace(/:\s*$/, '');
        expect(doc.querySelector('ui-element-dropdown').getAttribute('arialabel')).toBe(label);
    });
});
