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
import { Observable } from "rxjs/Observable";
import { IUserProperties } from "../../models/user";

import {SdcConfigToken, ISdcConfig} from "../config/sdc-config.config";
import { HttpClient } from "@angular/common/http";
import { HttpHelperService } from "./http-hepler.service";
/**
 * User Service provides CRUD for Users. See authentication service for authentication/login.
 */
@Injectable()
export class UserService { 
    private url:string;

    constructor(private http: HttpClient,
                @Inject(SdcConfigToken) private sdcConfig:ISdcConfig) {
        this.url = this.sdcConfig.api.root + this.sdcConfig.api.GET_user;
    }


    public getAllUsers() :Observable<IUserProperties[]> {
        return this.http.get(
            this.sdcConfig.api.root + this.sdcConfig.api.GET_all_users
        ).map(resp => <IUserProperties[]> resp) ;
    }

    public getUser(userId:string) :Observable<IUserProperties> {
        return this.http.get(
            HttpHelperService.replaceUrlParams(this.url, { id: userId })
        ).map(resp => <IUserProperties> resp);
    }
}
