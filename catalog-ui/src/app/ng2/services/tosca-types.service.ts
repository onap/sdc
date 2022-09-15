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

import {HttpClient} from '@angular/common/http';
import {Inject, Injectable} from '@angular/core';
import {Response} from '@angular/http';
import {
  CapabilityTypeModel,
  CapabilityTypesMap,
  IComponentsArray,
  NodeTypesMap,
  RelationshipTypesMap
} from 'app/models';
import {Observable} from 'rxjs/Observable';
import {ISdcConfig, SdcConfigToken} from '../config/sdc-config.config';
import 'rxjs/add/operator/toPromise';

declare var angular: angular.IAngularStatic;

@Injectable()
export class ToscaTypesServiceNg2 {

  protected baseUrl;

  constructor(protected http: HttpClient, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
    this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
  }

  async fetchRelationshipTypes(modelName: string): Promise<RelationshipTypesMap> {
    if(modelName) {
      return this.http.get<RelationshipTypesMap>(this.baseUrl + 'relationshipTypes', {params: {model: modelName}}).toPromise();
    }
    return this.http.get<RelationshipTypesMap>(this.baseUrl + 'relationshipTypes').toPromise();
  }

  async fetchNodeTypes(modelName: string): Promise<NodeTypesMap> {
    if(modelName) {
      return this.http.get<NodeTypesMap>(this.baseUrl + 'nodeTypes', {params: {model: modelName}}).toPromise();
    }
    return this.http.get<NodeTypesMap>(this.baseUrl + 'nodeTypes').toPromise();
  }

    async fetchCapabilityTypes(modelName: string): Promise<CapabilityTypesMap> {
    if(modelName) {
      return this.http.get<CapabilityTypesMap>(this.baseUrl + 'capabilityTypes', {params: {model: modelName}}).toPromise();
    }
    return this.http.get<CapabilityTypesMap>(this.baseUrl + 'capabilityTypes').toPromise();
    }
}

