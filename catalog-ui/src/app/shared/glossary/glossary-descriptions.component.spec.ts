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
import {TestBed} from '@angular/core/testing';
import {TranslateService} from '../translator/translate.service';
import {GlossaryDescriptionsComponent} from './glossary-descriptions.component';
import {GlossaryService} from './glossary.service';
import {translateServiceStub} from './glossary.testing';

describe('GlossaryDescriptionsComponent', () => {
    beforeEach(() => TestBed.configureTestingModule({
        declarations: [GlossaryDescriptionsComponent],
        providers: [GlossaryService, {provide: TranslateService, useValue: translateServiceStub()}]
    }));

    it('renders one hidden description per term, ids unique', () => {
        const fixture = TestBed.createComponent(GlossaryDescriptionsComponent);
        fixture.detectChanges();
        const root: HTMLElement = fixture.nativeElement;
        const items = Array.from(root.querySelectorAll('[id^="sdc-glossary-"]'));
        expect(items.length).toBe(26);
        const ids = items.map((el) => el.id);
        expect(ids.filter((id, i) => ids.indexOf(id) !== i)).toEqual([]);
        expect(root.querySelector('#sdc-glossary-VF').textContent.trim())
            .toBe(TestBed.get(GlossaryService).tooltipText('VF'));
        expect((root.firstElementChild as HTMLElement).hidden).toBe(true);
    });
});
