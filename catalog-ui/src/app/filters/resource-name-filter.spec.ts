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
 * Characterization test for the AngularJS `resourceName` filter (SDC-4829 Phase 10-12).
 *
 * The filter strips a fixed chain of TOSCA namespace prefixes from a resource type name to
 * produce a short display name. It splits on each of these tokens in turn and keeps the LAST
 * segment: 'tosca.nodes.', 'network.', 'relationships.', 'org.openecomp.', 'resource.nfv.',
 * 'nodes.module.', 'cp.', 'vl.'. Because it keeps the tail after each split, a name matching
 * several tokens is reduced by the last-matching token in that ordered chain.
 *
 * Falsy input: the function has no explicit return for a falsy `name`, so it returns undefined.
 */
import {ResourceNameFilter} from './resource-name-filter';

describe('ResourceNameFilter (AngularJS characterization)', () => {
    let resourceName: (name: any) => any;

    beforeEach(() => {
        resourceName = new ResourceNameFilter() as any;
    });

    it('strips the tosca.nodes. prefix', () => {
        expect(resourceName('tosca.nodes.Compute')).toBe('Compute');
    });

    it('strips the org.openecomp. prefix', () => {
        expect(resourceName('org.openecomp.resource.VF')).toBe('resource.VF');
    });

    it('keeps the tail after the last-matching token in the chain', () => {
        // contains both 'org.openecomp.' and 'resource.nfv.' -> reduced past resource.nfv.
        expect(resourceName('org.openecomp.resource.nfv.VNF')).toBe('VNF');
    });

    it('returns the name unchanged when no known prefix matches', () => {
        expect(resourceName('SimpleName')).toBe('SimpleName');
    });

    it('returns undefined for a falsy name (no explicit return branch)', () => {
        expect(resourceName(undefined)).toBeUndefined();
        expect(resourceName('')).toBeUndefined();
    });
});
