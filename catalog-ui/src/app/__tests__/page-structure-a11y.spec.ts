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

const sources = (dir: string): string[] =>
    fs.readdirSync(dir).reduce((found: string[], entry: string) => {
        const full = path.join(dir, entry);
        if (fs.statSync(full).isDirectory()) {
            return found.concat(sources(full));
        }
        return entry.endsWith('.html') ? found.concat(full) : found;
    }, []);

const appDir = path.join(__dirname, '..');
const parse = (file: string): Document =>
    new DOMParser().parseFromString(`<body>${fs.readFileSync(file, 'utf8')}</body>`, 'text/html');
const relative = (file: string): string => path.relative(appDir, file).split(path.sep).join('/');
const templates = sources(appDir).map((file) => ({file: relative(file), doc: parse(file)}));

describe('page structure', () => {
    // Reading order has to match the visual order (WCAG 1.3.2): the fixed top bar is painted first.
    it('renders <top-nav> before <main> in every page that has both', () => {
        const pages = templates.filter(({doc}) => doc.querySelector('top-nav') && doc.querySelector('main'));
        expect(pages.length).toBeGreaterThanOrEqual(6);
        const late = pages.filter(({doc}) =>
            // tslint:disable-next-line:no-bitwise
            !(doc.querySelector('top-nav').compareDocumentPosition(doc.querySelector('main')) & Node.DOCUMENT_POSITION_FOLLOWING))
            .map(({file}) => file);
        expect(late).toEqual([]);
    });

    // Selenium waits on the *ngIf loader node, so it stays; the status region next to it is what
    // tells a screen reader the page is busy.
    ['pages/onboard-vendor/onboard-vendor.component.html',
        'pages/admin-dashboard/user-management/user-management.component.html'].forEach((file) => {
        it(`pairs the inline loader in ${file} with a persistent status region`, () => {
            const doc = templates.find((t) => t.file === file).doc;
            const loader = doc.querySelector('[data-tests-id="loader"]');
            expect(loader.getAttribute('*ngIf')).toBe('isLoading');
            const status = loader.nextElementSibling;
            expect(status.getAttribute('role')).toBe('status');
            expect(status.hasAttribute('*ngIf')).toBe(false);
            expect(status.textContent).toBe(`{{isLoading ? 'Loading' : ''}}`);
        });
    });
});
