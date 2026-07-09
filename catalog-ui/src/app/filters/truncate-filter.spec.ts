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
 * Characterization test for the AngularJS `truncate` filter (SDC-4829 Phase 10-12).
 *
 * Behaviour, reverse-engineered from the source (word-boundary truncation):
 *  - str.length <= length -> returned unchanged.
 *  - otherwise, scan BACKWARDS starting at index `length` (decrement-then-read, so str[length]
 *    itself is never inspected) for the nearest preceding space:
 *      - space found at index > 0 -> str.substring(0, spaceIndex) + '...'
 *      - scan reaches index 0 (no space in [1..length-1]) -> the string is returned UNCHANGED.
 *
 * That last case is a code quirk worth pinning: the branch reads
 *   return (index === 0) ? str : str.substring(0, length - 3) + '...';
 * from INSIDE an `if (index === 0)`, so the ternary is always true and the `length - 3` cut is
 * dead code. The Angular pipe must reproduce this exact (arguably buggy) behaviour, or the
 * migration is a behaviour change that this spec will flag.
 */
import {TruncateFilter} from './truncate-filter';

describe('TruncateFilter (AngularJS characterization)', () => {
    let truncate: (str: string, length: number) => string;

    beforeEach(() => {
        truncate = new TruncateFilter() as any;
    });

    it('returns the string unchanged when its length <= the limit', () => {
        expect(truncate('hello', 10)).toBe('hello');
        expect(truncate('hello', 5)).toBe('hello');
    });

    it('truncates at the nearest preceding space and appends an ellipsis', () => {
        // "one two three" (len 13) with limit 10: backward scan from index 10 hits the space at 7.
        expect(truncate('one two three', 10)).toBe('one two...');
    });

    it('truncates at the first space found scanning back from the limit', () => {
        // "hello world foo" (len 15) limit 8: scan from index 8 back to the space at index 5.
        expect(truncate('hello world foo', 8)).toBe('hello...');
    });

    it('QUIRK: returns the FULL string unchanged when there is no interior space before the limit', () => {
        // "abcdefghij" has no spaces -> backward scan reaches index 0 -> dead-ternary returns str.
        expect(truncate('abcdefghij', 8)).toBe('abcdefghij');
    });
});
