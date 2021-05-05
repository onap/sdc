/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

import {TestBed} from '@angular/core/testing';
import {ISdcConfig, SdcConfigToken} from "../../config/sdc-config.config";
import {ComponentInstanceServiceNg2} from "./component-instance.service";
import {Capability} from "../../../models/capability";
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";

describe('ComponentInstanceServiceNg2', () => {
  let httpTestingController: HttpTestingController;
  let componentInstanceService: ComponentInstanceServiceNg2;
  let rootApi: string = 'http://localhost/'
  let componentApiRoot: string = 'catalog/'
  beforeEach(() => {
    const sdcConfigToken: Partial<ISdcConfig> = {
      'api': {
        'root': rootApi,
        'component_api_root': componentApiRoot,
      }
    };
    TestBed.configureTestingModule({
      providers: [ComponentInstanceServiceNg2,
        {provide: SdcConfigToken, useValue: sdcConfigToken}
      ],
      imports: [HttpClientTestingModule]
    });
    httpTestingController = TestBed.get(HttpTestingController);
    componentInstanceService = TestBed.get(ComponentInstanceServiceNg2);
  });

  it('should be created', () => {
    expect(componentInstanceService).toBeTruthy();
  });

  it('updateInstanceCapability call should return the expected data', () => {
    const capabilityToUpdate = new Capability();
    capabilityToUpdate.type = "tosca.capabilities.Scalable";
    capabilityToUpdate.name = "capScalable";
    capabilityToUpdate.ownerId = "191f8a83-d362-4db4-af30-75d71a55c959.a822dd1c-3560-47ea-b8a2-f557fed5e186.vfcapreq10";
    capabilityToUpdate.uniqueId = "2047eb3c-de31-4413-a358-8710a3dd2670";
    capabilityToUpdate.external = true;

    const componentTypeUrl = "services/";
    let actualCapability: Capability;
    componentInstanceService.updateInstanceCapability(componentTypeUrl, "componentId", "componentInstanceId", capabilityToUpdate)
    .subscribe(capability => {
      actualCapability = capability;
    });

    const request =
        httpTestingController.expectOne(`${rootApi}${componentApiRoot}${componentTypeUrl}componentId/componentInstances/componentInstanceId/capability/`);

    expect(request.request.method).toEqual('PUT');

    request.flush(capabilityToUpdate);
    expect(actualCapability.name).toEqual(capabilityToUpdate.name);
    expect(actualCapability.type).toEqual(capabilityToUpdate.type);
    expect(actualCapability.ownerId).toEqual(capabilityToUpdate.ownerId);
    expect(actualCapability.uniqueId).toEqual(capabilityToUpdate.uniqueId);
    expect(actualCapability.external).toEqual(capabilityToUpdate.external);
  });

});
