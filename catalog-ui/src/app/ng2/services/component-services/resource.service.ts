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

import {Inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ISdcConfig, SdcConfigToken} from "../../config/sdc-config.config";
import {Observable} from "rxjs/Observable";
import {ComponentMetadata} from "../../../models/component-metadata";
import {ComponentLifecycleState} from "../../../models/component-lifecycle-state.enum";

@Injectable()
export class ResourceServiceNg2 {

  private readonly baseUrl: string;

  constructor(protected http: HttpClient, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
    this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
  }

  public checkout(componentUniqueId: string): Observable<ComponentMetadata> {
    return this.changeLifecycleState(componentUniqueId, ComponentLifecycleState.CHECKOUT);
  }

  private changeLifecycleState(componentUniqueId: string, state: ComponentLifecycleState): Observable<ComponentMetadata> {
    const url: string = this.baseUrl + 'resources/' + componentUniqueId + '/lifecycleState/' + state;
    return this.http.post<ComponentMetadata>(url, {}).map(value => {
          return value;
        }
    );
  }

}
