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

import { Injectable } from '@angular/core';
import { InputBEModel, InputFEModel } from "app/models";
import { DataTypeService } from "app/ng2/services/data-type.service";

@Injectable()
export class InputsUtils {

    constructor(private dataTypeService:DataTypeService) {}

    public initDefaultValueObject = (input: InputFEModel): void => {
        input.resetDefaultValueObjValidation();
        input.defaultValueObj = input.getDefaultValueObj();
        input.updateDefaultValueObjOrig();
    };

    public resetInputDefaultValue = (input: InputFEModel, newDefaultValue: string): void => {
        input.defaultValue = newDefaultValue;
        this.initDefaultValueObject(input);
    }

    public convertInputBEToInputFE = (input: InputBEModel): InputFEModel => {
        const newFEInput: InputFEModel = new InputFEModel(input); //Convert input to FE
        this.initDefaultValueObject(newFEInput);
        return newFEInput;
    }

}
