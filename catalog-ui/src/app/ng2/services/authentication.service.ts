/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import {Injectable, Inject} from '@angular/core';
import {IAppConfigurtaion, ICookie} from "../../models/app-config";
import {Cookie2Service} from "./cookie.service";
import { Observable } from 'rxjs/Observable';
import {SdcConfigToken, ISdcConfig} from "../config/sdc-config.config";
import { IUserProperties } from "app/models";
import { CacheService } from "app/ng2/services/cache.service";
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable()
export class AuthenticationService {

    private _loggedinUser:IUserProperties;
    
    constructor(private cookieService:Cookie2Service, private http: HttpClient, @Inject(SdcConfigToken) private sdcConfig:ISdcConfig, private cacheService: CacheService) {
        this.cookieService = cookieService;
    }

    private getAuthHeaders():HttpHeaders {
        let cookie:ICookie = this.sdcConfig.cookie;
        let authHeaders: HttpHeaders = new HttpHeaders();
        authHeaders = authHeaders.set(cookie.userFirstName, this.cookieService.getFirstName())
        .set(cookie.userLastName, this.cookieService.getLastName())
        .set(cookie.userEmail, this.cookieService.getEmail())
        .set(cookie.userIdSuffix, this.cookieService.getUserId())
        return authHeaders;
    }

    public authenticate(): Observable<IUserProperties> {
        let authUrl = this.sdcConfig.api.root + this.sdcConfig.api.GET_user_authorize;
        return this.http.get<IUserProperties>(authUrl, {headers: this.getAuthHeaders()});
    }

    public getLoggedinUser():IUserProperties {
        return this._loggedinUser;
    }

    public setLoggedinUser(loggedinUser:IUserProperties) {
        this._loggedinUser = loggedinUser;
        this.cacheService.set('user', loggedinUser);
    };

}
