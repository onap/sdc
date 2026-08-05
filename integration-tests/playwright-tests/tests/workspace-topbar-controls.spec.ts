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

import { test, expect, gotoWorkspaceTab, settles, CreatedAsset } from './fixtures/sdc';
import { Page } from '@playwright/test';

/**
 * The five top-bar controls the AngularJS removal dropped. Jest can only assert the template
 * STRING contains them; only a browser proves they render, are hit-testable, and that their
 * handlers reach the backend. Archive/restore/delete-archived are also the three that leave the
 * component unviewable, so a broken navigation or a stale archive cache is only visible live.
 */

const TESTS_IDS = {
    upgrade: '[data-tests-id="open-upgrade-vsp-popup"]',
    restore: '[data-tests-id="restore-component-button"]',
    deleteVersion: '[data-tests-id="delete_version"]',
    deleteArchived: '[data-tests-id="delete_archive_version"]',
    archive: '[data-tests-id="archive-component-button"]',
};

async function lifecycle(page: Page, asset: CreatedAsset, action: string): Promise<any> {
    return page.evaluate(async ([id, act, coll]) => {
        const r = await fetch(`/sdc1/feProxy/rest/v1/catalog/${coll}/${id}/lifecycleState/${act}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', USER_ID: 'cs0008' },
            body: JSON.stringify({ userRemarks: 'playwright' }),
        });
        const body = await r.json().catch(() => ({}));
        return { status: r.status, id: body.uniqueId, state: body.lifecycleState };
    }, [asset.id, action, asset.type === 'service' ? 'services' : 'resources']);
}

/** Geometry + hit-test for one control: rendered, non-zero, and the topmost node at its centre. */
async function probe(page: Page, selector: string) {
    return page.evaluate((sel) => {
        const el = document.querySelector(sel);
        if (!el) { return null; }
        const r = el.getBoundingClientRect();
        const cs = getComputedStyle(el);
        const hit = document.elementFromPoint(r.x + r.width / 2, r.y + r.height / 2);
        return {
            text: (el.textContent || '').trim(),
            width: Math.round(r.width),
            height: Math.round(r.height),
            hasSprite: cs.backgroundImage !== 'none',
            greyed: el.classList.contains('disabled'),
            hitSelf: hit === el || el.contains(hit as Node),
        };
    }, selector);
}

test.describe('workspace top-bar controls', () => {

    test('a checked-out service offers Delete version but not Archive', async ({ sdcPage, api }) => {
        const svc = await api.createService('PwTopBarCo');
        try {
            await gotoWorkspaceTab(sdcPage, { id: svc.id, type: 'service', cold: true });
            await expect(sdcPage.locator('.sdc-workspace-top-bar')).toBeAttached({ timeout: 30_000 });

            // Exactly ONE element may carry this id. The lifecycle-button loop also has a
            // 'deleteVersion' entry, and suppressing it with [hidden] instead of filtering it out
            // of the array leaves a 0x0 button in the DOM ahead of the sprite span; Selenium and
            // Playwright both resolve the FIRST match, so the real control becomes unreachable
            // while every static gate stays green.
            await expect(sdcPage.locator(TESTS_IDS.deleteVersion)).toHaveCount(1);

            const del = await probe(sdcPage, TESTS_IDS.deleteVersion);
            expect(del, 'delete_version must render on a checked-out component').not.toBeNull();
            expect(del!.width).toBeGreaterThan(0);
            expect(del!.height).toBeGreaterThan(0);
            // The control is a sprite span, so a missing background-image means an invisible button.
            expect(del!.hasSprite).toBe(true);
            expect(del!.hitSelf).toBe(true);

            // Archive requires the checkout to be resolved first; delete_archive_version and
            // Restore require the component to already be archived.
            expect(await probe(sdcPage, TESTS_IDS.archive)).toBeNull();
            expect(await probe(sdcPage, TESTS_IDS.deleteArchived)).toBeNull();
            expect(await probe(sdcPage, TESTS_IDS.restore)).toBeNull();
        } finally {
            await api.deleteAsset(svc);
        }
    });

    test('archive → restore → delete-archived all reach the backend from the top bar', async ({ sdcPage, api }) => {
        const created = await api.createService('PwTopBarLc');
        let asset: CreatedAsset = created;
        try {
            await gotoWorkspaceTab(sdcPage, { id: asset.id, type: 'service', cold: true });
            await expect(sdcPage.locator('.sdc-workspace-top-bar')).toBeAttached({ timeout: 30_000 });

            await lifecycle(sdcPage, asset, 'CHECKIN');
            const certified = await lifecycle(sdcPage, asset, 'certify');
            expect(certified.state).toBe('CERTIFIED');
            asset = { ...asset, id: certified.id };

            // --- certified: Archive and Upgrade appear, Delete version goes away
            await gotoWorkspaceTab(sdcPage, { id: asset.id, type: 'service', cold: true });
            await expect(sdcPage.locator('.sdc-workspace-top-bar')).toBeAttached({ timeout: 30_000 });

            const archive = await probe(sdcPage, TESTS_IDS.archive);
            expect(archive, 'archive-component-button must render on a certified component').not.toBeNull();
            expect(archive!.hasSprite).toBe(true);
            expect(archive!.hitSelf).toBe(true);
            expect(await probe(sdcPage, TESTS_IDS.deleteVersion)).toBeNull();

            const upgrade = await probe(sdcPage, TESTS_IDS.upgrade);
            expect(upgrade, 'open-upgrade-vsp-popup must render on a certified service').not.toBeNull();
            // A service says "Update Services"; only a VF says "Upgrade Services".
            expect(upgrade!.text).toBe('Update Services');

            // --- archive: a REAL click must issue the POST and re-render the bar
            const archivePost = sdcPage.waitForRequest(
                (r) => /\/archive$/.test(r.url()) && r.method() === 'POST', { timeout: 20_000 });
            await sdcPage.click(TESTS_IDS.archive, { timeout: 10_000 });
            expect((await archivePost).method()).toBe('POST');
            await settles(sdcPage);

            await gotoWorkspaceTab(sdcPage, { id: asset.id, type: 'service', cold: true });
            await expect(sdcPage.locator('.sdc-workspace-top-bar')).toBeAttached({ timeout: 30_000 });
            await expect(sdcPage.locator('.archive-state-label')).toBeAttached({ timeout: 20_000 });

            const restore = await probe(sdcPage, TESTS_IDS.restore);
            expect(restore, 'restore-component-button must render once archived').not.toBeNull();
            expect(restore!.text).toBe('Restore');
            expect(restore!.hitSelf).toBe(true);

            const deleteArchived = await probe(sdcPage, TESTS_IDS.deleteArchived);
            expect(deleteArchived, 'delete_archive_version must render once archived').not.toBeNull();
            expect(deleteArchived!.hasSprite).toBe(true);
            expect(deleteArchived!.hitSelf).toBe(true);
            // Archive must give way to its two archived-state counterparts.
            expect(await probe(sdcPage, TESTS_IDS.archive)).toBeNull();

            // --- restore
            const restorePost = sdcPage.waitForRequest(
                (r) => /\/restore$/.test(r.url()) && r.method() === 'POST', { timeout: 20_000 });
            await sdcPage.click(TESTS_IDS.restore, { timeout: 10_000 });
            expect((await restorePost).method()).toBe('POST');
            await settles(sdcPage);
            await expect(sdcPage.locator(TESTS_IDS.archive)).toBeAttached({ timeout: 25_000 });
            await expect(sdcPage.locator('.archive-state-label')).toHaveCount(0);

            // --- archive again, then delete-archived through its confirmation modal
            const reArchive = sdcPage.waitForRequest(
                (r) => /\/archive$/.test(r.url()) && r.method() === 'POST', { timeout: 20_000 });
            await sdcPage.click(TESTS_IDS.archive, { timeout: 10_000 });
            await reArchive;
            await settles(sdcPage);

            await gotoWorkspaceTab(sdcPage, { id: asset.id, type: 'service', cold: true });
            await expect(sdcPage.locator(TESTS_IDS.deleteArchived)).toBeAttached({ timeout: 30_000 });

            await sdcPage.click(TESTS_IDS.deleteArchived, { timeout: 10_000 });
            // NOT the `testId: 'ok-button'` passed to openWarningModal — sdc-ui's modal template
            // overrides it with `'button-' + button.text | calculateTestId : <containerTestId>`,
            // so the rendered id is '<modal testId>-button-<label>'. Same in the AngularJS original.
            const ok = sdcPage.locator("[data-tests-id='alert-modal-button-ok']").first();
            await expect(ok, 'delete-archived must confirm before deleting').toBeVisible({ timeout: 15_000 });

            // Stacking gate (§UU): the OK button of a dialog opened from the workspace must be the
            // topmost node at its own centre, or a real click lands on the backdrop instead.
            const okHit = await sdcPage.evaluate(() => {
                const b = document.querySelector("[data-tests-id='alert-modal-button-ok']");
                if (!b) { return null; }
                const r = b.getBoundingClientRect();
                const h = document.elementFromPoint(r.x + r.width / 2, r.y + r.height / 2);
                return { hitSelf: h === b || b.contains(h as Node), hit: h ? h.tagName : null };
            });
            expect(okHit!.hitSelf, `OK button occluded by ${okHit!.hit}`).toBe(true);

            const deleteReq = sdcPage.waitForRequest(
                (r) => r.method() === 'DELETE' && r.url().includes(asset.id), { timeout: 20_000 });
            await ok.click({ timeout: 10_000 });
            await deleteReq;
            await settles(sdcPage);

            // Polled, not read once: the archive list is a read-after-write against the BE and a
            // single immediate GET still sees the component (observed on the gating cluster).
            await expect.poll(async () => sdcPage.evaluate(async (id) => {
                const r = await fetch('/sdc1/feProxy/rest/v1/catalog/archive', { headers: { USER_ID: 'cs0008' } });
                const j = await r.json();
                return [].concat(j.services || [], j.resources || []).some((c: any) => c.uniqueId === id);
            }, asset.id), { message: 'the component must leave the archive list', timeout: 30_000 })
                .toBe(false);
        } finally {
            await api.deleteAsset(asset);
        }
    });
});
