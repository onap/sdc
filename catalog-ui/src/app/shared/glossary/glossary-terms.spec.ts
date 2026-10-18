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
import {GLOSSARY_TERMS, nameKey, descKey} from './glossary-terms';

declare const require: any;
const EN_US: {[k: string]: string} = require('../../../assets/languages/en_US.json');

describe('glossary registry', () => {
    it('has a name and a description in en_US.json for every term', () => {
        const missing = GLOSSARY_TERMS
            .filter((t) => !EN_US[nameKey(t.key)] || !EN_US[descKey(t.key)])
            .map((t) => t.key);
        expect(missing).toEqual([]);
    });

    it('has no GLOSSARY_* entry in en_US.json without a registry term', () => {
        const keys = GLOSSARY_TERMS.map((t) => t.key);
        const orphans = Object.keys(EN_US)
            .filter((k) => k.indexOf('GLOSSARY_') === 0)
            .filter((k) => keys.indexOf(k.replace(/^GLOSSARY_/, '').replace(/_(NAME|DESC)$/, '')) === -1);
        expect(orphans).toEqual([]);
    });

    it('maps every alias to exactly one term', () => {
        const all: string[] = [];
        GLOSSARY_TERMS.forEach((t) => t.aliases.forEach((a) => all.push(a)));
        expect(all.filter((a, i) => all.indexOf(a) !== i)).toEqual([]);
    });

    it('uses keys that are safe in an element id', () => {
        expect(GLOSSARY_TERMS.filter((t) => !/^[A-Z0-9_]+$/.test(t.key)).map((t) => t.key)).toEqual([]);
    });
});
