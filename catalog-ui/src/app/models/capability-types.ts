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

import {ToscaPresentationData} from "./tosca-presentation";

export class CapabilityTypesMap {
    capabilityTypesMap: CapabilityTypesMapData;

    constructor(capabilityTypesMap: CapabilityTypesMapData) {
        this.capabilityTypesMap = capabilityTypesMap;
    }
}

export class CapabilityTypesMapData {
    [capabilityTypeId: string]: CapabilityTypeModel;
}

export class CapabilityTypeModel {
    derivedFrom: string;
    toscaPresentation: ToscaPresentationData;

    constructor(capabilityType?: CapabilityTypeModel) {
        if (capabilityType) {
            this.derivedFrom = capabilityType.derivedFrom;
            this.toscaPresentation = capabilityType.toscaPresentation;
        }
    }
}