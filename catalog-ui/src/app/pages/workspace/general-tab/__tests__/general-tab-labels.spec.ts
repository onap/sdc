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
import { unlabelledControls } from '../../../../../jest/a11y-helpers';

describe('general-tab template', () => {
    it('pairs every form control with a label', () => {
        const html = fs.readFileSync(path.join(__dirname, '..', 'general-tab.component.html'), 'utf8');
        expect(unlabelledControls(html)).toEqual([]);
    });
});

describe('general-tab file controls', () => {
    const html = fs.readFileSync(path.join(__dirname, '..', 'general-tab.component.html'), 'utf8');
    const doc = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html');

    // The invisible input sat under the Browse div with no handler: a dead, nameless tab stop.
    it('has no dead input type=button in the VSP import block', () => {
        expect(doc.querySelectorAll('input[type="button"]').length).toBe(0);
    });

    it('renders the VSP Browse control as a native button disabled exactly when it looks disabled', () => {
        const browse = doc.querySelector('.file-upload-browse-btn[data-tests-id="browseButton"]');
        expect(browse.tagName).toBe('BUTTON');
        expect(browse.getAttribute('type')).toBe('button');
        expect(browse.getAttribute('aria-label')).toBe('Browse VSP');
        expect(browse.getAttribute('(click)')).toBe('openOnBoardingModal()');
        expect(browse.getAttribute('[disabled]')).toBe(browse.getAttribute('[class.disabled]'));
    });

    it('renders the clear-file control as a named native button', () => {
        const clear = doc.querySelector('.i-sdc-form-file-upload-x-btn');
        expect(clear.tagName).toBe('BUTTON');
        expect(clear.getAttribute('type')).toBe('button');
        expect(clear.className).toBe('i-sdc-form-file-upload-x-btn');
        expect(clear.getAttribute('aria-label')).toBe('Clear file');
    });
});

describe('general-tab icon controls', () => {
    const html = fs.readFileSync(path.join(__dirname, '..', 'general-tab.component.html'), 'utf8');
    const doc = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html');

    [
        ['revert', 'sprite-new revert-btn', 'Revert'],
        ['update-component-icon', 'sprite-new update-component-icon', 'Update icon'],
    ].forEach(([testId, className, name]) => {
        it(`renders ${testId} as a named native sprite button`, () => {
            const el = doc.querySelector(`[data-tests-id="${testId}"]`);
            expect(el.tagName).toBe('BUTTON');
            expect(el.getAttribute('type')).toBe('button');
            expect(el.className).toBe(className);
            expect(el.getAttribute('aria-label')).toBe(name);
        });
    });

    it('deletes a tag through a button named after the tag', () => {
        const del = doc.querySelector('[data-tests-id="i-sdc-tag-delete"]');
        expect(del.tagName).toBe('BUTTON');
        expect(del.getAttribute('type')).toBe('button');
        expect(del.getAttribute('[attr.aria-label]')).toBe(`'Delete tag ' + tag`);
    });
});
