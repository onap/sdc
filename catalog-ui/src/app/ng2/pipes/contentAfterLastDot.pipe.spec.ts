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
import {ContentAfterLastDotPipe} from './contentAfterLastDot.pipe';

describe('ContentAfterLastDotPipe', () => {
    let pipe: ContentAfterLastDotPipe;

    beforeEach(() => {
        pipe = new ContentAfterLastDotPipe();
    });

    it('returns the segment after the last dot', () => {
        expect(pipe.transform('org.openecomp.datatypes.heat.network.AllocationPool')).toBe('AllocationPool');
    });

    it('returns the value unchanged when there is no dot', () => {
        expect(pipe.transform('string')).toBe('string');
    });

    // BUG 1 (SDC-4829): a new map/list whose entry-schema type is not yet chosen feeds this pipe undefined
    // (dynamic-property.component.html line 65). Without the guard, value.split('.') throws and blanks the
    // whole value editor. The pipe must degrade to '' instead of crashing.
    it('returns "" for undefined without throwing (create-new complex property)', () => {
        expect(() => pipe.transform(undefined)).not.toThrow();
        expect(pipe.transform(undefined)).toBe('');
    });

    it('returns "" for null without throwing', () => {
        expect(() => pipe.transform(null)).not.toThrow();
        expect(pipe.transform(null)).toBe('');
    });
});
