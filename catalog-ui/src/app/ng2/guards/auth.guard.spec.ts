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

import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/throw';
import {AuthGuard} from './auth.guard';

/**
 * The guard absorbed three behaviours that used to live in the ui-router run block's
 * `onStateChangeStart` (app.ts:640-698), which is why it is now asynchronous:
 *   - authenticate-if-no-cached-user (:644),
 *   - the ADMIN → adminDashboard redirect (:656-659 AND :688-691, twice),
 *   - the `catalog` state's `resolve.auth` reject-if-no-user (:546-555).
 * Every case below traces back to one of those.
 */
describe('AuthGuard', () => {
    let guard: AuthGuard;
    let authService: any;
    let navigationService: any;

    const designer = {role: 'DESIGNER', userId: 'cs0008'};
    const admin = {role: 'ADMIN', userId: 'jh0003'};

    function route(data: any = {}): any {
        return {data};
    }

    function decide(routeSnapshot: any = route()): Promise<boolean> {
        return guard.canActivate(routeSnapshot, {} as any).toPromise();
    }

    beforeEach(() => {
        authService = {
            authenticate: jest.fn(),
            getLoggedinUser: jest.fn(),
            setLoggedinUser: jest.fn()
        };
        navigationService = {navigate: jest.fn()};
        guard = new AuthGuard(authService, navigationService);
    });

    describe('with a user already cached', () => {
        beforeEach(() => {
            authService.getLoggedinUser.mockReturnValue(designer);
        });

        it('allows a route that declares no permissions', () => {
            return decide().then((ok) => {
                expect(ok).toBe(true);
                expect(authService.authenticate).not.toHaveBeenCalled();
            });
        });

        it('allows a route whose permissions include the user role', () => {
            return decide(route({permissions: ['DESIGNER', 'ADMIN']})).then((ok) => expect(ok).toBe(true));
        });

        it('treats an empty permissions array as unrestricted', () => {
            return decide(route({permissions: []})).then((ok) => expect(ok).toBe(true));
        });

        it('sends a user without the required role to error-403', () => {
            return decide(route({permissions: ['GOVERNOR']})).then((ok) => {
                expect(ok).toBe(false);
                expect(navigationService.navigate).toHaveBeenCalledWith('error-403');
            });
        });
    });

    /**
     * ui-router redirected an ADMIN unconditionally on EVERY state change, so an admin could not
     * reach /dashboard at all. Reproduced by declaring `permissions: ['DESIGNER']` on that one route
     * and redirecting the refusal to adminDashboard instead of error-403 — same observable outcome,
     * without a global redirect that would also bounce the admin off /adminDashboard itself.
     */
    describe('ADMIN', () => {
        beforeEach(() => {
            authService.getLoggedinUser.mockReturnValue(admin);
        });

        it('is redirected to its own dashboard rather than error-403', () => {
            return decide(route({permissions: ['DESIGNER']})).then((ok) => {
                expect(ok).toBe(false);
                expect(navigationService.navigate).toHaveBeenCalledWith('adminDashboard');
                expect(navigationService.navigate).not.toHaveBeenCalledWith('error-403');
            });
        });

        it('reaches adminDashboard itself', () => {
            return decide(route({permissions: ['ADMIN']})).then((ok) => {
                expect(ok).toBe(true);
                expect(navigationService.navigate).not.toHaveBeenCalled();
            });
        });
    });

    /**
     * APP_INITIALIZER already authenticates once at bootstrap, but it swallows its own failure
     * (app.module.ts's `.catch()`), so the very first navigation can still find no cached user — for
     * instance on a cold deep link that races the initializer. The run block re-authenticated in
     * exactly that spot; so does this.
     */
    describe('with no user cached yet', () => {
        beforeEach(() => {
            authService.getLoggedinUser.mockReturnValue(undefined);
        });

        it('authenticates, caches the user and then decides', () => {
            authService.authenticate.mockReturnValue(Observable.of(designer));
            return decide(route({permissions: ['DESIGNER']})).then((ok) => {
                expect(ok).toBe(true);
                expect(authService.authenticate).toHaveBeenCalledTimes(1);
                expect(authService.setLoggedinUser).toHaveBeenCalledWith(designer);
            });
        });

        it('applies the permission check to the freshly-authenticated user', () => {
            authService.authenticate.mockReturnValue(Observable.of(designer));
            return decide(route({permissions: ['ADMIN']})).then((ok) => {
                expect(ok).toBe(false);
                expect(navigationService.navigate).toHaveBeenCalledWith('error-403');
            });
        });

        it('sends an ADMIN that authenticates on a DESIGNER route to adminDashboard', () => {
            authService.authenticate.mockReturnValue(Observable.of(admin));
            return decide(route({permissions: ['DESIGNER']})).then((ok) => {
                expect(ok).toBe(false);
                expect(navigationService.navigate).toHaveBeenCalledWith('adminDashboard');
            });
        });

        it('sends a failed authentication to error-403 instead of erroring the navigation', () => {
            authService.authenticate.mockReturnValue(Observable.throw(new Error('401')));
            return decide().then((ok) => {
                expect(ok).toBe(false);
                expect(navigationService.navigate).toHaveBeenCalledWith('error-403');
            });
        });

        /**
         * A guard that errors leaves the router with `navigated === false`, so every later navigation
         * is treated as the initial one. Swallowing the error into `false` is what keeps the app
         * usable after a failed auth — hence the assertion that nothing rejects.
         */
        it('never rejects, so the router is not left wedged', () => {
            authService.authenticate.mockReturnValue(Observable.throw(new Error('boom')));
            return decide().then(() => undefined, () => fail('canActivate must not error'));
        });
    });
});
