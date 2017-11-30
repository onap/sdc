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
import {Response, Headers, RequestOptions, Http} from '@angular/http';
import {Cookie2Service} from "./cookie.service";
import { Observable } from 'rxjs/Observable';
import {SdcConfigToken, ISdcConfig} from "../config/sdc-config.config";

@Injectable()
export class AuthenticationService {

    constructor(private cookieService:Cookie2Service, private http: Http, @Inject(SdcConfigToken) private sdcConfig:ISdcConfig) {
        this.cookieService = cookieService;
        this.http = http;
    }

    private getAuthHeaders():any {
        let cookie:ICookie = this.sdcConfig.cookie;
        let authHeaders:any = {};
        authHeaders[cookie.userFirstName] = this.cookieService.getFirstName();
        authHeaders[cookie.userLastName] = this.cookieService.getLastName();
        authHeaders[cookie.userEmail] = this.cookieService.getEmail();
        authHeaders[cookie.userIdSuffix] = this.cookieService.getUserId();
        return authHeaders;
    }

    public authenticate(): Observable<JSON> {
        let options = new RequestOptions({
            headers: new Headers(this.getAuthHeaders())
        });

        let authUrl = this.sdcConfig.api.root + this.sdcConfig.api.GET_user_authorize;
        return this.http
            .get(authUrl, options)
            .map((res: Response) => res.json());
    }

}
