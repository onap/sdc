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
 * Specs for `ResourceNamePipe` (SDC-4829 Phase 12).
 *
 * This pipe is the single Angular home of the TOSCA short-display-name rule. It absorbs the
 * behaviour of the deleted AngularJS `resourceName` filter (`filters/resource-name-filter.ts`),
 * whose chained `_.last(split(prefix))` reduction these specs pin, plus the first-letter
 * capitalization the pipe has applied since SDC-4220.
 *
 * Before Phase 12 the pipe's prefix-stripping was inoperative: the eight prefixes were joined
 * into ONE regex (`/tosca\.nodes\..*network\.…vl\./`) that required every prefix to appear, in
 * order, in the same string — so no real name ever matched and the pipe only capitalized. The
 * "strips ... " cases below are the regression tests for that fix.
 */
import {ResourceNamePipe} from './resource-name.pipe';

describe('ResourceNamePipe', () => {
    let pipe: ResourceNamePipe;

    beforeEach(() => {
        pipe = new ResourceNamePipe();
    });

    describe('getDisplayName', () => {
        it('strips the tosca.nodes. prefix', () => {
            expect(ResourceNamePipe.getDisplayName('tosca.nodes.Compute')).toBe('Compute');
        });

        it('strips the org.openecomp. + resource.nfv. chain', () => {
            expect(ResourceNamePipe.getDisplayName('org.openecomp.resource.nfv.VnfMain')).toBe('VnfMain');
        });

        it('keeps only the segment after the LAST matching prefix in the chain', () => {
            // 'network.' comes after 'tosca.nodes.' in the chain, so it wins.
            expect(ResourceNamePipe.getDisplayName('tosca.nodes.network.Port')).toBe('Port');
        });

        it('strips the cp. prefix', () => {
            expect(ResourceNamePipe.getDisplayName('extcp.Port')).toBe('Port');
        });

        it('strips the vl. prefix', () => {
            expect(ResourceNamePipe.getDisplayName('myvl.Net')).toBe('Net');
        });

        it('strips the nodes.module. prefix', () => {
            expect(ResourceNamePipe.getDisplayName('tosca.nodes.nodes.module.Mod')).toBe('Mod');
        });

        it('strips the relationships. prefix', () => {
            expect(ResourceNamePipe.getDisplayName('tosca.relationships.DependsOn')).toBe('DependsOn');
        });

        it('leaves a name with no known prefix intact apart from capitalization', () => {
            expect(ResourceNamePipe.getDisplayName('ciService1234')).toBe('CiService1234');
        });

        it('capitalizes the first letter of the stripped short name', () => {
            expect(ResourceNamePipe.getDisplayName('tosca.nodes.compute')).toBe('Compute');
        });

        it('leaves an already-capitalized name unchanged', () => {
            expect(ResourceNamePipe.getDisplayName('MyVF')).toBe('MyVF');
        });

        it('does not strip a prefix that only partially matches', () => {
            // 'resource.vf.' is NOT in the prefix chain, so only 'org.openecomp.' applies.
            expect(ResourceNamePipe.getDisplayName('org.openecomp.resource.vf.Vfw')).toBe('Resource.vf.Vfw');
        });
    });

    describe('transform', () => {
        it('delegates to getDisplayName', () => {
            expect(pipe.transform('tosca.nodes.Compute')).toBe('Compute');
        });

        it('returns undefined for a falsy value rather than throwing', () => {
            expect(pipe.transform('')).toBeUndefined();
            expect(pipe.transform(undefined)).toBeUndefined();
            expect(pipe.transform(null)).toBeUndefined();
        });
    });
});
