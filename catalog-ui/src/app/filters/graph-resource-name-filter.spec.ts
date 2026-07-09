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
 * Characterization test for the AngularJS `graphResourceName` filter (SDC-4829 Phase 10-12).
 *
 * The filter shortens a node name to fit a fixed pixel width on the composition canvas:
 *  - measures the name with a 13px Arial canvas 2d context;
 *  - if width <= 67px, returns the name unchanged;
 *  - otherwise trims 3 chars, then removes one char at a time until width <= 59px, and appends '...'.
 *
 * jsdom's canvas has no real text-metrics engine, so this test injects a DETERMINISTIC width
 * model (width == characters * 10px) by stubbing `HTMLCanvasElement.prototype.getContext`. That
 * lets us assert the width-driven branch logic without depending on a font renderer. The Angular
 * replacement must keep the same 67/59 thresholds and '...' suffix.
 */
import {GraphResourceNameFilter} from './graph-resource-name-filter';

describe('GraphResourceNameFilter (AngularJS characterization)', () => {
    let getContextSpy: jest.SpyInstance;

    beforeEach(() => {
        // Deterministic metrics: 10px per character.
        getContextSpy = jest
            .spyOn(HTMLCanvasElement.prototype as any, 'getContext')
            .mockReturnValue({
                font: '',
                measureText: (text: string) => ({width: (text ? text.length : 0) * 10}),
            } as any);
    });

    afterEach(() => {
        getContextSpy.mockRestore();
    });

    const graphResourceName = () => new GraphResourceNameFilter() as any;

    it('returns a short name unchanged (width <= 67px)', () => {
        // 6 chars * 10px = 60px <= 67px -> unchanged.
        expect(graphResourceName()('Router')).toBe('Router');
    });

    it('returns a name exactly on the boundary unchanged', () => {
        // 'x'.repeat(6) -> 60px; still <= 67px.
        expect(graphResourceName()('xxxxxx')).toBe('xxxxxx');
    });

    it('truncates an over-wide name and appends an ellipsis', () => {
        // 12 chars * 10 = 120px > 67px. Drop 3 -> 9 chars (90px) still > 59px; keep removing
        // until width <= 59px: 5 chars = 50px. Result is the first 5 chars + '...'.
        const result = graphResourceName()('LongNodeName');
        expect(result.endsWith('...')).toBe(true);
        expect(result).toBe('LongN...');
    });
});
