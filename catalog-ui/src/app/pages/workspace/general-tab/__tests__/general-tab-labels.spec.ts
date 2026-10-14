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
