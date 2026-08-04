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
            // `initialNavigation` is deliberately left at its default ('legacy_enabled'): the router
            // then navigates from an APP_BOOTSTRAP_LISTENER, i.e. after every APP_INITIALIZER has
            // resolved, so the first routed component sees an authenticated user and a loaded
            // configuration. 'enabled' would run the navigation from a competing APP_INITIALIZER and
            // race `configServiceFactory`. It was 'disabled' while the hybrid bootstrap owned the
            // ordering — main.ts fired the first navigation by hand after upgrade.bootstrap().
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
