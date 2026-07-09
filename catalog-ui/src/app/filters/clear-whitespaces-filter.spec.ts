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
 * Characterization test for the AngularJS `clearWhiteSpaces` filter (SDC-4829 Phase 10-12).
 * NOTE: unlike `trim`, this filter removes EVERY space character, including interior ones,
 * via `replace(/ /g, '')`. It only strips the literal space char (U+0020) — not tabs/newlines.
 */
import {ClearWhiteSpacesFilter} from './clear-whitespaces-filter';

describe('ClearWhiteSpacesFilter (AngularJS characterization)', () => {
    let clear: (text: any) => any;

    beforeAll(() => {
        (global as any).angular = {isString: (v: any) => typeof v === 'string'};
    });

    afterAll(() => {
        delete (global as any).angular;
    });

    beforeEach(() => {
        clear = new ClearWhiteSpacesFilter() as any;
    });

    it('removes ALL spaces, including interior ones', () => {
        expect(clear('a b c')).toBe('abc');
        expect(clear('  hello  world  ')).toBe('helloworld');
    });

    it('only removes the literal space char, NOT tabs or newlines', () => {
        expect(clear('a\tb\nc')).toBe('a\tb\nc');
    });

    it('returns an empty string unchanged', () => {
        expect(clear('')).toBe('');
    });

    it('returns non-string input unchanged (guard branch)', () => {
        expect(clear(undefined)).toBeUndefined();
        expect(clear(null)).toBeNull();
        expect(clear(7 as any)).toBe(7);
    });
});
