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

const html: string = fs.readFileSync(path.join(__dirname, 'info-tab.component.html'), 'utf8');
const doc = new DOMParser().parseFromString('<body>' + html + '</body>', 'text/html');

describe('info tab glossary terms', () => {
    ['rightTab_resourceType', 'rightTab_category', 'rightTab_subCategory'].forEach((id) => {
        it(id + ' explains glossary terms and keeps the raw tooltip for everything else', () => {
            const span = doc.querySelector('[data-tests-id="' + id + '"]');
            expect(span.getAttribute('[sdcterm]')).toBeTruthy();
            expect(span.getAttribute('[tooltipdisabled]')).toContain('isGlossaryTerm');
        });
    });
});
