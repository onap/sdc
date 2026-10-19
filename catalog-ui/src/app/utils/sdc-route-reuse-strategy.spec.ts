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

import {ActivatedRouteSnapshot, Route} from '@angular/router';
import {SdcRouteReuseStrategy} from './sdc-route-reuse-strategy';

describe('SdcRouteReuseStrategy', () => {
    const strategy = new SdcRouteReuseStrategy();
    const shell: Route = {path: ':previousState/workspace/:id/:type'};
    const generalTab: Route = {path: 'general'};
    const propertiesTab: Route = {path: 'properties'};
    const dashboard: Route = {path: 'dashboard'};
    const snapshot = (routeConfig: Route, params: any = {}) => ({routeConfig, params} as ActivatedRouteSnapshot);

    it('rebuilds a tab whose inherited id changed', () => {
        expect(strategy.shouldReuseRoute(snapshot(generalTab, {id: 'v2'}), snapshot(generalTab, {id: 'v1'}))).toBe(false);
    });

    it('reuses a tab when the id stayed the same', () => {
        expect(strategy.shouldReuseRoute(snapshot(generalTab, {id: 'v1'}), snapshot(generalTab, {id: 'v1'}))).toBe(true);
    });

    it('reuses the shell across an id change, since it declares :id itself', () => {
        expect(strategy.shouldReuseRoute(snapshot(shell, {id: 'v2'}), snapshot(shell, {id: 'v1'}))).toBe(true);
    });

    it('never reuses across different route configs', () => {
        expect(strategy.shouldReuseRoute(snapshot(propertiesTab, {id: 'v1'}), snapshot(generalTab, {id: 'v1'}))).toBe(false);
    });

    it('reuses routes without an id like the default strategy', () => {
        expect(strategy.shouldReuseRoute(snapshot(dashboard), snapshot(dashboard))).toBe(true);
        expect(strategy.shouldReuseRoute(snapshot(null), snapshot(null))).toBe(true);
    });

    it('detaches and stores nothing', () => {
        expect(strategy.shouldDetach(snapshot(generalTab))).toBe(false);
        expect(strategy.shouldAttach(snapshot(generalTab))).toBe(false);
        expect(strategy.retrieve(snapshot(generalTab))).toBeNull();
    });
});
