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
 * Workspace content gutter and top-bar overflow (SDC-4857).
 *
 * WHAT THESE GUARD — two purely geometric properties of the workspace shell that no other gate in
 * this repo models. Both defects shipped and survived for months precisely because nothing measures
 * them: Jest never lays out a page, AOT has no opinion on CSS, and every Selenium wait is an
 * exact-class EXISTENCE check that passes on a mislaid element exactly as on a correct one.
 * composition-geometry.spec.ts is the closest relative and still misses both, since it compares
 * border boxes — which padding does not shift.
 *
 * 1. THE GUTTER. Until f89b30141 the ui-view element itself carried
 *    `padding: 0 100px 20px 100px`. That commit relocated the ui-view and the replacement selector
 *    (`.workspace-child-view > .sdc-workspace-general-step`) silently matched nothing, so for months
 *    every tab rendered flush against the 242px sidebar: Tags and Description were partly covered by
 *    it and the right-hand column ran to the exact viewport edge. The rule now lives on the routed
 *    element, the one node present for all 19 tabs.
 *
 *    Asserted as a RELATION (content starts right of the sidebar; content ends left of the viewport)
 *    rather than as absolute pixel values, so the test survives a theme or sidebar-width change and
 *    still fails on a lost gutter.
 *
 * 2. THE TOP BAR. `.sdc-workspace-top-bar-buttons > span.sprite-new` hides its "Close" label with
 *    `text-indent: 100%`, which pushes the text out of the sprite's 24px box but does NOT remove it
 *    — it still counts toward scrollWidth. That inflated the flex row past the bar and tripped the
 *    bar's own `overflow-x: auto`, giving a stray scrollbar and a truncated "✕ Clo…" at the right
 *    edge. Asserted on the bar's own scrollWidth-vs-clientWidth, which is the mechanism itself.
 *
 * The two full-bleed exclusions (Composition, Deployment) are covered here too: a fix that simply
 * padded EVERY routed element would break their canvases, and this suite would not otherwise notice.
 *
 * HOW TO RUN: see README.md — `npx playwright test workspace-gutter`.
 */

import { test, expect, SEL, settles, gotoWorkspaceTab } from './fixtures/sdc';

const CHILD_VIEW =
    '.sdc-workspace-container .w-sdc-main-right-container > .w-sdc-main-right-container-content > router-outlet ~ *';
const LEFT_SIDEBAR = '.w-sdc-left-sidebar';
const TOP_BAR = '.sdc-workspace-top-bar';
const TOP_BAR_BUTTONS = '.sdc-workspace-top-bar-buttons';

interface Layout {
    routedTag: string | null;
    routedPaddingLeft: number;
    sidebarRight: number | null;
    /** Leftmost laid-out label/field inside the tab, and the rightmost field edge. */
    contentLeft: number | null;
    contentRight: number | null;
    viewportWidth: number;
    barOverflow: number | null;
    buttonsOverflow: number | null;
    /** The Close control: its rendered box vs the width its hidden label wants. */
    closeBox: { width: number; scrollWidth: number; overflow: string } | null;
}

async function measure(page): Promise<Layout> {
    return page.evaluate(({ childViewSel, sidebarSel, barSel, buttonsSel }) => {
        const routed = document.querySelector(childViewSel) as HTMLElement;
        const sidebar = document.querySelector(sidebarSel) as HTMLElement;
        const bar = document.querySelector(barSel) as HTMLElement;
        const buttons = document.querySelector(buttonsSel) as HTMLElement;
        const close = document.querySelector(`${buttonsSel} > span.sprite-new`) as HTMLElement;

        // Only elements that were actually laid out: a 0x0 box carries no positional information and
        // would drag the minimum to 0, turning a real gutter loss into a pass.
        let left = Infinity;
        let right = -Infinity;
        if (routed) {
            for (const el of Array.from(
                routed.querySelectorAll('input, select, textarea, label, .i-sdc-form-label'),
            )) {
                const r = el.getBoundingClientRect();
                if (!r.width || !r.height) continue;
                left = Math.min(left, r.left);
                right = Math.max(right, r.right);
            }
        }
        const cs = routed && getComputedStyle(routed);
        return {
            routedTag: routed ? routed.tagName.toLowerCase() : null,
            routedPaddingLeft: cs ? parseFloat(cs.paddingLeft) || 0 : 0,
            sidebarRight: sidebar ? Math.round(sidebar.getBoundingClientRect().right) : null,
            contentLeft: left === Infinity ? null : Math.round(left),
            contentRight: right === -Infinity ? null : Math.round(right),
            viewportWidth: window.innerWidth,
            barOverflow: bar ? bar.scrollWidth - bar.clientWidth : null,
            buttonsOverflow: buttons ? buttons.scrollWidth - buttons.clientWidth : null,
            closeBox: close
                ? {
                    width: Math.round(close.getBoundingClientRect().width),
                    scrollWidth: close.scrollWidth,
                    overflow: getComputedStyle(close).overflow,
                }
                : null,
        };
    }, { childViewSel: CHILD_VIEW, sidebarSel: LEFT_SIDEBAR, barSel: TOP_BAR, buttonsSel: TOP_BAR_BUTTONS });
}

test.describe('Workspace layout', () => {

    test('tab content clears the sidebar and stays inside the viewport', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwGutter');

        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });
        await settles(sdcPage);
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 30_000 });

        const g = await measure(sdcPage);

        expect(g.routedTag, 'no routed element under the workspace outlet').toBe('general-tab');
        expect(g.sidebarRight, 'the workspace sidebar is not mounted').not.toBeNull();
        expect(g.contentLeft, 'no laid-out form field found on the General tab').not.toBeNull();

        // The defect: content began 2px INSIDE the sidebar's right edge.
        expect(g.contentLeft!, 'form content starts under the left sidebar — the gutter is gone')
            .toBeGreaterThan(g.sidebarRight!);
        // ...and the right-hand column ran to the exact viewport edge with no margin.
        expect(g.contentRight!, 'form content reaches the viewport edge — the right gutter is gone')
            .toBeLessThan(g.viewportWidth);
        // Asserted last: the two relations above are what users see, this pins the mechanism.
        expect(g.routedPaddingLeft, 'the routed element carries no left padding').toBeGreaterThan(0);
    });

    test('the top bar does not overflow and the Close label stays clipped', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwTopBar');

        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });
        await settles(sdcPage);
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 30_000 });

        const g = await measure(sdcPage);

        expect(g.closeBox, 'no sprite Close control in the top bar').not.toBeNull();
        // The precondition that makes this defect possible — asserted so that a future change which
        // stops indenting the label (and thus makes the clip unnecessary) shows up as a failure here
        // rather than as a silently vacuous test.
        expect(g.closeBox!.scrollWidth,
            'the Close label no longer overflows its sprite box — re-check whether the clip is still needed')
            .toBeGreaterThan(g.closeBox!.width);
        expect(g.closeBox!.overflow, 'the indented Close label is unclipped and will inflate the bar')
            .toBe('hidden');

        expect(g.buttonsOverflow, 'the top-bar button row overflows its own box').toBe(0);
        expect(g.barOverflow,
            'the top bar overflows — its overflow-x:auto will show a scrollbar and clip the Close control')
            .toBe(0);
    });

    /**
     * The counterweight to the gutter test: padding every routed element would satisfy that test and
     * break both canvas tabs. Composition is matched by body class (its component is selectorless)
     * and Deployment by tag (its route declares no bodyClass) — two different hooks, so both are
     * measured rather than assumed to travel together.
     */
    test('the full-bleed canvas tabs keep the whole box', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwFullBleed');

        for (const tab of ['composition', 'deployment']) {
            await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab });
            await settles(sdcPage);
            // The canvas mounts asynchronously after the route resolves; poll rather than assert once
            // so a slow graph init is not read as a layout regression.
            await expect.poll(async () => (await measure(sdcPage)).routedPaddingLeft,
                { timeout: 30_000, message: `the ${tab} tab was given the content gutter` })
                .toBe(0);
        }
    });
});
