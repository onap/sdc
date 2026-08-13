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
 * Workspace tab-title visibility (SDC-4879).
 *
 * WHAT THIS GUARDS — that the 110px `{{getTabTitle()}}` heading is actually READABLE on every tab
 * that renders one, and absent without leaving dead space on the three that suppress it.
 *
 * THE DEFECT. `.w-sdc-main-container-body-content-header` is a preceding SIBLING of the
 * <router-outlet>, so it sits in normal flow and claims no space from an absolutely positioned box.
 * The routed tab was offset by @action_nav_height (53px) alone, i.e. it started at the TOP of the
 * title strip and painted over it. All 16 header-rendering tabs lost their heading; the tab content
 * also sat 110px too high, directly under the top bar. Introduced by f89b30141 (the ui-view ->
 * router-outlet swap), which replaced a `ui-view` nested BELOW the header inside
 * `.w-sdc-main-container-body-content-wrapper` with an outlet that is the header's sibling.
 *
 * WHY EVERY EXISTING GATE PASSED, and why this spec asserts what it does:
 *   - The element is present, non-empty and `toBeVisible()`-true the whole time; it is merely
 *     painted OVER. So existence checks — which is what every Selenium wait in
 *     `integration-tests/src/test/.../frontend/` is, including the exact-class xpath waits that
 *     already resolve `.workspace-tab-title` — cannot see this at all.
 *   - Jest never lays out a page and AOT has no opinion on CSS.
 *   - The mechanism is therefore asserted with `elementFromPoint` at the heading's own centre:
 *     the ONE check that distinguishes "rendered" from "rendered and not covered".
 *
 * THE SECOND INVARIANT — `routedOverhang`. Offsetting the tab downwards is only safe while the box
 * stays inside `.w-sdc-main-right-container`, which is `overflow: hidden`. A routed component that
 * declares its own height (properties-assignment and attributes-outputs both set `:host { height:
 * 100% }`) over-constrains the
 * absolutely positioned box, CSS 2.1 §10.6.4 then ignores `bottom`, and the tab hangs `top` px past
 * that clip — silently swallowing its last rows, with no scroll container to reach them. That was
 * already true of the 53px offset and became a test failure at 163px (EtsiModelUiTests could no
 * longer see the alphabetically last VF property). `height: auto` in the shared rule is the fix;
 * this assertion is what keeps the next offset change from re-introducing it.
 *
 * The three full-bleed states (Composition, Deployment, plugins) are the counterweight: a fix that
 * simply offset EVERY routed element by 110px would satisfy the title assertions and leave those
 * three with a 110px white band above their canvas. They are measured, not assumed to travel
 * together — each is matched by a DIFFERENT hook in workspace.less (composition by body class, since
 * its component is selectorless; deployment-page and plugin-context-view by tag, since neither route
 * declares a bodyClass).
 *
 * The third of those, `plugins/:path`, is asserted in Jest rather than here: reaching it needs a
 * plugin registered and reachable in the deployed stack, which the integration-test stack has none
 * of. `isPlugins` (declared but assigned nowhere, so stuck false — the second half of this fix) is
 * covered by workspace-container.component.spec.ts's `updateFullBleedFlags` block. Note the
 * flow-editor tabs are NOT plugin states: Management Workflow and Network Call Flow have their own
 * state names and DO render a heading, so they belong with the titled tabs below.
 *
 * HOW TO RUN: see README.md — `npx playwright test workspace-tab-title`.
 */

import { test, expect, SEL, settles, gotoWorkspaceTab } from './fixtures/sdc';

const TITLE = '.workspace-tab-title';
const CHILD_VIEW =
    '.sdc-workspace-container .w-sdc-main-right-container > .w-sdc-main-right-container-content > router-outlet ~ *';
const TOP_BAR = '.sdc-workspace-top-bar';
/** The `overflow: hidden` box the routed tab must stay inside (assets/styles/layout/main.less). */
const CLIP = '.w-sdc-main-right-container';

interface TitleLayout {
    titlePresent: boolean;
    titleText: string | null;
    titleTop: number | null;
    titleBottom: number | null;
    titleHeight: number | null;
    routedTag: string | null;
    routedTop: number | null;
    topBarBottom: number | null;
    /** True when the routed tab's box vertically intersects the heading's box. */
    overlapsTitle: boolean | null;
    /** How far the routed tab's bottom edge falls past the clipping box's — must be <= 0. */
    routedOverhang: number | null;
    /** The tag actually painted at the heading's centre — the tab element iff it covers the title. */
    tagAtTitleCentre: string | null;
}

async function measure(page): Promise<TitleLayout> {
    return page.evaluate(({ titleSel, childViewSel, barSel, clipSel }) => {
        const title = document.querySelector(titleSel) as HTMLElement;
        const routed = document.querySelector(childViewSel) as HTMLElement;
        const bar = document.querySelector(barSel) as HTMLElement;
        const clip = document.querySelector(clipSel) as HTMLElement;

        const t = title ? title.getBoundingClientRect() : null;
        const r = routed ? routed.getBoundingClientRect() : null;
        const c = clip ? clip.getBoundingClientRect() : null;

        let tagAtCentre: string | null = null;
        if (t && t.width && t.height) {
            // Probed 60px in from the heading's left edge rather than at its horizontal midpoint:
            // the heading is a full-width flex child, so its centre can fall beyond the end of a
            // short label ("General") and hit the heading's own padding either way. 60px is inside
            // the shortest tab name once the 100px text indent is accounted for by the caller's
            // left edge, and is where the covering element — if any — is painted.
            const el = document.elementFromPoint(t.left + 60, t.top + t.height / 2);
            tagAtCentre = el ? el.tagName.toLowerCase() : null;
        }

        return {
            titlePresent: !!title,
            titleText: title ? (title.textContent || '').trim() : null,
            titleTop: t ? Math.round(t.top) : null,
            titleBottom: t ? Math.round(t.bottom) : null,
            titleHeight: t ? Math.round(t.height) : null,
            routedTag: routed ? routed.tagName.toLowerCase() : null,
            routedTop: r ? Math.round(r.top) : null,
            topBarBottom: bar ? Math.round(bar.getBoundingClientRect().bottom) : null,
            overlapsTitle: t && r ? !(r.top >= t.bottom || r.bottom <= t.top) : null,
            routedOverhang: r && c ? Math.round(r.bottom - c.bottom) : null,
            tagAtTitleCentre: tagAtCentre,
        };
    }, { titleSel: TITLE, childViewSel: CHILD_VIEW, barSel: TOP_BAR, clipSel: CLIP });
}

/**
 * The tabs that render a heading, with the element the outlet activates for each.
 * `properties_assignment` is `ng-component` because PropertiesAssignmentComponent declares no
 * selector; it is in this list for the overhang assertion, being one of the two tabs whose own :host
 * sets a height (attributes_outputs is the other) and so reachable by the over-constraint above.
 */
const TITLED_TABS: Array<{ tab: string; tag: string; title: string }> = [
    { tab: 'general', tag: 'general-tab', title: 'General' },
    { tab: 'information_artifacts', tag: 'information-artifact-page', title: 'Information Artifact' },
    { tab: 'tosca_artifacts', tag: 'tosca-artifact-page', title: 'TOSCA Artifacts' },
    { tab: 'deployment_artifacts', tag: 'deployment-artifact-page', title: 'Deployment Artifact' },
    { tab: 'activity_log', tag: 'activity-log', title: 'Activity Log' },
    { tab: 'properties_assignment', tag: 'ng-component', title: 'Properties Assignment' },
    { tab: 'attributes_outputs', tag: 'attributes-outputs', title: 'Attributes & Outputs' },
];

test.describe('Workspace tab title', () => {

    /**
     * One asset, several tabs: the defect is a property of the SHELL, identical on every tab, so
     * re-creating a VF per tab would buy nothing but minutes. Driven through gotoWorkspaceTab
     * (a real navigation) rather than by mutating the DOM, so the assertion covers the compiled
     * bundle's CSS exactly as a user meets it.
     */
    test('the heading is readable, not painted over, on every tab that renders one', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwTabTitle');

        for (const { tab, tag, title } of TITLED_TABS) {
            await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab });
            await settles(sdcPage);
            // Poll: the routed element mounts after the route resolves, and asserting once would
            // read a slow tab as a layout regression.
            await expect.poll(async () => (await measure(sdcPage)).routedTag,
                { timeout: 30_000, message: `the ${tab} tab never mounted` })
                .toBe(tag);

            const g = await measure(sdcPage);

            expect(g.titlePresent, `${tab}: no .workspace-tab-title in the shell`).toBe(true);
            expect(g.titleText, `${tab}: the heading rendered empty`).toBe(title);

            // THE REGRESSION ASSERTION. Existence and visibility both held throughout the defect;
            // only these two see it.
            expect(g.overlapsTitle,
                `${tab}: the routed tab overlaps the heading — the tab is painted over its own title`)
                .toBe(false);
            expect(g.tagAtTitleCentre,
                `${tab}: <${g.tagAtTitleCentre}> is painted at the heading's centre, not the heading`)
                .not.toBe(tag);

            // The heading occupies the strip between the top bar and the tab, in that order.
            expect(g.titleTop!, `${tab}: the heading starts above the top bar`)
                .toBeGreaterThanOrEqual(g.topBarBottom!);
            expect(g.routedTop!, `${tab}: the tab content starts above the heading's bottom edge`)
                .toBeGreaterThanOrEqual(g.titleBottom!);

            // ...and it must still END inside the clipping box: pushing the top down without
            // resolving the height from top/bottom moves the tab's whole box down, past an
            // overflow:hidden edge that swallows its last rows with no way to scroll to them.
            // <= 1 rather than <= 0 to absorb subpixel rounding of the two rects.
            expect(g.routedOverhang!,
                `${tab}: the tab hangs ${g.routedOverhang}px below .w-sdc-main-right-container, ` +
                `whose overflow:hidden clips it — its last rows are unreachable`)
                .toBeLessThanOrEqual(1);
        }
    });

    /**
     * The counterweight. These three suppress the header, so they must reclaim the 110px the titled
     * tabs reserve — otherwise the fix trades a covered heading for a white band above the canvas.
     * Asserted as a RELATION to the top bar (the tab begins where the bar ends) rather than as an
     * absolute offset, so it survives a change to @action_nav_height or @tab_title.
     *
     * 'plugins/*' covers the isPlugins half of the fix: with that flag stuck false the header
     * rendered here too, which this test's titlePresent assertion catches directly.
     */
    test('the full-bleed tabs render no heading and no dead space', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwFullBleedTitle');

        for (const tab of ['composition', 'deployment']) {
            await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab });
            await settles(sdcPage);
            await expect.poll(async () => (await measure(sdcPage)).routedTop !== null,
                { timeout: 30_000, message: `the ${tab} tab never mounted` })
                .toBe(true);

            const g = await measure(sdcPage);

            expect(g.titlePresent, `${tab}: a heading rendered on a full-bleed tab`).toBe(false);
            // Composition additionally reclaims the action-bar strip (its top bar floats OVER the
            // canvas), so it may start at or above the bar's bottom edge; Deployment starts at it.
            // Either way the tab must NOT begin a title-strip's height below the bar.
            expect(g.routedTop!, `${tab}: the canvas starts a title strip below the top bar — dead space`)
                .toBeLessThan(g.topBarBottom! + 110);
            expect(g.routedOverhang!,
                `${tab}: the canvas hangs ${g.routedOverhang}px below .w-sdc-main-right-container's ` +
                `overflow:hidden edge`)
                .toBeLessThanOrEqual(1);
        }
    });
});
