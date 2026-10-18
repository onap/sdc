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
import {GlossaryService, glossaryDescriptionId} from './glossary.service';
import {translateServiceStub} from './glossary.testing';

describe('GlossaryService', () => {
    const service = new GlossaryService(translateServiceStub());
    const notLoaded = new GlossaryService(translateServiceStub({}));

    it('resolves an alias to its translated entry', () => {
        expect(service.lookup('VF')).toEqual({
            key: 'VF', abbr: 'VF', name: 'Virtual Function',
            description: expect.any(String), descriptionId: 'sdc-glossary-VF'
        });
        expect(service.lookup('S').key).toBe('SERVICE');
    });

    it('knows a term before the language file is loaded, but has no text for it yet', () => {
        expect(notLoaded.isKnown('VF')).toBe(true);
        expect(notLoaded.lookup('VF')).toBeNull();
        expect(notLoaded.tooltipText('VF')).toBeNull();
    });

    it('falls back to the raw term for unknown, empty and not-yet-translated terms', () => {
        expect(service.displayName('XYZ')).toBe('XYZ');
        expect(service.label('XYZ')).toBe('XYZ');
        expect(service.displayName('')).toBe('');
        expect(service.displayName(undefined)).toBe(undefined);
        expect(notLoaded.displayName('VF')).toBe('VF');
        expect(notLoaded.label('VF')).toBe('VF');
    });

    it('labels with the abbreviation in brackets, and without it when there is none', () => {
        expect(service.label('VFC')).toBe('Virtual Function Component (VFC)');
        expect(service.label('S')).toBe('Service');
    });

    it('builds the tooltip text from label and description', () => {
        expect(service.tooltipText('PNF')).toBe('Physical Network Function (PNF): ' + service.lookup('PNF').description);
        expect(service.tooltipText('XYZ')).toBeNull();
    });

    it('gives a description id per term key, shared by all aliases', () => {
        expect(service.descriptionId('S')).toBe(glossaryDescriptionId('SERVICE'));
        expect(service.descriptionId('Service')).toBe(service.descriptionId('S'));
        expect(service.descriptionId('XYZ')).toBeNull();
    });

    it('lists every translated entry, sorted by what the user reads first', () => {
        const all = service.all();
        expect(all.length).toBe(26);
        const sortKeys = all.map((e) => (e.abbr || e.name).toLowerCase());
        expect(sortKeys).toEqual(sortKeys.slice().sort());
        expect(notLoaded.all()).toEqual([]);
    });
});
