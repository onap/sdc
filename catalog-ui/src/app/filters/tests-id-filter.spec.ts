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

/**
 * Characterization test for the AngularJS `testsId` filter (SDC-4829 Phase 10-12).
 * Builds Selenium `data-tests-id` values: replaces EVERY whitespace char with '_' and
 * lower-cases the result. Selenium selectors depend on this transform being stable, so the
 * Angular pipe replacing it must produce byte-identical ids.
 */
import {TestsIdFilter} from './tests-id-filter';

describe('TestsIdFilter (AngularJS characterization)', () => {
    let testsId: (id: any) => any;

    beforeEach(() => {
        testsId = new TestsIdFilter() as any;
    });

    it('replaces spaces with underscores and lower-cases', () => {
        expect(testsId('Create New')).toBe('create_new');
    });

    it('replaces every whitespace char (tab, newline) with underscore', () => {
        expect(testsId('a\tb\nc d')).toBe('a_b_c_d');
    });

    it('lower-cases an already underscore-free id', () => {
        expect(testsId('MyButton')).toBe('mybutton');
    });

    it('returns an empty string unchanged', () => {
        expect(testsId('')).toBe('');
    });
});
