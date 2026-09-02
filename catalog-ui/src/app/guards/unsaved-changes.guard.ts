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

import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanDeactivate, Router, RouterStateSnapshot} from '@angular/router';

export interface UnsavedChangesAware {
    hasChangedData: boolean;
    showUnsavedChangesAlert(): Promise<any>;
}

/**
 * Guards the properties-assignment and attributes-outputs tabs, the only two components that
 * implement the `UnsavedChangesAware` contract. The General tab uses a boolean flag and a
 * different modal instead — see `UnsavedChangesFlagGuard`.
 *
 * NOTE this guard is NEW behaviour, not a port: under ui-router the PA/AO states carried no
 * `data.unsavedChanges`, so app.ts:694's dirty check never fired for them and leaving the tab by
 * route silently discarded edits. Their `showUnsavedChangesAlert` was only ever reached from
 * in-page interactions (instance switch, tab switch). Wiring it to route exit closes that hole.
 */
@Injectable()
export class UnsavedChangesGuard implements CanDeactivate<UnsavedChangesAware> {

    constructor(private router: Router) {
    }

    /**
     * Blocks synchronously and re-issues the navigation once the modal resolves, rather than
     * returning a promise-backed Observable the modal settles later. That shape is required:
     * `showUnsavedChangesAlert` resolves its promise ONLY from the Cancel/Discard/Save button
     * callbacks, so closing the modal with the header X (`sdc-modal-close-button`, which
     * onap-ui-angular renders on every modal) leaves it forever pending. Measured on router
     * 5.2.11, a `canDeactivate` Observable that never emits wedges the router permanently — every
     * subsequent navigation in the app hangs. Returning false keeps the router idle, and an
     * abandoned modal costs nothing.
     */
    public canDeactivate(component: UnsavedChangesAware,
                         currentRoute: ActivatedRouteSnapshot,
                         currentState: RouterStateSnapshot,
                         nextState?: RouterStateSnapshot): boolean {
        if (!component || !component.hasChangedData || !nextState) {
            return true;
        }

        const targetUrl = nextState.url;
        component.showUnsavedChangesAlert().then(
            () => {
                // Discard reverses the edits and Save persists them; both clear `hasChangedData`
                // (via updateHasChangedData), so the re-navigation passes this guard cleanly.
                this.router.navigateByUrl(targetUrl);
            },
            () => undefined // Cancel: stay put. The two tabs previously disagreed on whether the
                            // rejection was handled at all; swallowing it here gives Cancel the
                            // same blocking semantics on both.
        );
        return false;
    }
}
