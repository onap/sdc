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
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {FormsModule} from '@angular/forms';
import {CacheService} from '../../services/cache.service';
import {GlossaryService} from '../../shared/glossary/glossary.service';
import {translateServiceStub} from '../../shared/glossary/glossary.testing';
import {TranslateService} from '../../shared/translator/translate.service';
import {GlossaryPageComponent} from './glossary-page.component';

describe('GlossaryPageComponent', () => {
    const create = () => {
        TestBed.configureTestingModule({
            imports: [FormsModule],
            declarations: [GlossaryPageComponent],
            providers: [GlossaryService, {provide: TranslateService, useValue: translateServiceStub()},
                {provide: CacheService, useValue: {get: () => '1.19.0'}}],
            schemas: [NO_ERRORS_SCHEMA]
        });
        const fixture = TestBed.createComponent(GlossaryPageComponent);
        fixture.detectChanges();
        return fixture;
    };
    const rows = (root: HTMLElement) => Array.from(root.querySelectorAll('[data-tests-id^="glossary-term-"]'));

    it('lists every term with abbreviation, name and description, and has one h1', () => {
        const root: HTMLElement = create().nativeElement;
        expect(rows(root).length).toBe(26);
        expect(root.querySelectorAll('h1').length).toBe(1);
        expect(root.querySelector('[data-tests-id="glossary-term-VF"]').textContent).toContain('Virtual Function');
        expect(root.querySelector('main#main-content')).not.toBeNull();
    });

    it('filters by abbreviation, name or description, case-insensitively', () => {
        const fixture = create();
        fixture.componentInstance.filterText = 'radio';
        fixture.detectChanges();
        const keys = rows(fixture.nativeElement).map((r) => r.getAttribute('data-tests-id'));
        expect(keys).toContain('glossary-term-RU');
        expect(keys).not.toContain('glossary-term-VF');
    });

    it('has a labelled filter input', () => {
        const input = create().nativeElement.querySelector('[data-tests-id="glossary-filter"]');
        expect(input.getAttribute('aria-label')).toBe('Filter glossary');
    });

    it('separates the abbreviation from the name in the text a screen reader reads', () => {
        const dt = create().nativeElement.querySelector('[data-tests-id="glossary-term-VF"] dt');
        expect(dt.textContent.replace(/\s+/g, ' ').trim()).toBe('VF Virtual Function');
    });
});
