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
 * Characterization test for `SdcVersionService` (SDC-4829 Phase 10-12 safety net).
 *
 * HTTP-shape spec: pins the GET URL (api.root + api.GET_SDC_Version) and that the resolved value
 * is the response `.data` (unwrapped). This is the contract that must survive the port to Angular
 * HttpClient in Phase 11 — a different URL or a failure to unwrap `.data` is a regression this
 * catches. $http and $q are faked with native Promises (no AngularJS runtime needed).
 */
import {SdcVersionService} from './sdc-version-service';

describe('SdcVersionService (HTTP-shape characterization)', () => {
    const api = {root: 'http://be/', GET_SDC_Version: 'v1/version'} as any;

    const makeQ = () => {
        // Minimal $q.defer backed by a native Promise.
        return {
            defer: () => {
                let resolveFn: (v: any) => void;
                const promise = new Promise((res) => (resolveFn = res));
                return {promise, resolve: (v: any) => resolveFn(v)};
            },
        };
    };

    it('GETs api.root + GET_SDC_Version and resolves the unwrapped .data', async () => {
        const httpGet = jest.fn().mockReturnValue(Promise.resolve({data: {version: '1.17.0'}}));
        const $http = {get: httpGet} as any;
        const svc = new SdcVersionService($http, makeQ() as any, {api} as any);

        const result = await svc.getVersion();

        expect(httpGet).toHaveBeenCalledWith('http://be/v1/version');
        expect(result).toEqual({version: '1.17.0'});
    });
});
