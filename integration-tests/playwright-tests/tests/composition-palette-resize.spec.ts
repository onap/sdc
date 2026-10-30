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
 * Resizable composition palette.
 *
 * WHAT THIS GUARDS — the three things outside the palette that have to follow its width, none of
 * which a Jest spec can see because each depends on real layout:
 *
 *   - the workspace top bar, which `workspace.less` positions absolutely over the composition page
 *     at `--composition-palette-width`; without it the bar covers the top of a widened palette;
 *   - the cytoscape canvas, which only re-measures its container on `cy.resize()`; a stale canvas
 *     overflows its box and is drawn under the right-hand panel;
 *   - the stored width, so the next workspace opens at the width the user chose.
 *
 * HOW TO RUN: see README.md — `npx playwright test composition-palette-resize`.
 */

import { test, expect, settles, gotoWorkspaceTab } from './fixtures/sdc';

const HANDLE = '[data-tests-id="palette-resize-handle"]';
const DEFAULT_WIDTH = 244;
const DRAG_BY = 200;

async function measure(page) {
    return page.evaluate(() => {
        const rect = (sel: string) => document.querySelector(sel)!.getBoundingClientRect();
        const palette = rect('.composition-palette-component');
        const hitX = palette.right + 40;
        const hit = document.elementFromPoint(hitX, 400) as HTMLElement;
        return {
            palette: Math.round(palette.width),
            topBarLeft: Math.round(rect('.sdc-workspace-top-bar').left),
            canvasBox: Math.round(rect('.sdc-composition-graph-wrapper').width),
            cyCanvas: Math.round(rect('.sdc-composition-graph-wrapper canvas').width),
            hitRightOfPalette: hit ? hit.tagName.toLowerCase() : 'none',
        };
    });
}

test.describe('Composition palette resize', () => {

    test('dragging the handle widens the palette, and the top bar and canvas follow',
        async ({ sdcPage, api }) => {
            const vf = await api.createVf('PwPalette');
            await sdcPage.evaluate(() => localStorage.removeItem('sdc.palette.width'));
            await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'composition' });
            await settles(sdcPage);
            const handle = sdcPage.locator(HANDLE);
            await expect(handle).toBeVisible({ timeout: 30_000 });
            await expect.poll(async () => (await measure(sdcPage)).hitRightOfPalette,
                { timeout: 20_000, message: 'the canvas never became hit-testable' })
                .toBe('canvas');

            const before = await measure(sdcPage);
            expect(before.palette).toBe(DEFAULT_WIDTH);

            const box = (await handle.boundingBox())!;
            await sdcPage.mouse.move(box.x + box.width / 2, box.y + 300);
            await sdcPage.mouse.down();
            await sdcPage.mouse.move(box.x + box.width / 2 + DRAG_BY, box.y + 300, { steps: 10 });
            await sdcPage.mouse.up();

            const after = await measure(sdcPage);
            expect(after.palette).toBe(DEFAULT_WIDTH + DRAG_BY);
            await expect(handle).toHaveAttribute('aria-valuenow', String(DEFAULT_WIDTH + DRAG_BY));
            expect(after.topBarLeft, 'the top bar did not move with the palette edge')
                .toBe(before.topBarLeft + DRAG_BY);
            expect(after.cyCanvas, 'cytoscape was not resized to its narrower box').toBe(after.canvasBox);
            expect(after.hitRightOfPalette, 'nothing hit-testable just right of the new palette edge')
                .toBe('canvas');

            await sdcPage.reload();
            await settles(sdcPage);
            await expect(handle).toHaveAttribute('aria-valuenow', String(DEFAULT_WIDTH + DRAG_BY),
                { timeout: 30_000 });

            await handle.dblclick();
            await expect(handle).toHaveAttribute('aria-valuenow', String(DEFAULT_WIDTH));
            expect((await measure(sdcPage)).topBarLeft).toBe(before.topBarLeft);
        });
});
