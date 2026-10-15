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
import { DynamicElementModule } from '../dynamic-element.module';
import { DynamicElementComponent } from '../dynamic-element.component';
import { ValidationConfiguration } from 'app/models/validation-config';

describe('DynamicElementComponent', () => {
    let savedValidation: any;

    beforeEach(() => {
        savedValidation = ValidationConfiguration.validation;
        ValidationConfiguration.validation = JSON.parse(fs.readFileSync(
            path.join(__dirname, '../../../../../../configurations/validation.json'), 'utf8'));
        TestBed.configureTestingModule({ imports: [DynamicElementModule] });
    });

    afterEach(() => {
        ValidationConfiguration.validation = savedValidation;
    });

    const render = (type: string, ariaLabel?: string, constraints?: any[]): HTMLElement => {
        const fixture = TestBed.createComponent(DynamicElementComponent);
        Object.assign(fixture.componentInstance, { type, ariaLabel, constraints, name: 'p1', testId: 'p1' });
        fixture.detectChanges();
        fixture.detectChanges();
        return fixture.nativeElement;
    };

    it('names a text input from ariaLabel', () => {
        expect(render('string', 'cds_model_name').querySelector('input').getAttribute('aria-label'))
            .toBe('cds_model_name');
    });

    it('names a number input from ariaLabel', () => {
        expect(render('integer', 'count').querySelector('input').getAttribute('aria-label')).toBe('count');
    });

    it('names the boolean select from ariaLabel', () => {
        expect(render('boolean', 'enabled').querySelector('select').getAttribute('aria-label')).toBe('enabled');
    });

    it('names the valid-values select from ariaLabel', () => {
        const el = render('string', 'flavor', [{ validValues: ['small', 'large'] }]);
        expect(el.querySelector('select').getAttribute('aria-label')).toBe('flavor');
    });

    it('renders inputId as the id of the native control, so a <label for> can name it', () => {
        const fixture = TestBed.createComponent(DynamicElementComponent);
        Object.assign(fixture.componentInstance, { type: 'boolean', name: 'p1', testId: 'p1' });
        fixture.detectChanges();
        fixture.componentInstance.cmpRef.instance.inputId = 'pc-type';
        fixture.detectChanges();
        expect(fixture.nativeElement.querySelector('select').id).toBe('pc-type');
    });

    it('renders no empty aria-label without one', () => {
        expect(render('string').querySelector('input').hasAttribute('aria-label')).toBe(false);
    });
});

describe('ui-element-dropdown usages', () => {
    const appDir = path.join(__dirname, '../../../..');
    const templates = (dir: string): string[] => fs.readdirSync(dir).reduce((acc: string[], entry) => {
        const full = path.join(dir, entry);
        if (fs.statSync(full).isDirectory()) {
            return acc.concat(templates(full));
        }
        return entry.endsWith('.html') ? acc.concat(full) : acc;
    }, []);

    it('give every dropdown an accessible name, by aria-label or by a <label for> matching its inputId', () => {
        const unnamed = templates(appDir)
            .filter(file => fs.readFileSync(file, 'utf8').indexOf('<ui-element-dropdown') !== -1)
            .reduce((acc: string[], file) => {
                const doc = new DOMParser().parseFromString(`<body>${fs.readFileSync(file, 'utf8')}</body>`, 'text/html');
                const labelFor = Array.from(doc.querySelectorAll('label'))
                    .map(label => label.getAttribute('for') || label.getAttribute('[attr.for]'));
                const labelled = (el: Element) => {
                    const id = el.getAttribute('inputid') || el.getAttribute('[inputid]');
                    return !!id && labelFor.indexOf(id) !== -1;
                };
                Array.from(doc.querySelectorAll('ui-element-dropdown'))
                    .filter(el => !el.hasAttribute('arialabel') && !el.hasAttribute('[arialabel]') && !labelled(el))
                    .forEach(el => acc.push(`${path.relative(appDir, file)}: ${el.outerHTML.split('>')[0]}>`));
                return acc;
            }, []);
        expect(unnamed).toEqual([]);
    });
});
