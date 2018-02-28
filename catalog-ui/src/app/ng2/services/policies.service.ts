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
import {SdcConfigToken, ISdcConfig} from "../config/sdc-config.config";


@Injectable()
export class PoliciesService {
    protected baseUrl;

    private mapApiDirections = {
        'RESOURCE':'resources',
        'SERVICE':'services'
    }

    constructor(private http: HttpService, @Inject(SdcConfigToken) sdcConfig:ISdcConfig) {
        this.baseUrl = sdcConfig.api.root ;
    }

    public createPolicyInstance(entityType:string, id:string, policyType:string) {
        return this.http.post(this.baseUrl + '/v1/catalog/' + this.mapApiDirections[entityType] + '/' + id +'/policies/' + policyType, {}).map(resp => {
            return resp.json(); 
        });
    }

}

