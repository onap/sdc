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
 * Composition full-bleed layout.
 *
 * WHAT THIS GUARDS — a layout that is driven entirely by a CSS class on <body>, which is the
 * one category of breakage every other gate in this repo is blind to:
 *
 *   ui-router puts `bodyClass: 'composition'` in the state's `data` (app.ts:381-383); the run
 *   block copies it to `$rootScope.bodyClass` (app.ts:726-729) and `index.html:29`'s
 *   `<body data-ng-class="bodyClass">` paints it. `workspace.less:331-336` then keys the
 *   full-bleed geometry off `.composition .workspace-child-view` — reclaiming the 242px sidebar
 *   gutter and the 53px action-bar strip that every other tab reserves.
 *
 *   Phase 13 CR 2 deletes both the ui-router states and the `$stateChangeSuccess` run block that
 *   sets `bodyClass`. If the replacement forgets it — or sets it on the wrong element, or a tick
 *   too late — the canvas renders inside a 1678px box indented 242px, with the palette shoved off
 *   under the sidebar. Nothing detects that: Jest never mounts <body>, AOT has no opinion on CSS
 *   class strings, and Selenium's waits are all EXACT-class *existence* checks that pass on a
 *   mislaid canvas exactly as they do on a correct one.
 *
 * WHY GEOMETRY AND HIT-TESTING, not a screenshot: 242px of unwanted indent is the kind of
 * difference that reads as "looks fine" in a screenshot review. `elementFromPoint` additionally
 * proves the canvas is genuinely reachable at x=300 rather than merely sized to include it —
 * an overlay that swallows clicks is a real ngUpgrade failure mode and geometry alone misses it.
 *
 * Measured on master (1920x1080 viewport, Docker stack), which is what the numbers below encode:
 *   general      body='ng-scope general'      child-view 242,103 1678x977   sidebar 0,50 242x1030
 *   composition  body='ng-scope composition'  child-view   0,50 1920x1030   sidebar ABSENT
 *
 * HOW TO RUN: see README.md — `npx playwright test composition-geometry`.
 */

import { test, expect, SEL, settles, gotoWorkspaceTab, currentRoute } from './fixtures/sdc';

/** The element the full-bleed rule targets. Not in SEL: nothing outside this spec asserts on it. */
const CHILD_VIEW = '.workspace-child-view';

/** The workspace shell's left tab rail, suppressed on composition by `*ngIf="!isComposition"`. */
const LEFT_SIDEBAR = '.w-sdc-left-sidebar';

interface Geometry {
    bodyClass: string;
    childView: { left: number; top: number; width: number; height: number } | null;
    sidebarPresent: boolean;
    hitAt300x400: string;
}

async function measure(page): Promise<Geometry> {
    return page.evaluate(({ childViewSel, sidebarSel }) => {
        const cv = document.querySelector(childViewSel) as HTMLElement;
        const r = cv && cv.getBoundingClientRect();
        const hit = document.elementFromPoint(300, 400) as HTMLElement;
        return {
            bodyClass: document.body.className,
            childView: r ? {
                left: Math.round(r.left), top: Math.round(r.top),
                width: Math.round(r.width), height: Math.round(r.height),
            } : null,
            sidebarPresent: !!document.querySelector(sidebarSel),
            hitAt300x400: hit ? hit.tagName : 'NONE',
        };
    }, { childViewSel: CHILD_VIEW, sidebarSel: LEFT_SIDEBAR });
}

test.describe('Composition full-bleed layout', () => {

    /**
     * Both tabs are measured in ONE test, deliberately. The absolute numbers are viewport- and
     * theme-dependent, so what actually pins the behaviour is the DIFFERENCE between the two:
     * composition must reclaim the gutter that general reserves. Split across two tests, a change
     * that indented BOTH tabs by 242px would leave the composition test green.
     */
    test('composition reclaims the sidebar gutter that other tabs reserve',
        async ({ sdcPage, api }) => {
            const vf = await api.createVf('PwGeom');

            await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });
            await settles(sdcPage);
            await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 30_000 });
            const general = await measure(sdcPage);

            expect(general.bodyClass, 'the General tab lost its bodyClass').toContain('general');
            expect(general.sidebarPresent, 'the General tab lost the workspace sidebar').toBe(true);
            expect(general.childView, 'no .workspace-child-view on the General tab').not.toBeNull();
            // The gutter General reserves: 242px sidebar, 50px top-nav + 53px action bar.
            expect(general.childView!.left).toBeGreaterThan(200);
            expect(general.childView!.top).toBeGreaterThan(90);

            // The trailing slash is NOT optional — app.ts:371 declares `url: 'composition/'`, and
            // without it ui-router fails to match and silently lands on /dashboard.
            await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'composition/' });
            await settles(sdcPage);
            await expect(sdcPage.locator('composition-page')).toBeAttached({ timeout: 30_000 });
            await expect.poll(() => sdcPage.evaluate(() => document.body.className),
                { timeout: 20_000, message: 'body never got the composition class' })
                .toContain('composition');
            // The canvas mounts asynchronously after the route resolves; poll on the geometry
            // rather than asserting once, so a slow graph init is not read as a layout regression.
            await expect.poll(async () => (await measure(sdcPage)).childView?.left,
                { timeout: 20_000, message: 'the child view never went full-bleed' })
                .toBe(0);
            const composition = await measure(sdcPage);

            expect(await currentRoute(sdcPage)).toContain(`/workspace/${vf.id}/resource/composition/`);

            expect(composition.childView!.left,
                'composition is indented — the full-bleed rule did not apply').toBe(0);
            expect(composition.childView!.top,
                'composition still reserves the action-bar strip').toBeLessThan(general.childView!.top);
            expect(composition.childView!.width,
                'composition is narrower than General — it must be WIDER, it reclaims the gutter')
                .toBeGreaterThan(general.childView!.width);
            expect(composition.sidebarPresent,
                'the workspace sidebar is still mounted on composition — isComposition did not fire')
                .toBe(false);

            // Geometry alone would pass on a canvas covered by a stale overlay.
            expect(composition.hitAt300x400,
                'nothing hit-testable at x=300 inside the reclaimed gutter')
                .toBe('CANVAS');
        });
});
