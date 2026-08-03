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

import {UnsavedChangesGuard} from './unsaved-changes.guard';

describe('UnsavedChangesGuard', () => {
    let guard: UnsavedChangesGuard;
    let router: any;
    const nextState: any = {url: '/dashboard'};

    beforeEach(() => {
        router = {navigateByUrl: jest.fn()};
        guard = new UnsavedChangesGuard(router);
    });

    it('allows deactivation when the form is clean', () => {
        const component: any = {hasChangedData: false, showUnsavedChangesAlert: jest.fn()};
        expect(guard.canDeactivate(component, null, null, nextState)).toBe(true);
        expect(component.showUnsavedChangesAlert).not.toHaveBeenCalled();
        expect(router.navigateByUrl).not.toHaveBeenCalled();
    });

    it('allows deactivation when the component is absent', () => {
        expect(guard.canDeactivate(null, null, null, nextState)).toBe(true);
    });

    /**
     * The guard always blocks the CURRENT navigation, even when the user will go on to confirm.
     * This is the whole point of the shape — see the canDeactivate doc comment: an Observable that
     * the modal resolves later wedges the router forever if the modal is dismissed with the X.
     */
    it('blocks the current navigation when the form is dirty, and prompts', () => {
        const component: any = {
            hasChangedData: true,
            showUnsavedChangesAlert: jest.fn().mockReturnValue(new Promise(() => undefined))
        };
        expect(guard.canDeactivate(component, null, null, nextState)).toBe(false);
        expect(component.showUnsavedChangesAlert).toHaveBeenCalled();
    });

    it('re-navigates to the original target when the user discards or saves', () => {
        const component: any = {hasChangedData: true, showUnsavedChangesAlert: () => Promise.resolve()};
        guard.canDeactivate(component, null, null, {url: '/catalog'} as any);
        return Promise.resolve().then(() => {
            expect(router.navigateByUrl).toHaveBeenCalledWith('/catalog');
        });
    });

    it('does not navigate when the user cancels, and does not reject', () => {
        const component: any = {hasChangedData: true, showUnsavedChangesAlert: () => Promise.reject('cancel')};
        guard.canDeactivate(component, null, null, nextState);
        return Promise.resolve().then(() => {
            expect(router.navigateByUrl).not.toHaveBeenCalled();
        });
    });

    /**
     * The X-close path: the modal's promise never settles. Nothing must happen — in particular the
     * guard must already have returned (not be awaiting), which the synchronous `false` proves.
     */
    it('leaves the router idle when the modal is dismissed without a decision', () => {
        const component: any = {hasChangedData: true, showUnsavedChangesAlert: () => new Promise(() => undefined)};
        expect(guard.canDeactivate(component, null, null, nextState)).toBe(false);
        return Promise.resolve().then(() => {
            expect(router.navigateByUrl).not.toHaveBeenCalled();
        });
    });
});
