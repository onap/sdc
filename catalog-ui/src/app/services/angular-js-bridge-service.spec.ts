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

/**
 * Characterization test for `AngularJSBridge` (SDC-4829 Phase 10-12 safety net).
 *
 * A static bridge that stashes AngularJS `$filter` and the app config so pure-Angular code can
 * reach them without DI. Constructing it populates the statics; the static getters read them
 * back. This is a bridge that Phase 13 removes, so pinning its exact contract guards against a
 * premature/incorrect teardown.
 */
import {AngularJSBridge} from './angular-js-bridge-service';

describe('AngularJSBridge (characterization)', () => {
    it('getFilter delegates to the $filter captured at construction', () => {
        const upperFilter = (s: string) => s.toUpperCase();
        const $filter = jest.fn().mockReturnValue(upperFilter);
        // Constructing the bridge captures $filter + config into the statics.
        // tslint:disable-next-line:no-unused-expression
        new AngularJSBridge($filter as any, {api: {}} as any);

        const resolved = AngularJSBridge.getFilter('uppercase');

        expect($filter).toHaveBeenCalledWith('uppercase');
        expect(resolved).toBe(upperFilter);
    });

    it('getAngularConfig returns the sdcConfig captured at construction', () => {
        const config = {api: {root: 'http://be/'}} as any;
        // tslint:disable-next-line:no-unused-expression
        new AngularJSBridge(jest.fn() as any, config);

        expect(AngularJSBridge.getAngularConfig()).toBe(config);
    });
});
