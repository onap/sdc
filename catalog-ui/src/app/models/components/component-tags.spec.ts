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

import {ComponentMetadata} from '../component-metadata';
import {Resource} from './resource';

// Both `tags` assignments used to be angular.copy(src, this.tags) against an array destination.
// angular.copy REFILLS such a destination (so a null/undefined source yielded [] rather than
// undefined) and preserves its identity. The lodash replacement assigns instead, so the []
// fallback has to be explicit — displayComponent does tags.join(' ') and catalog.component does
// tags.toString(), both unguarded, and would throw on undefined.
describe('Component tags copy semantics', () => {

    describe('setComponentMetadata', () => {

        const metadataWith = (tags: any): ComponentMetadata => {
            const metadata = new ComponentMetadata();
            metadata.tags = tags;
            return metadata;
        };

        it('deep-copies tags rather than aliasing the metadata', () => {
            const metadata = metadataWith(['a', 'b']);
            const resource = new Resource();
            resource.setComponentMetadata(metadata);
            expect(resource.tags).toEqual(['a', 'b']);
            expect(resource.tags).not.toBe(metadata.tags);
            resource.tags.push('c');
            expect(metadata.tags).toEqual(['a', 'b']);
        });

        // The consumers below dereference tags unguarded, so [] — never undefined — is the contract.
        [{label: 'undefined', tags: undefined}, {label: 'null', tags: null}].forEach(({label, tags}) => {
            it(`falls back to [] when metadata tags is ${label}`, () => {
                const resource = new Resource();
                resource.setComponentMetadata(metadataWith(tags));
                expect(resource.tags).toEqual([]);
                expect(() => resource.tags.join(' ')).not.toThrow();
                expect(() => resource.tags.toString()).not.toThrow();
            });
        });
    });

    describe('deserialize (copy constructor)', () => {

        it('deep-copies tags rather than aliasing the source component', () => {
            const source = new Resource();
            source.tags = ['x', 'y'];
            const copy = new Resource(undefined, source);
            expect(copy.tags).toEqual(['x', 'y']);
            expect(copy.tags).not.toBe(source.tags);
        });

        it('falls back to [] when the source component has no tags', () => {
            const source = new Resource();
            source.tags = undefined;
            const copy = new Resource(undefined, source);
            expect(copy.tags).toEqual([]);
        });
    });
});
