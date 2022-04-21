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

'use strict';

import {ArtifactModel} from "./artifacts";
import {SchemaPropertyGroupModel} from "./schema-property";
import {PROPERTY_DATA, PROPERTY_TYPES} from "../utils/constants";

export class InputOperationParameter {
    name: string;
    type: string;
    schema: SchemaPropertyGroupModel;
    inputId: string;
    toscaDefaultValue?: string;
    value?: any;

    constructor(param?: any) {
        if (param) {
            this.name = param.name;
            this.type = param.type;
            this.schema = param.schema;
            this.inputId = param.inputId;
            this.toscaDefaultValue = param.toscaDefaultValue;
            this.value = param.value;
        }
    }

    public getDefaultValue(): any {
        if (this.isTypeNotSimple()) {
            if (this.toscaDefaultValue) {
                this.toscaDefaultValue = JSON.parse(this.toscaDefaultValue);
                return JSON.parse(this.toscaDefaultValue);
            }
            switch (this.type) {
                case PROPERTY_TYPES.LIST:
                    return [];
                default:
                    return {};
            }
        }

        return this.toscaDefaultValue;
    }

    private isTypeNotSimple() {
        return PROPERTY_DATA.SIMPLE_TYPES.indexOf(this.type) == -1;
    }
}

export class PropertyOperationParameter {
    name: string;
    type: string;
    value?: string;
    propertyId: string;

    constructor(param?: any) {
        if (param) {
            this.name = param.name;
            this.type = param.type;
            this.value = param.value;
            this.propertyId = param.propertyId;
        }
    }
}

export interface IOperationParamsList {
    listToscaDataDefinition: Array<InputOperationParameter>;
}

export class BEInterfaceOperationModel {
    name: string;
    description: string;
    uniqueId: string;
    inputs: IOperationParamsList;
    implementation: ArtifactModel;

    constructor(operation?: any) {
        if (operation) {
            this.name = operation.name;
            this.description = operation.description;
            this.uniqueId = operation.uniqueId;
            this.inputs = operation.inputs;
            this.implementation = operation.implementation;
        }
    }
}

export class InterfaceOperationModel extends BEInterfaceOperationModel {
    isCollapsed: boolean = true;
    isEllipsis: boolean;
    MAX_LENGTH = 75;

    interfaceType: string;
    interfaceId: string;
    operationType: string;
    description: string;
    uniqueId: string;
    inputParams: IOperationParamsList;
    implementation: ArtifactModel;

    constructor(operation?: any) {
        super(operation);
        if (operation) {
            this.interfaceId = operation.interfaceId;
            this.interfaceType = operation.interfaceType;
            this.description = operation.description;
            this.operationType = operation.operationType;
            this.uniqueId = operation.uniqueId;
            if (operation.inputParams && operation.inputParams.listToscaDataDefinition) {
                const listToscaDataDefinition: InputOperationParameter[] = [];
                operation.inputParams.listToscaDataDefinition.forEach(inputOperation => {
                    listToscaDataDefinition.push(new InputOperationParameter(inputOperation));
                });
                this.inputParams = <IOperationParamsList> {
                    'listToscaDataDefinition': listToscaDataDefinition
                };
            }
            if (operation.implementation) {
                this.implementation = new ArtifactModel(operation.implementation);
            }
        }
    }

    public displayType(): string {
        return displayType(this.interfaceType);
    }

    getDescriptionEllipsis(): string {
        if (this.isCollapsed && this.description.length > this.MAX_LENGTH) {
            return this.description.substr(0, this.MAX_LENGTH - 3) + '...';
        }
        return this.description;
    }

    toggleCollapsed(e) {
        e.stopPropagation();
        this.isCollapsed = !this.isCollapsed;
    }

}

export class ComponentInterfaceDefinitionModel {
    type: string;
    uniqueId: string;
    operations: Array<InterfaceOperationModel>;

    constructor(interfaceOperation?: any) {
        if (interfaceOperation) {
            this.type = interfaceOperation.type;
            this.uniqueId = interfaceOperation.uniqueId;
            this.operations = interfaceOperation.operations;
        }
    }

    public displayType(): string {
        return displayType(this.type);
    }
}

const displayType = (type:string) => type && type.substr(type.lastIndexOf('.') + 1);
