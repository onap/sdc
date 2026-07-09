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
 * Characterization test for `EcompHeaderService` (SDC-4829 Phase 10-12 safety net).
 *
 * HTTP-shape spec: pins the :userId path substitution in GET_ecomp_menu_items, the .data unwrap
 * on success, and the .data reject on failure. The :userId interpolation is the load-bearing
 * detail most likely to break silently when reimplemented on Angular HttpClient.
 */
import {EcompHeaderService} from './ecomp-service';

describe('EcompHeaderService (HTTP-shape characterization)', () => {
    const api = {root: 'http://be/', GET_ecomp_menu_items: 'v1/user/:userId/menu'} as any;

    const makeQ = () => ({
        defer: () => {
            let resolveFn: (v: any) => void;
            let rejectFn: (v: any) => void;
            const promise = new Promise((res, rej) => {
                resolveFn = res;
                rejectFn = rej;
            });
            return {promise, resolve: (v: any) => resolveFn(v), reject: (v: any) => rejectFn(v)};
        },
    });

    it('substitutes :userId into the URL and resolves the unwrapped .data', async () => {
        const httpGet = jest.fn().mockReturnValue(Promise.resolve({data: [{id: 'home'}]}));
        const svc = new EcompHeaderService({get: httpGet} as any, makeQ() as any, {api} as any);

        const result = await svc.getMenuItems('cs0008');

        expect(httpGet).toHaveBeenCalledWith('http://be/v1/user/cs0008/menu');
        expect(result).toEqual([{id: 'home'}]);
    });

    it('rejects with the response .data on HTTP failure', async () => {
        const httpGet = jest.fn().mockReturnValue(Promise.reject({data: 'boom'}));
        const svc = new EcompHeaderService({get: httpGet} as any, makeQ() as any, {api} as any);

        await expect(svc.getMenuItems('cs0008')).rejects.toBe('boom');
    });
});
