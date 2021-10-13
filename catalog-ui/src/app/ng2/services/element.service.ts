/*
* ============LICENSE_START=======================================================
*  Copyright (C) 2021 Nordix Foundation. All rights reserved.
*  ================================================================================
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  SPDX-License-Identifier: Apache-2.0
*  ============LICENSE_END=========================================================
*/

import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { ISdcConfig, SdcConfigToken } from '../config/sdc-config.config';
import {map} from "rxjs/operators";

@Injectable()
export class ElementService {

    protected baseUrl;

    constructor(protected http: HttpClient, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
        this.baseUrl = sdcConfig.api.root;
    }

    getCategoryBaseTypes(categoryName: string, modelName: string): Observable<ListBaseTypesResponse> {
	    let modelQueryParam: string = modelName ? '?model=' + modelName : '';
        return this.http.get<ListBaseTypesResponse>(this.baseUrl + "/v1/category/services/" + categoryName + "/baseTypes" + modelQueryParam);
    }

}

