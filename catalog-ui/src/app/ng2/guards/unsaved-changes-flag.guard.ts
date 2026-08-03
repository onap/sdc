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

import {Inject, Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanDeactivate, Router, RouterStateSnapshot} from '@angular/router';
import {SdcUiCommon, SdcUiComponents, SdcUiServices} from 'onap-ui-angular';
import {IAppMenu, SdcMenuToken} from '../config/sdc-menu.config';
import {WorkspaceService} from '../pages/workspace/workspace.service';

/**
 * Port of the `unsavedChanges` half of ui-router's `onStateChangeStart` (app.ts:693-696 →
 * `onNavigateOut` :619-638), which CR 2 deletes along with the rest of the run block.
 *
 * This is deliberately a SECOND, separate guard from `UnsavedChangesGuard`. The two cover
 * disjoint tabs and cannot be merged:
 *
 *   - `UnsavedChangesGuard` drives a component-owned modal through the `UnsavedChangesAware`
 *     contract (`hasChangedData` + `showUnsavedChangesAlert`). Only properties-assignment and
 *     attributes-outputs implement it.
 *   - The General tab implements NEITHER — verified: no `hasChangedData` and no
 *     `showUnsavedChangesAlert` in general-tab.component.ts. It only raises a boolean flag
 *     (`setUnsavedChanges(true)`, 6 call sites) and the run block owned both the flag test and
 *     the modal. Registering `UnsavedChangesGuard` on the General tab would type-check, run,
 *     find `hasChangedData === undefined`, allow the navigation and silently lose the edit.
 */
@Injectable()
export class UnsavedChangesFlagGuard implements CanDeactivate<any> {

    constructor(private workspaceService: WorkspaceService,
                private router: Router,
                private modalService: SdcUiServices.ModalService,
                @Inject(SdcMenuToken) private sdcMenu: IAppMenu) {
    }

    public canDeactivate(component: any,
                         currentRoute: ActivatedRouteSnapshot,
                         currentState: RouterStateSnapshot,
                         nextState?: RouterStateSnapshot): boolean {
        if (!this.workspaceService.unsavedChanges || !nextState) {
            return true;
        }
        if (!this.isLeavingAsset(currentRoute, nextState)) {
            return true;
        }
        this.prompt(nextState.url);
        return false;
    }

    /**
     * app.ts:694's `fromParams.id != toParams.id`, with the same loose comparison. Switching TABS
     * within one asset never prompted, only leaving the asset did. This condition is what keeps
     * that true: measured against router 5.2.11, a child `canDeactivate` fires on EVERY child
     * transition including a plain tab switch, so without the id test this guard would prompt on
     * each tab click — a regression the legacy code did not have.
     *
     * In create mode the current id is absent and the post-create redirect supplies one, which
     * must not prompt either; general-tab.component.ts:1190 clears the flag on a successful save,
     * so by then `unsavedChanges` is already false and this is never reached.
     */
    private isLeavingAsset(currentRoute: ActivatedRouteSnapshot, nextState: RouterStateSnapshot): boolean {
        return currentRoute.params['id'] != this.findParam(nextState.root, 'id');
    }

    private findParam(snapshot: ActivatedRouteSnapshot, key: string): any {
        let cursor: ActivatedRouteSnapshot = snapshot;
        let found: any;
        while (cursor) {
            if (cursor.params[key] !== undefined) {
                found = cursor.params[key];
            }
            cursor = cursor.firstChild;
        }
        return found;
    }

    /**
     * Returning `false` and re-navigating on OK — rather than returning a pending Observable the
     * modal later resolves — is required, not stylistic. Measured on router 5.2.11: a
     * `canDeactivate` Observable that never emits (the dismiss-the-modal path, which
     * `openWarningModal` gives no callback for) leaves the router permanently wedged, and every
     * later navigation hangs forever. Blocking synchronously and re-issuing the navigation from
     * the OK callback is also what app.ts:619-638 did — `$state.go(toState.name, toParams)`.
     */
    private prompt(targetUrl: string): void {
        const data = this.sdcMenu.alertMessages.exitWithoutSaving;
        const okButton = {
            testId: 'OK',
            text: this.sdcMenu.alertMessages.okButton,
            type: SdcUiCommon.ButtonType.warning,
            callback: () => {
                // app.ts:621 cleared the flag before re-issuing the transition; without this the
                // re-navigation would hit this same guard again and block a second time.
                this.workspaceService.unsavedChanges = false;
                this.router.navigateByUrl(targetUrl);
            },
            closeModal: true
        } as SdcUiComponents.ModalButtonComponent;

        this.modalService.openWarningModal(data.title, data.message, 'navigate-modal', [okButton]);
    }
}
