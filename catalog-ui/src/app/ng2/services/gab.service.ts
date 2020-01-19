/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs';
import { ISdcConfig, SdcConfigToken } from '../config/sdc-config.config';

// tslint:disable-next-line:interface-name
export interface IServerResponse {
  data: [{ [key: string]: string }];
}

export class GabRequest {
  constructor(public fields: string[],
              public parentId: string,
              public artifactUniqueId: string) {

  }
}

// tslint:disable-next-line:max-classes-per-file
@Injectable()
export class GabService {
  baseUrl: string;
  gabUrl: string;

  constructor(@Inject(SdcConfigToken) sdcConfig: ISdcConfig, protected http: HttpClient) {
    this.baseUrl = sdcConfig.api.root;
    this.gabUrl = sdcConfig.api.POST_GAB_Search;
  }

  public getArtifact(artifactUniqueId: string, resourceId: string, columns: string[]): Observable<Response> {
    const finalUrl: string = this.baseUrl + this.gabUrl;
    const request: GabRequest = {
      fields: columns,
      parentId: resourceId,
      artifactUniqueId
    };

    return this.http.post<Response>(finalUrl, request);
  }
}