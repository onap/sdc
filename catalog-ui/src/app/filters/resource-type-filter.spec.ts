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
 * Characterization test for the AngularJS `resourceTypeName` filter (SDC-4829 Phase 10-12).
 *
 * Registered as `resourceTypeName`; maps a raw resource-type key to its human label from the
 * cached `UIConfiguration.resourceTypes` map, falling back to the raw key when the map (or the
 * key) is absent. Depends on `Sdc.Services.CacheService`, injected via its constructor — a
 * minimal fake cache is supplied here.
 */
import {ResourceTypeFilter} from './resource-type-filter';

describe('ResourceTypeFilter (AngularJS characterization)', () => {
    const makeFilter = (uiConfiguration: any) => {
        const cacheServiceMock = {
            get: (key: string) => (key === 'UIConfiguration' ? uiConfiguration : undefined),
        };
        return new ResourceTypeFilter(cacheServiceMock as any) as any;
    };

    it('returns the mapped label when UIConfiguration.resourceTypes has the key', () => {
        const filter = makeFilter({resourceTypes: {VF: 'Virtual Function', CP: 'Connection Point'}});
        expect(filter('VF')).toBe('Virtual Function');
        expect(filter('CP')).toBe('Connection Point');
    });

    it('falls back to the raw key when the key is not in the map', () => {
        const filter = makeFilter({resourceTypes: {VF: 'Virtual Function'}});
        expect(filter('PNF')).toBe('PNF');
    });

    it('falls back to the raw key when UIConfiguration is absent', () => {
        const filter = makeFilter(undefined);
        expect(filter('VF')).toBe('VF');
    });

    it('falls back to the raw key when resourceTypes is absent', () => {
        const filter = makeFilter({});
        expect(filter('VF')).toBe('VF');
    });
});
