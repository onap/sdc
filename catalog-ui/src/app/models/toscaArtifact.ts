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

import {PropertyBEModel} from "./properties-inputs/property-be-model";

export class ToscaArtifactModel {
  type: string;
  file: string;
  repository: string
  description: string
  deploy_path: string;
  artifact_version: string;
  checksum: string;
  checksum_algorithm: string;
  propertiesData: Array<PropertyBEModel>;

  constructor(param?: any) {
    this.type = param.type;
    this.file = param.file;
    this.repository = param.repository;
    this.description = param.description;
    this.deploy_path = param.deploy_path;
    this.artifact_version = param.artifact_version;
    this.checksum = param.checksum;
    this.checksum_algorithm = param.checksum_algorithm;
    this.propertiesData = param.propertiesData;
  }

}
