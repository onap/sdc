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

import {ComponentMetadata} from './component-metadata';

// deserialize() used to do `this.tags = angular.copy(response.tags, this.tags)`. ComponentMetadata has
// NO constructor, so this.tags was always undefined at that point — and angular.copy treats a falsy
// destination as the 1-arg form, returning a fresh copy. It was therefore never really a 2-arg copy.
describe('ComponentMetadata.deserialize tags', () => {

    it('deep-copies the tags array rather than aliasing the response', () => {
        const response: any = {tags: ['a', 'b']};
        const metadata = new ComponentMetadata().deserialize(response);
        expect(metadata.tags).toEqual(['a', 'b']);
        expect(metadata.tags).not.toBe(response.tags);
        metadata.tags.push('c');
        expect(response.tags).toEqual(['a', 'b']);
    });

    it('leaves tags undefined when the response omits them (angular.copy(undefined) parity)', () => {
        const metadata = new ComponentMetadata().deserialize({});
        expect(metadata.tags).toBeUndefined();
    });

    it('does not throw when deserializing onto a fresh instance whose tags is unset', () => {
        expect(() => new ComponentMetadata().deserialize({tags: ['x']})).not.toThrow();
    });
});
