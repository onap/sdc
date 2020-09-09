/*
* ============LICENSE_START=======================================================
*  Copyright (C) 2020 Nordix Foundation. All rights reserved.
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

import {PropertyBEModel} from 'app/models';

export class OutputBEModel extends PropertyBEModel {

  outputPath: string;
  outputs: Array<OutputComponentInstanceModel>;
  instanceUniqueId: string;
  ownerId: string;
  propertyId: string;
  properties: Array<OutputComponentInstanceModel>;

  constructor(output?: OutputBEModel) {
    super(output);
    this.instanceUniqueId = output.instanceUniqueId;
    this.propertyId = output.propertyId;
    this.properties = output.properties;
    this.outputs = output.outputs;
    this.ownerId = output.ownerId;
    this.outputPath = output.outputPath;
  }

}

export interface OutputComponentInstanceModel extends OutputBEModel {
  componentInstanceId: string;
  componentInstanceName: string;
}
