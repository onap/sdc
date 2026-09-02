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

import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/switchMap';
import {IUserProperties} from '../../models/user';
import {Role} from '../utils/constants';
import {AuthenticationService} from '../services/authentication.service';
import {NavigationService} from '../services/navigation.service';

/**
 * The single authorisation gate. Besides the permission check it absorbs three behaviours that used
 * to live in ui-router's `onStateChangeStart` run block (app.ts:640-698):
 *
 *  - **authenticate-if-no-cached-user.** `APP_INITIALIZER` authenticates at bootstrap but swallows
 *    its own failure, so the first navigation can still find no user; the run block re-authenticated
 *    right here (:644) and so does this.
 *  - **the ADMIN redirect** (:656-659 and again :688-691). ui-router bounced an ADMIN to
 *    adminDashboard on every state change; expressed here as "a refusal caused by a missing role
 *    sends an ADMIN to adminDashboard rather than error-403". That avoids the global version's
 *    obvious defect of also bouncing the admin off adminDashboard itself.
 *  - **`catalog`'s `resolve.auth`** (:546-555), which rejected when no user was cached — now the
 *    no-user branch below.
 */
@Injectable()
export class AuthGuard implements CanActivate {

    constructor(private authService: AuthenticationService,
                private navigationService: NavigationService) {
    }

    public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
        const cached: IUserProperties = this.authService.getLoggedinUser();
        if (cached) {
            return Observable.of(this.decide(cached, route));
        }

        return this.authService.authenticate()
            .switchMap((user: IUserProperties) => {
                this.authService.setLoggedinUser(user);
                return Observable.of(this.decide(user, route));
            })
            // A guard that ERRORS leaves the router with `navigated === false`, so every later
            // navigation is treated as the initial one and `onSameUrlNavigation` stops working.
            // A failed authentication therefore has to resolve to false rather than propagate.
            .catch(() => {
                this.navigationService.navigate('error-403');
                return Observable.of(false);
            });
    }

    private decide(user: IUserProperties, route: ActivatedRouteSnapshot): boolean {
        if (!user) {
            this.navigationService.navigate('error-403');
            return false;
        }

        const requiredRoles: string[] = route.data && route.data['permissions'];
        if (!requiredRoles || !requiredRoles.length || requiredRoles.indexOf(user.role) !== -1) {
            return true;
        }

        this.navigationService.navigate(user.role === Role.ADMIN ? 'adminDashboard' : 'error-403');
        return false;
    }
}
