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
 * Characterization test for the AngularJS `trim` filter (SDC-4829 Phase 10-12 safety net).
 * Encodes the CURRENT observable behaviour so the Angular pipe that replaces it in Phase 12
 * must reproduce identical output. No production code is changed by this spec.
 *
 * The filter's constructor RETURNS the filter function, so `new TrimFilter()` is the callable.
 * It uses the AngularJS `angular.isString` global, which is absent under Jest — provided as a
 * minimal mock below (mirrors the property-form-modal.component.spec.ts pattern).
 */
import {TrimFilter} from './trim-filter';

describe('TrimFilter (AngularJS characterization)', () => {
    let trim: (text: any) => any;

    beforeAll(() => {
        (global as any).angular = {isString: (v: any) => typeof v === 'string'};
    });

    afterAll(() => {
        delete (global as any).angular;
    });

    beforeEach(() => {
        trim = new TrimFilter() as any;
    });

    it('removes leading and trailing whitespace', () => {
        expect(trim('  hello  ')).toBe('hello');
    });

    it('collapses tabs/newlines at the edges but preserves interior whitespace', () => {
        expect(trim('\t\n  a b c \n')).toBe('a b c');
    });

    it('returns an empty string unchanged', () => {
        expect(trim('')).toBe('');
    });

    it('returns a string with no surrounding whitespace unchanged', () => {
        expect(trim('hello')).toBe('hello');
    });

    it('returns non-string input unchanged (guard branch)', () => {
        expect(trim(undefined)).toBeUndefined();
        expect(trim(null)).toBeNull();
        expect(trim(42 as any)).toBe(42);
    });
});
