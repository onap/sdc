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

import {PropertyBEModel} from 'app/models';
/**
 * Created by rc2122 on 6/1/2017.
 */
export class InputBEModel extends PropertyBEModel {

    inputPath: string;
    inputs: Array<ComponentInstanceModel>;
    instanceUniqueId: string;
    ownerId: string;
    propertyId: string;
    properties: Array<ComponentInstanceModel>;

    constructor(input?: InputBEModel) {
        super(input);
        this.instanceUniqueId = input.instanceUniqueId;
        this.propertyId = input.propertyId;
        this.properties = input.properties;
        this.inputs = input.inputs;
        this.ownerId = input.ownerId;
        this.inputPath = input.inputPath;
    }

    public toJSON = (): any => {
    };

}

export interface ComponentInstanceModel extends InputBEModel {
    componentInstanceId:string;
    componentInstanceName: string;
}
