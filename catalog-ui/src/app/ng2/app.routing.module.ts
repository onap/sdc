/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2025 Deutsche Telekom AG. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

import {NgModule} from '@angular/core';
import {RouterModule} from '@angular/router';
import {routes} from './app.routes';

/**
 * `SdcUrlHandlingStrategy` used to live here, gating which URLs the Angular router was allowed to
 * process while ui-router owned the rest. With ui-router gone there is nothing to share the URL
 * with, and the gate rejected every workspace URL — so it is deleted rather than widened.
 *
 * `LocationStrategy` is deliberately NOT overridden here: `RouterModule.forRoot()` registers its own
 * `provideLocationStrategy` in whichever module imports it, so a competing provider in this same
 * module is ambiguous. `SdcHashLocationStrategy` is bound in `AppModule` instead.
 */
@NgModule({
    imports: [
        RouterModule.forRoot(routes, {
            useHash: true,
            // The initial navigation is triggered by hand from main.ts, AFTER upgrade.bootstrap().
            // Left at the default ('legacy_enabled') the router navigates from an
            // APP_BOOTSTRAP_LISTENER, so the first routed component is constructed before the
            // AngularJS injector exists and every `deps: ['$injector']` provider it needs throws.
            // 'disabled' still installs the location-change listener, so only the FIRST navigation
            // is ours to fire; hash changes keep working untouched.
            initialNavigation: 'disabled',
            // ui-router's `{reload: true}` has no NavigationExtras equivalent; this is what makes
            // NavigationService.reload() re-run the workspace resolver on an unchanged URL.
            onSameUrlNavigation: 'reload',
            // The 19 workspace children read the shell's :id/:type params. Without 'always' they
            // would only inherit through a path-less or component-less parent, and the shell is
            // neither — every tab would see empty params.
            paramsInheritanceStrategy: 'always'
        })
    ],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
