/*
 * Copyright (C) 2026 Deutsche Telekom AG
 *
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
 */
import en from 'i18nJson';
import {
    GLOSSARY_TERMS,
    nameKey,
    descKey
} from 'nfvo-components/term/glossaryTerms.js';

const catalogUi = require('../../../../catalog-ui/src/assets/languages/en_US.json');

describe('onboarding glossary', () => {
    it('has a name and description for every term', () => {
        const missing = GLOSSARY_TERMS.filter(
            t => !en[nameKey(t.key)] || !en[descKey(t.key)]
        );
        expect(missing.map(t => t.key)).toEqual([]);
    });

    it('words shared terms exactly as catalog-ui does', () => {
        const shared = GLOSSARY_TERMS.filter(
            t => catalogUi['GLOSSARY_' + t.key + '_NAME']
        );
        expect(shared.map(t => t.key).sort()).toEqual([
            'CSAR',
            'HEAT',
            'VFC',
            'VLM',
            'VNF',
            'VSP'
        ]);
        shared.forEach(t => {
            expect(en[nameKey(t.key)]).toBe(
                catalogUi['GLOSSARY_' + t.key + '_NAME']
            );
            expect(en[descKey(t.key)]).toBe(
                catalogUi['GLOSSARY_' + t.key + '_DESC']
            );
        });
    });
});
