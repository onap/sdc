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

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import { Response } from '@angular/http';
import {Service} from "app/models";
import { downgradeInjectable } from '@angular/upgrade/static';
import {sdc2Config} from "../../../../main";
import {InterceptorService} from "ng2-interceptors/index";


@Injectable()
export class ServiceServiceNg2 {

    protected baseUrl = "";

    constructor(private http: InterceptorService) {
        this.baseUrl = sdc2Config.api.root + sdc2Config.api.component_api_root;
    }

    validateConformanceLevel(service: Service): Observable<boolean> {

        return this.http.get(this.baseUrl + service.getTypeUrl() + service.uuid + '/conformanceLevelValidation')
            .map((res: Response) => {
                return res.json();
            });
    }

}

angular.module('Sdc.Services').factory('ServiceServiceNg2', downgradeInjectable(ServiceServiceNg2)); // This is in order to use the service in angular1 till we finish remove all angular1 code
