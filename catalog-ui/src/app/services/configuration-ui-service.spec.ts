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
 * Characterization test for `ConfigurationUiService` (SDC-4829 Phase 10-12 safety net).
 * HTTP-shape spec: pins the GET URL (api.root + api.GET_configuration_ui) and the .data unwrap.
 */
import {ConfigurationUiService} from './configuration-ui-service';

describe('ConfigurationUiService (HTTP-shape characterization)', () => {
    const api = {root: 'http://be/', GET_configuration_ui: 'v1/config/ui'} as any;

    const makeQ = () => ({
        defer: () => {
            let resolveFn: (v: any) => void;
            const promise = new Promise((res) => (resolveFn = res));
            return {promise, resolve: (v: any) => resolveFn(v)};
        },
    });

    it('GETs api.root + GET_configuration_ui and resolves the unwrapped .data', async () => {
        const httpGet = jest.fn().mockReturnValue(Promise.resolve({data: {featureX: true}}));
        const svc = new ConfigurationUiService({get: httpGet} as any, makeQ() as any, {api} as any);

        const result = await svc.getConfigurationUi();

        expect(httpGet).toHaveBeenCalledWith('http://be/v1/config/ui');
        expect(result).toEqual({featureX: true});
    });
});
