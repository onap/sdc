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
 * A static bridge that stashes the AngularJS-side app config so pure-Angular code can reach it
 * without DI. Constructing it populates the static; the static getter reads it back. This is a
 * bridge that Phase 13 removes, so pinning its exact contract guards against a premature or
 * incorrect teardown.
 *
 * Phase 12 removed the bridge's `$filter` half along with the AngularJS filter layer it served
 * (`getFilter` had no callers left once `Sdc.Filters` was deleted); `sdcConfig` is all that
 * remains, still read by 8 call sites for `imagesPath`.
 */
import {AngularJSBridge} from './angular-js-bridge-service';

describe('AngularJSBridge (characterization)', () => {
    it('getAngularConfig returns the sdcConfig captured at construction', () => {
        const config = {api: {root: 'http://be/'}} as any;
        // Constructing the bridge captures the config into the static.
        // tslint:disable-next-line:no-unused-expression
        new AngularJSBridge(config);

        expect(AngularJSBridge.getAngularConfig()).toBe(config);
    });
});
