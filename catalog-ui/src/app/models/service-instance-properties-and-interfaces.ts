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

import {PropertyModel, InputModel, InterfaceModel} from 'app/models';

export class ServiceInstanceObject {
    id: string;
    name: string;
    properties: Array<PropertyModel> = [];
    inputs: Array<InputModel> = [];
    interfaces: Array<InterfaceModel> = [];

    constructor(input?:any) {
        if(input) {
            this.id = input.id;
            this.name = input.name;
            this.properties = input.properties;
            this.inputs = input.inputs;
            this.interfaces = _.map(input.interfaces, interf => new InterfaceModel(interf));
        }
    }
}
