/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

const templates = (dir: string): string[] =>
    fs.readdirSync(dir).reduce((found: string[], entry: string) => {
        const full = path.join(dir, entry);
        if (fs.statSync(full).isDirectory()) {
            return found.concat(templates(full));
        }
        return entry.endsWith('.html') ? found.concat(full) : found;
    }, []);

describe('ngx-datatable accessibility opt-in', () => {
    const root = path.join(__dirname, '..');
    const tables = templates(root)
        .map((file) => {
            const doc = new DOMParser().parseFromString(`<body>${fs.readFileSync(file, 'utf8')}</body>`, 'text/html');
            return {file: path.relative(root, file), tables: Array.from(doc.querySelectorAll('ngx-datatable'))};
        })
        .filter((entry) => entry.tables.length > 0);

    it('finds the tables', () => {
        expect(tables.length).toBeGreaterThanOrEqual(14);
    });

    it('opts every ngx-datatable into sdcDatatableA11y with a name for its scroll region', () => {
        const missing = tables
            .filter((entry) => entry.tables.some((el) => !(el.getAttribute('sdcdatatablea11y') || '').trim()))
            .map((entry) => entry.file);
        expect(missing).toEqual([]);
    });
});
