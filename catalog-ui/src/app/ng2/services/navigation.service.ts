/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2025 Deutsche Telekom AG. All rights reserved.
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

import {Inject, Injectable, Optional} from '@angular/core';
import {Router, ActivatedRoute} from '@angular/router';

export interface NavigationOptions {
    location?: boolean | 'replace';
    notify?: boolean;
    reload?: boolean;
}

export interface NavigationStartEvent {
    toState: string;
    toParams: any;
    fromState: string;
    fromParams: any;
    preventDefault: () => void;
}

export type NavigationStartCallback = (event: NavigationStartEvent) => void;

// States whose VIEW is actually rendered by the Angular Router (<router-outlet> in app.component.html).
// Only these may be navigated through router.navigate(); everything else must go through ui-router's
// $state.go so the visible <div ui-view> (index.html) re-renders.
//
// 'dashboard' and 'catalog' were listed here prematurely: their URLs are claimed by SdcUrlHandlingStrategy,
// but their views are still rendered by ui-router via the DOWNGRADED <home-page>/<catalog-page> components
// (app.ts state templates), NOT by the Angular routes. Routing them through router.navigate() updated the
// Angular Router state but left ui-router's <ui-view> untouched, so the top-nav HOME/CATALOG buttons became
// dead clicks (menu highlighted, view never changed). type-workspace proves the correct pattern: its URL is
// also claimed by the strategy, yet it is absent here and navigates fine via $state.go. Keep this list empty
// until a state's view is genuinely served by <router-outlet>; the navigateWithAngularRouter machinery below
// is retained for that future migration.
const ANGULAR_ROUTER_STATES: string[] = [];

@Injectable()
export class NavigationService {

    constructor(@Inject('$state') private $state: ng.ui.IStateService,
                @Optional() private router: Router) {
    }

    navigate(state: string, params?: any, options?: NavigationOptions): any {
        if (this.isAngularRouterState(state) && this.router) {
            return this.navigateWithAngularRouter(state, params, options);
        }
        return this.$state.go(state, params || undefined, options as any);
    }

    getCurrentStateName(): string {
        return this.$state.current.name || '';
    }

    getParams(): any {
        return this.$state.params;
    }

    getParam(key: string): any {
        return this.$state.params[key];
    }

    includes(stateName: string): boolean {
        return this.$state.includes(stateName);
    }

    updateUrlParams(params: any): ng.IPromise<any> {
        return this.$state.go('.', params, {location: 'replace', notify: false});
    }

    private isAngularRouterState(state: string): boolean {
        return ANGULAR_ROUTER_STATES.indexOf(state) !== -1;
    }

    private navigateWithAngularRouter(state: string, params?: any, options?: NavigationOptions): Promise<boolean> {
        const queryParams = params ? {...params} : undefined;
        const extras: any = {};
        if (queryParams) {
            extras.queryParams = queryParams;
        }
        if (options && options.location === 'replace') {
            extras.replaceUrl = true;
        }
        return this.router.navigate(['/' + state], extras);
    }
}
