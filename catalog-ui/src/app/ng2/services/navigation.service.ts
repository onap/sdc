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

import {Inject, Injectable} from '@angular/core';

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

@Injectable()
export class NavigationService {

    constructor(@Inject('$state') private $state: ng.ui.IStateService) {
    }

    navigate(state: string, params?: any, options?: NavigationOptions): ng.IPromise<any> {
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
}
