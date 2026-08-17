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
 *   `bodyClass: 'composition'` lives in the route's `data` (app.routes.ts) and RouteMetadataService
 *   writes it onto <body> on every NavigationEnd. `workspace.less` then keys the full-bleed geometry
 *   off `.composition … router-outlet ~ *` — reclaiming the 242px sidebar gutter and the 53px
 *   action-bar strip that every other tab reserves.
 *
 *   Before phase 13 CR 2 this chain ran through ui-router: the state's `data`, then
 *   `$rootScope.bodyClass` in the run block, then `index.html`'s `<body data-ng-class>`. CR 2
 *   deleted all three. If the replacement forgets the class — or sets it on the wrong element, or a
 *   tick too late — the canvas renders inside a 1678px box indented 242px, with the palette shoved
 *   off under the sidebar. Nothing else detects that: Jest never mounts <body>, AOT has no opinion
 *   on CSS class strings, and Selenium's waits are all EXACT-class *existence* checks that pass on
 *   a mislaid canvas exactly as they do on a correct one.
 *
 * WHY GEOMETRY AND HIT-TESTING, not a screenshot: 242px of unwanted indent is the kind of
 * difference that reads as "looks fine" in a screenshot review. `elementFromPoint` additionally
 * proves the canvas is genuinely reachable at x=300 rather than merely sized to include it —
 * an overlay that swallows clicks is a real ngUpgrade failure mode and geometry alone misses it.
 *
 * The hit test has to outlast the LOAD OVERLAY, which is why it waits for it below. The canvas
 * itself is not the slow part: CompositionGraphComponent creates it in ngAfterViewInit, so all
 * three cytoscape layers exist before any request resolves. What arrives late is the *data* —
 * loadCompositionData() raises the global loader from ngOnInit and drops it only when the topology
 * response lands — and while that overlay is up it is `position: fixed; z-index: 9999` over the
 * whole viewport, so it, not the canvas, is the topmost element at the probe point. A trace from
 * the 2026-08-17 run has it exactly: canvas layers at left 244 / top 103 covering (300,400), the
 * overlay still mounted, `elementFromPoint` returning DIV, four BE requests still in flight.
 *
 * Measured at 1920x1080 on the Docker stack, which is what the numbers below encode:
 *   general      body='general'      child-view 242,103 1678x977   sidebar 0,50 242x1030
 *   composition  body='composition'  child-view   0,50 1920x1030   sidebar ABSENT
 *
 * HOW TO RUN: see README.md — `npx playwright test composition-geometry`.
 */

import { test, expect, SEL, settles, gotoWorkspaceTab, currentRoute } from './fixtures/sdc';

// The element the full-bleed rule targets — the component the workspace shell's <router-outlet>
// activated — is SEL.workspaceRoutedTab. Its note explains why it is matched as the outlet's sibling
// and why no wrapper element may be introduced to match instead.

/** The workspace shell's left tab rail, suppressed on composition by `*ngIf="!isComposition"`. */
const LEFT_SIDEBAR = '.w-sdc-left-sidebar';
/**
 * Any full-viewport load overlay. Deliberately UNSCOPED, matching workspace-tab-title.spec.ts: the
 * one that matters here is app-root's own `<sdc-loader [global]="true">`, which mounts outside
 * `.sdc-workspace-container` entirely. It is torn down rather than hidden when it deactivates, so
 * counting is the check.
 */
const LOADER = '.sdc-loader-global-wrapper';

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
    }, { childViewSel: SEL.workspaceRoutedTab, sidebarSel: LEFT_SIDEBAR });
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

            // Deliberately the LEGACY trailing-slash form, which is what ui-router emitted and what 6
            // Cypress specs and every old bookmark still use. It works because `Location.path()`
            // normalises the slash away before the router matches — which is also why the route is
            // declared as bare 'composition' (app.routes.ts). Keep this URL as-is: navigating with the
            // slash is the only thing that pins the compatibility.
            await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'composition/' });
            await settles(sdcPage);
            // Asserted on the template's own root div, not on a host element: CompositionPageComponent
            // declares NO selector (it was only ever reached through the ui-router template
            // '<composition-page>', an AngularJS downgradeComponent wrapper that CR 2 deleted), so the
            // router now renders it as an anonymous <ng-component>. Nothing else in the product, the
            // stylesheets or the Selenium page objects references either name, so the wrapper name was
            // not worth resurrecting purely to be asserted on.
            await expect(sdcPage.locator('.workspace-composition-page')).toBeAttached({ timeout: 30_000 });
            await expect.poll(() => sdcPage.evaluate(() => document.body.className),
                { timeout: 20_000, message: 'body never got the composition class' })
                .toContain('composition');
            // The canvas mounts asynchronously after the route resolves; poll on the geometry
            // rather than asserting once, so a slow graph init is not read as a layout regression.
            await expect.poll(async () => (await measure(sdcPage)).childView?.left,
                { timeout: 20_000, message: 'the child view never went full-bleed' })
                .toBe(0);
            // The composition tab raises the load overlay from its own ngOnInit and drops it only
            // when the topology request resolves — i.e. AFTER the geometry above has settled, since
            // none of it depends on that request. Measuring here reads backend latency as an
            // occluded canvas. Waiting does not weaken the guard: what the hit test exists to catch
            // is an overlay that never comes down, and 30s of a backend not answering is a backend
            // problem, reported as such by this wait rather than mis-reported by the assertion below.
            await expect(sdcPage.locator(LOADER), 'the load overlay never came down')
                .toHaveCount(0, { timeout: 30_000 });
            const composition = await measure(sdcPage);

            // No trailing slash: we navigated WITH one, and the router normalised it away and rewrote
            // the hash. Asserted to pin that rewrite, since it is what makes the legacy URL work.
            expect(await currentRoute(sdcPage)).toContain(`/workspace/${vf.id}/resource/composition`);

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
            //
            // Polled, not asserted once, even though the overlay is already down by here: the last
            // thing loadGraphData() does is `setTimeout(() => zoomAllWithMax(cy))`, so cytoscape
            // re-lays out its layers one macrotask after the response the wait above observed. The
            // poll costs nothing on the happy path and keeps that step from deciding the verdict.
            await expect.poll(async () => (await measure(sdcPage)).hitAt300x400,
                { timeout: 20_000, message: 'nothing hit-testable at x=300 inside the reclaimed gutter' })
                .toBe('CANVAS');
        });
});
