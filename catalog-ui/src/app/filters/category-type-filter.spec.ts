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
 * Characterization test for the AngularJS `categoryTypeFilter` filter (SDC-4829 Phase 10-12).
 *
 * Filters a list of category objects down to those belonging to the selected component types.
 * Rules, from the source:
 *  - If RESOURCE is NOT in selectedType but resource sub-types ARE selected, RESOURCE is added.
 *  - If, after that, selectedType is empty, the full input list is returned unchanged.
 *  - Otherwise the allowed set is the union of `cacheService.get(type.toLowerCase()+'Categories')`
 *    for each selected type; a category is kept only if it is (by reference) in that union.
 *
 * The reference-identity match is a load-bearing detail: the filter keeps the SAME objects the
 * cache holds, so the test seeds the cache with the very objects it passes in.
 */
import {CategoryTypeFilter} from './category-type-filter';

describe('CategoryTypeFilter (AngularJS characterization)', () => {
    const resourceCat = {name: 'ResourceCat'};
    const serviceCat = {name: 'ServiceCat'};
    const orphanCat = {name: 'OrphanCat'};

    const makeFilter = () => {
        const cache: {[k: string]: any} = {
            resourceCategories: [resourceCat],
            serviceCategories: [serviceCat],
            productCategories: [],
        };
        const cacheServiceMock = {get: (key: string) => cache[key]};
        return new CategoryTypeFilter(cacheServiceMock as any) as any;
    };

    it('returns all categories when no type is selected', () => {
        const filter = makeFilter();
        const all = [resourceCat, serviceCat, orphanCat];
        expect(filter(all, [], [])).toBe(all);
    });

    it('keeps only categories belonging to the selected type', () => {
        const filter = makeFilter();
        const all = [resourceCat, serviceCat, orphanCat];
        expect(filter(all, ['Resource'], [])).toEqual([resourceCat]);
    });

    it('unions categories across multiple selected types', () => {
        const filter = makeFilter();
        const all = [resourceCat, serviceCat, orphanCat];
        expect(filter(all, ['Resource', 'Service'], [])).toEqual([resourceCat, serviceCat]);
    });

    it('adds RESOURCE implicitly when resource sub-types are selected without RESOURCE', () => {
        const filter = makeFilter();
        const all = [resourceCat, serviceCat, orphanCat];
        expect(filter(all, ['Service'], ['VF'])).toEqual([resourceCat, serviceCat]);
    });

    it('drops categories that are in no selected type bucket', () => {
        const filter = makeFilter();
        expect(filter([orphanCat], ['Resource'], [])).toEqual([]);
    });
});
