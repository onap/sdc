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
 * Characterization test for the AngularJS `stringToDate` filter (SDC-4829 Phase 10-12).
 *
 * The filter parses SDC's " UTC"-suffixed timestamp strings into a Date by:
 *   - stripping the literal " UTC" suffix,
 *   - turning the FIRST space (date/time separator) into 'T',
 *   - appending '+00:00' so the value is interpreted as UTC regardless of the host TZ.
 * A falsy input returns undefined (no Date constructed).
 */
import {StringToDateFilter} from './string-to-date-filter';

describe('StringToDateFilter (AngularJS characterization)', () => {
    let toDate: (date: any) => Date | undefined;

    beforeEach(() => {
        toDate = new StringToDateFilter() as any;
    });

    it('parses a " UTC"-suffixed timestamp as a UTC instant', () => {
        const d = toDate('2020-01-15 10:30:00 UTC');
        expect(d).toBeInstanceOf(Date);
        // TZ-independent assertion: the instant is fixed to UTC by the +00:00 suffix.
        expect((d as Date).toISOString()).toBe('2020-01-15T10:30:00.000Z');
    });

    it('parses a timestamp without the " UTC" suffix (still pinned to +00:00)', () => {
        const d = toDate('2021-12-31 23:59:59');
        expect((d as Date).toISOString()).toBe('2021-12-31T23:59:59.000Z');
    });

    it('returns undefined for a falsy input', () => {
        expect(toDate(undefined)).toBeUndefined();
        expect(toDate('')).toBeUndefined();
        expect(toDate(null)).toBeUndefined();
    });
});
