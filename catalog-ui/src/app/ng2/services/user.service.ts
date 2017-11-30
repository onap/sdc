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

import { Injectable, Inject } from "@angular/core";
import { Headers } from "@angular/http";
import { Observable } from "rxjs/Observable";
import { HttpService } from "./http.service";
import { Cookie2Service } from "./cookie.service";
import { IUserProperties } from "../../models/user";

import {ICookie} from "../../models/app-config";
import {SdcConfigToken, ISdcConfig} from "../config/sdc-config.config";

@Injectable()
export class UserService {
    private url:string;
    private authorizeUrl:string;

    private _loggedinUser:IUserProperties;

    constructor(private httpService:HttpService,
                private cookieService:Cookie2Service,
                @Inject(SdcConfigToken) private sdcConfig:ISdcConfig) {
        this.url = this.sdcConfig.api.root + this.sdcConfig.api.GET_user;
        this.authorizeUrl = this.sdcConfig.api.root + this.sdcConfig.api.GET_user_authorize;
    }

    public authorize() :Observable<IUserProperties> {
        let cookie:ICookie = this.sdcConfig.cookie;
        let authorizeHeaders:Headers = new Headers();
        authorizeHeaders.set(cookie.userFirstName, this.cookieService.getFirstName());
        authorizeHeaders.set(cookie.userLastName, this.cookieService.getLastName());
        authorizeHeaders.set(cookie.userEmail, this.cookieService.getEmail());
        authorizeHeaders.set(cookie.userIdSuffix, this.cookieService.getUserId());

        return this.httpService.get(
            this.authorizeUrl,
            { headers: authorizeHeaders }
        ).map(resp => resp.json());
    }

    public getAllUsers() :Observable<IUserProperties[]> {
        return this.httpService.get(
            this.sdcConfig.api.root + this.sdcConfig.api.GET_all_users
        ).map(resp => resp.json());
    }

    public getUser(userId:string) :Observable<IUserProperties> {
        return this.httpService.get(
            HttpService.replaceUrlParams(this.url, { id: userId })
        ).map(resp => resp.json());
    }

    public createUser(userData:{[index:string]: any}) :Observable<IUserProperties> {
        return this.httpService.post(
            this.sdcConfig.api.root + this.sdcConfig.api.POST_create_user,
            userData
        ).map(resp => resp.json());
    }

    public deleteUser(userId:string) :Observable<IUserProperties> {
        return this.httpService.delete(
            HttpService.replaceUrlParams(this.sdcConfig.api.root + this.sdcConfig.api.DELETE_delete_user, { id: userId })
        ).map(resp => resp.json());
    }

    public editUserRole(userId:string, role:string) :Observable<IUserProperties> {
        return this.httpService.post(
            HttpService.replaceUrlParams(this.sdcConfig.api.root + this.sdcConfig.api.POST_edit_user_role, { id: userId }),
            { role: role }
        ).map(resp => resp.json());
    }

    public getLoggedinUser():IUserProperties {
        return this._loggedinUser;
    }

    public setLoggedinUser(loggedinUser:IUserProperties) {
        this._loggedinUser = loggedinUser;
    };
}
