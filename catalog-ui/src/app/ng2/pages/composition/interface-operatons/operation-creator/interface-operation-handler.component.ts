/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
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

import {Component} from '@angular/core';
import {UIInterfaceModel} from "../interface-operations.component";
import {
    InputOperationParameter,
    InterfaceOperationModel,
    IOperationParamsList
} from "../../../../../models/interfaceOperation";
import {TranslateService} from "../../../../shared/translator/translate.service";

@Component({
    selector: 'operation-handler',
    templateUrl: './interface-operation-handler.component.html',
    styleUrls: ['./interface-operation-handler.component.less'],
    providers: [TranslateService]
})

export class InterfaceOperationHandlerComponent {

    input: {
        selectedInterface: UIInterfaceModel;
        selectedInterfaceOperation: InterfaceOperationModel;
        validityChangedCallback: Function;
    };

    interfaceType: string;
    interfaceOperationName: string;
    operationToUpdate: InterfaceOperationModel;
    inputs: Array<InputOperationParameter> = [];
    isLoading: boolean = false;
    readonly: boolean;

    ngOnInit() {
        this.interfaceType = this.input.selectedInterface.displayType();
        this.operationToUpdate = new InterfaceOperationModel(this.input.selectedInterfaceOperation);
        this.operationToUpdate.interfaceId = this.input.selectedInterface.uniqueId;
        this.operationToUpdate.interfaceType = this.input.selectedInterface.type;
        if (!this.operationToUpdate.inputs) {
            this.operationToUpdate.inputs = new class implements IOperationParamsList {
                listToscaDataDefinition: Array<InputOperationParameter> = [];
            }
        }
        this.inputs = this.operationToUpdate.inputs.listToscaDataDefinition;
        this.removeImplementationQuote();
        this.validityChanged();
    }

    onAddInput(inputOperationParameter?: InputOperationParameter): void {
        let newInput = new InputOperationParameter(inputOperationParameter)
        newInput.type = "string";
        newInput.inputId = this.generateUniqueId();
        this.inputs.push(newInput);
        this.validityChanged();
    }

    onRemoveInput = (inputParam: InputOperationParameter): void => {
        let index = this.inputs.indexOf(inputParam);
        this.inputs.splice(index, 1);
        this.validityChanged();
    }

    private generateUniqueId = (): string => {
        let result = '';
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        const charactersLength = characters.length;
        for (let i = 0; i < 36; i++ ) {
            result += characters.charAt(Math.floor(Math.random() * charactersLength * 1));
        }
        return result;
    }

    validityChanged = () => {
        let validState = this.checkFormValidForSubmit();
        this.input.validityChangedCallback(validState);
        if (validState) {
            this.readonly = false;
        }
    }

    onDescriptionChange= (value: any): void => {
        this.operationToUpdate.description = value;
    }

    private checkFormValidForSubmit = (): boolean => {
        return this.operationToUpdate.name && this.isParamsValid();
    }

    private isParamsValid = (): boolean => {
        const isInputValid = (input) => input.name && input.inputId;
        const isValid = this.inputs.every(isInputValid);
        if (!isValid) {
            this.readonly = true;
        }
        return isValid;
    }

    private removeImplementationQuote(): void {
        if (!this.operationToUpdate.implementation
            || !this.operationToUpdate.implementation.artifactName) {
            return;
        }

        let implementation = this.operationToUpdate.implementation.artifactName.trim();

        if (implementation.startsWith("'") && implementation.endsWith("'")) {
            this.operationToUpdate.implementation.artifactName = implementation.slice(1, -1);
        }
    }

}
