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

import {UnsavedChangesFlagGuard} from './unsaved-changes-flag.guard';

/** Mirrors the RouterStateSnapshot shape the guard walks: root -> firstChild -> … */
function stateWith(url: string, params: any[]): any {
    const nodes = params.map((p) => ({params: p, firstChild: null}));
    nodes.forEach((n, i) => n.firstChild = nodes[i + 1] || null);
    return {url, root: nodes[0]};
}

describe('UnsavedChangesFlagGuard', () => {
    let guard: UnsavedChangesFlagGuard;
    let workspaceService: any;
    let router: any;
    let modalService: any;
    const sdcMenu: any = {alertMessages: {exitWithoutSaving: {title: 'T', message: 'M'}, okButton: 'OK'}};

    const generalRoute: any = {params: {id: 'abc', type: 'resource'}};

    beforeEach(() => {
        workspaceService = {unsavedChanges: false};
        router = {navigateByUrl: jest.fn()};
        modalService = {openWarningModal: jest.fn()};
        guard = new UnsavedChangesFlagGuard(workspaceService, router, modalService, sdcMenu);
    });

    it('allows deactivation when the form is clean', () => {
        const next = stateWith('/dashboard', [{}]);
        expect(guard.canDeactivate({}, generalRoute, null, next)).toBe(true);
        expect(modalService.openWarningModal).not.toHaveBeenCalled();
    });

    /**
     * The regression this condition exists to prevent. A child canDeactivate fires on EVERY child
     * transition on router 5.2.11, so without the id comparison a dirty General tab would prompt on
     * every tab click — which app.ts:694's `fromParams.id != toParams.id` never did.
     */
    it('does NOT prompt on a tab switch within the same asset', () => {
        workspaceService.unsavedChanges = true;
        const next = stateWith('/dashboard/workspace/abc/resource/properties',
            [{}, {previousState: 'dashboard', id: 'abc', type: 'resource'}, {}]);
        expect(guard.canDeactivate({}, generalRoute, null, next)).toBe(true);
        expect(modalService.openWarningModal).not.toHaveBeenCalled();
    });

    it('prompts and blocks when leaving the asset with unsaved changes', () => {
        workspaceService.unsavedChanges = true;
        const next = stateWith('/dashboard', [{}]);
        expect(guard.canDeactivate({}, generalRoute, null, next)).toBe(false);
        expect(modalService.openWarningModal)
            .toHaveBeenCalledWith('T', 'M', 'navigate-modal', expect.any(Array));
        expect(router.navigateByUrl).not.toHaveBeenCalled();
    });

    it('prompts when switching to a different asset id', () => {
        workspaceService.unsavedChanges = true;
        const next = stateWith('/dashboard/workspace/xyz/resource/general',
            [{}, {previousState: 'dashboard', id: 'xyz', type: 'resource'}, {}]);
        expect(guard.canDeactivate({}, generalRoute, null, next)).toBe(false);
        expect(modalService.openWarningModal).toHaveBeenCalled();
    });

    /**
     * The OK callback must clear the flag before re-navigating, or the re-issued navigation hits
     * this same guard and blocks a second time — the modal would reappear forever.
     */
    it('OK clears the flag and re-navigates to the original target', () => {
        workspaceService.unsavedChanges = true;
        guard.canDeactivate({}, generalRoute, null, stateWith('/catalog', [{}]));

        const buttons = modalService.openWarningModal.mock.calls[0][3];
        expect(buttons).toHaveLength(1);
        buttons[0].callback();

        expect(workspaceService.unsavedChanges).toBe(false);
        expect(router.navigateByUrl).toHaveBeenCalledWith('/catalog');
    });

    /**
     * The OK button's real DOM attribute is `navigate-modal-button-ok`, NOT the `testId: 'OK'` set
     * here: onap-ui-angular's modal template runs `'button-' + button.text | calculateTestId :
     * testId`, so the container testId is only a PREFIX and the suffix comes from the button TEXT.
     * The Playwright suite selects on that real attribute; this pins the inputs it derives from.
     */
    it('labels the OK button from sdcMenu so the rendered testId stays navigate-modal-button-ok', () => {
        workspaceService.unsavedChanges = true;
        guard.canDeactivate({}, generalRoute, null, stateWith('/dashboard', [{}]));
        expect(modalService.openWarningModal.mock.calls[0][3][0].text).toBe('OK');
    });

    it('allows deactivation when there is no next state', () => {
        workspaceService.unsavedChanges = true;
        expect(guard.canDeactivate({}, generalRoute, null, undefined)).toBe(true);
    });
});
