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

import { Point } from 'app/models/graph/point';
import { CompositionGraphGeneralUtils } from './composition-graph-general-utils';
import { CompositionGraphPaletteUtils } from './composition-graph-palette-utils';

describe('composition graph palette utils', () => {

    let generalGraphUtils: Partial<CompositionGraphGeneralUtils>;
    let utils: CompositionGraphPaletteUtils;
    const canvasAt = (left: number, top: number) => <Cy.Instance><any>{
        container: () => ({getBoundingClientRect: () => ({left, top})})
    };

    beforeEach(() => {
        generalGraphUtils = {isPaletteDropValid: jest.fn().mockReturnValue(true)};
        utils = new CompositionGraphPaletteUtils(<CompositionGraphGeneralUtils>generalGraphUtils,
            null, null, null, null, null, null, null, null, null, null);
    });

    it('should reject a drag over the palette, wherever the palette ends', () => {
        expect(utils.isDragValid(canvasAt(500, 143), new Point(450, 300))).toBe(false);
        expect(generalGraphUtils.isPaletteDropValid).not.toHaveBeenCalled();
    });

    it('should reject a drag over the header', () => {
        expect(utils.isDragValid(canvasAt(500, 143), new Point(600, 100))).toBe(false);
    });

    it('should check the drop in coordinates relative to the canvas', () => {
        const cy = canvasAt(500, 143);
        expect(utils.isDragValid(cy, new Point(600, 243))).toBe(true);
        expect(generalGraphUtils.isPaletteDropValid).toHaveBeenCalledWith(cy, {x1: 80, y1: 80, x2: 120, y2: 120});
    });
});
