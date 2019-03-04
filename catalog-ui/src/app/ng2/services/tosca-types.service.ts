/*!
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import {Injectable, Inject} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {HttpService} from './http.service';
import {SdcConfigToken, ISdcConfig} from "../config/sdc-config.config";
import {CapabilityTypesMap, NodeTypesMap, RelationshipTypesMap} from "app/models";
import {Response} from '@angular/http';

declare var angular: angular.IAngularStatic;

@Injectable()
export class ToscaTypesServiceNg2 {

    protected baseUrl;

    constructor(protected http: HttpService, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
    }

    fetchRelationshipTypes(): Observable<RelationshipTypesMap> {
        return this.http.get(this.baseUrl + 'relationshipTypes')
            .map((res: Response) => {
                return res.json();
            });
    }

    fetchNodeTypes(): Observable<NodeTypesMap> {
        return this.http.get(this.baseUrl + 'nodeTypes')
            .map((res: Response) => {
                return res.json();
            });
    }

    fetchCapabilityTypes(): Observable<CapabilityTypesMap> {
        return this.http.get(this.baseUrl + 'capabilityTypes')
            .map((res: Response) => {
                return res.json();
            });
    }
}
