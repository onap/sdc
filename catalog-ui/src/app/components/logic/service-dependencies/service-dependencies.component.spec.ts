/*
 * Copyright (C) 2026 Deutsche Telekom AG.
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

describe('ServiceDependenciesComponent template', () => {
    // tslint:disable-next-line:no-var-requires
    const template: string = require('./service-dependencies.component.html');
    const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');

    it('hides the directive selector while read-only', () => {
        expect(doc.querySelector('select-directives').getAttribute('*ngif')).toBe('!readonly && (!isDependent || isEditable)');
    });

    it('hides both Add Node Filter buttons while read-only instead of disabling them', () => {
        const adds = Array.from(doc.querySelectorAll('sdc-button')).filter((el) => el.getAttribute('[testId]') === `'add-rule-button'`);
        expect(adds.length).toBe(2);
        adds.forEach((add) => {
            expect(add.hasAttribute('[disabled]')).toBe(false);
            expect(add.parentElement.getAttribute('*ngIf')).toBe('!readonly');
        });
    });
});
