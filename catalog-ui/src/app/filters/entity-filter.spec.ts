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
 * Characterization test for the AngularJS `entityFilter` filter (SDC-4829 Phase 10-12).
 *
 * DELETION CANDIDATE: this ng1 filter has zero remaining template usages; the Angular
 * `EntityFilterPipe` (ng2/pipes/entity-filter.pipe.ts, already spec'd) supersedes it. This spec
 * pins the ng1 behaviour so the deletion in Phase 12 can be proven behaviour-preserving against
 * its ng2 successor rather than taken on faith. See directive-migration-inventory.md.
 *
 * Covers the four independent narrowing stages: type, category, status, distributed. The filter
 * uses `angular.forEach`, absent under Jest, so a minimal mock is installed.
 */
import {EntityFilter} from './entity-filter';

describe('EntityFilter (AngularJS characterization, deletion candidate)', () => {
    let entityFilter: (components: any[], filter: any) => any[];

    beforeAll(() => {
        (global as any).angular = {
            forEach: (coll: any[], cb: (v: any) => void) => (coll || []).forEach(cb),
        };
    });

    afterAll(() => {
        delete (global as any).angular;
    });

    beforeEach(() => {
        entityFilter = new EntityFilter() as any;
    });

    const svc = {componentType: 'SERVICE', isResource: () => false, lifecycleState: 'CERTIFIED'};
    const vf = {
        componentType: 'RESOURCE',
        isResource: () => true,
        getComponentSubType: () => 'VF',
        lifecycleState: 'NOT_CERTIFIED_CHECKOUT',
    };

    it('returns the input unchanged when no filter criteria are set', () => {
        const all = [svc, vf];
        expect(entityFilter(all, {})).toBe(all);
    });

    it('filters by component type (first-capital match)', () => {
        const result = entityFilter([svc, vf], {selectedComponentTypes: ['Service'], selectedResourceSubTypes: []});
        expect(result).toEqual([svc]);
    });

    it('filters resources by sub-type when Resource type is not itself selected', () => {
        const result = entityFilter([svc, vf], {selectedComponentTypes: ['Service'], selectedResourceSubTypes: ['VF']});
        expect(result).toContain(vf);
    });

    it('filters by lifecycle status', () => {
        const result = entityFilter([svc, vf], {selectedStatuses: ['CERTIFIED']});
        expect(result).toEqual([svc]);
    });

    it('filters by distribution status', () => {
        const distributed = {componentType: 'SERVICE', distributionStatus: 'DISTRIBUTED', isResource: () => false};
        const notDistributed = {componentType: 'SERVICE', distributionStatus: 'DISTRIBUTION_NOT_APPROVED', isResource: () => false};
        const result = entityFilter([distributed, notDistributed], {distributed: ['DISTRIBUTED']});
        expect(result).toEqual([distributed]);
    });
});
