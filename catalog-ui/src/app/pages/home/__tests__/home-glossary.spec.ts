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
declare const require: any;
const fs = require('fs');
const path = require('path');

const html: string = fs.readFileSync(path.join(__dirname, '..', 'home.component.html'), 'utf8');
const doc = new DOMParser().parseFromString('<body>' + html + '</body>', 'text/html');

describe('dashboard Add/Import buttons explain their acronym', () => {
    const cases: Array<[string, string]> = [
        ['[testId="createResourceButton"]', 'VF'],
        ['[testId="createCRButton"]', 'CR'],
        ['[testId="createPNFButton"]', 'PNF'],
        ['[testId="importVFCbutton"]', 'VFC'],
        ['[data-tests-id="importButtonsVSP"]', 'VSP'],
        ['[testId="importDCAE"]', 'DCAE']
    ];
    cases.forEach(([selector, term]) => {
        it(selector + ' carries sdcTerm="' + term + '" and keeps its text', () => {
            const button = doc.querySelector(selector);
            expect(button.getAttribute('sdcterm')).toBe(term);
            expect(button.getAttribute('text')).toMatch(new RegExp('\\b' + term + '\\b'));
        });
    });
});
