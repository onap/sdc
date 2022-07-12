/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 Nordix Foundation. All rights reserved.
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

import { AttributeBEModel } from './attribute-be-model';

export class OutputBEModel extends AttributeBEModel {

  outputPath: string;
  instanceUniqueId: string;
  ownerId: string;
  attributeId: string;
  attribute: OutputComponentInstanceModel;

  constructor(output?: OutputBEModel) {
    super(output);
    this.instanceUniqueId = output.instanceUniqueId;
    this.attributeId = output.attributeId;
    this.attribute = output.attribute;
    this.ownerId = output.ownerId;
    this.outputPath = output.outputPath;
  }

}

export interface OutputComponentInstanceModel extends OutputBEModel {
  componentInstanceId: string;
  componentInstanceName: string;
}
