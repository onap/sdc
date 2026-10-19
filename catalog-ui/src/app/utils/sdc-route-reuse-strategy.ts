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

import {ActivatedRouteSnapshot, DetachedRouteHandle, RouteReuseStrategy} from '@angular/router';

/**
 * The router's default, plus: a route that only INHERITS :id (every workspace tab, through
 * `paramsInheritanceStrategy: 'always'`) is rebuilt when :id changes. The tabs read their component
 * once on init, so a reused tab kept showing — and saving — the previous asset after a version
 * switch. A route that declares :id in its own path (the workspace shell, type-workspace) is still
 * reused and handles the change itself; rebuilding the shell would briefly drop the whole workspace
 * from the DOM.
 */
export class SdcRouteReuseStrategy implements RouteReuseStrategy {

    public shouldDetach(route: ActivatedRouteSnapshot): boolean {
        return false;
    }

    public store(route: ActivatedRouteSnapshot, handle: DetachedRouteHandle): void {
    }

    public shouldAttach(route: ActivatedRouteSnapshot): boolean {
        return false;
    }

    public retrieve(route: ActivatedRouteSnapshot): DetachedRouteHandle | null {
        return null;
    }

    public shouldReuseRoute(future: ActivatedRouteSnapshot, curr: ActivatedRouteSnapshot): boolean {
        if (future.routeConfig !== curr.routeConfig) {
            return false;
        }
        const declaresId = !!future.routeConfig && (future.routeConfig.path || '').split('/').indexOf(':id') > -1;
        return declaresId || future.params.id === curr.params.id;
    }
}
