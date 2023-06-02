/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {InputOperationParameter} from "../../../../../../models/interfaceOperation";
import {DataTypeModel} from "../../../../../../models/data-types";
import {DerivedPropertyType} from "../../../../../../models/properties-inputs/property-be-model";
import {SubPropertyToscaFunction} from "../../../../../../models/sub-property-tosca-function";
import {PROPERTY_DATA, PROPERTY_TYPES} from "../../../../../../utils/constants";
import {InstanceFeDetails} from "../../../../../../models/instance-fe-details";
import {ToscaFunction} from "../../../../../../models/tosca-function";
import {CustomToscaFunction} from "../../../../../../models/default-custom-functions";
import {ToscaFunctionType} from 'app/models/tosca-function-type.enum';

@Component({
  selector: 'input-list',
  templateUrl: './input-list.component.html',
  styleUrls: ['./input-list.component.less']
})
export class InputListComponent {

  @Input() set inputs(inputs: Array<InputOperationParameter>) {
    this._inputs = new Array<InputOperationParameter>();
    if (inputs) {
      inputs.forEach(input => {
        const inputCopy = new InputOperationParameter(input);
        this.initValue(inputCopy);

        this._inputs.push(inputCopy);
      });
    }
  }
  @Input() dataTypeMap: Map<string, DataTypeModel>;
  @Input() isViewOnly: boolean;
  @Input() title: string;
  @Input() emptyMessage: string;
  @Input() showToscaFunctionOption: boolean = false;
  @Input() allowDeletion: boolean = false;
  @Input() componentInstanceMap: Map<string, InstanceFeDetails> = new Map();
  @Input() customToscaFunctions: Array<CustomToscaFunction> = [];
  @Output('onInputsValidityChange') inputsValidityChangeEvent: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output('onValueChange') inputValueChangeEvent: EventEmitter<InputOperationParameter> = new EventEmitter<InputOperationParameter>();
  @Output('onDelete') inputDeleteEvent: EventEmitter<string> = new EventEmitter<string>();

  _inputs: Array<InputOperationParameter> = [];

  getDataType(type: string): DataTypeModel {
    return this.dataTypeMap.get(type);
  }

  private initValue(input: InputOperationParameter): void {
    if (input.value) {
      try {
        input.value = JSON.parse(input.value);
      } catch (e) {
        console.debug('Could not parse value', input.value, e);
      }
      return;
    }

    if (input.toscaDefaultValue) {
      try {
        input.value = JSON.parse(input.toscaDefaultValue);
        return;
      } catch (e) {
        console.debug('Could not parse value', input.value, e);
      }
    }

    if (this.isTypeComplex(input.type) || this.isTypeMap(input.type)) {
      input.value = {};
    } else if (this.isTypeList(input.type)) {
      input.value = [];
    } else {
      input.value = undefined;
    }
  }

  getType(typeName: string): DerivedPropertyType {
    if (PROPERTY_DATA.SIMPLE_TYPES.indexOf(typeName) > -1) {
      return DerivedPropertyType.SIMPLE;
    } else if (typeName === PROPERTY_TYPES.LIST) {
      return DerivedPropertyType.LIST;
    } else if (typeName === PROPERTY_TYPES.MAP) {
      return DerivedPropertyType.MAP;
    } else {
      return DerivedPropertyType.COMPLEX;
    }
  }

  isTypeSimple(typeName: string): boolean {
    return this.getType(typeName) == DerivedPropertyType.SIMPLE;
  }

  isTypeList(typeName: string): boolean {
    return this.getType(typeName) == DerivedPropertyType.LIST;
  }

  isTypeMap(typeName: string): boolean {
    return this.getType(typeName) == DerivedPropertyType.MAP;
  }

  isTypeComplex(typeName: string): boolean {
    return !this.isTypeSimple(typeName) && !this.isTypeList(typeName) && !this.isTypeMap(typeName);
  }

  onValueChange($event: any) {
    const inputOperationParameter = this._inputs.find(input => input.name == $event.name);
    if (!inputOperationParameter.subPropertyToscaFunctions) {
      inputOperationParameter.subPropertyToscaFunctions = [];
    }
    if (inputOperationParameter) {
      inputOperationParameter.valid = true;
      if ($event.isToscaFunction) {
        inputOperationParameter.toscaFunction = $event.value;
        if (!inputOperationParameter.toscaFunction) {
          inputOperationParameter.valid = false;
        }
      } else if (this.isTypeComplex(inputOperationParameter.type)) {
        this.setComplexType($event, inputOperationParameter);
      } else {
        inputOperationParameter.value = $event.value;
        inputOperationParameter.toscaFunction = null;
      }
    }
      this.inputsValidityChangeEvent.emit(this._inputs.every(input => input.valid === true));
      this.inputValueChangeEvent.emit(new InputOperationParameter(inputOperationParameter));
  }

  private setComplexType ($event, inputOperationParameter): void {
    $event.value.forEach((value, key) => {
      let subPropertyToscaFunction = inputOperationParameter.subPropertyToscaFunctions.find(existingSubPropertyToscaFunction =>
        this.areEqual(existingSubPropertyToscaFunction.subPropertyPath, [key])
      );
      let valueKeys = value instanceof Object ? Object.keys(value) : undefined;
      if (value && value.type && value.type in ToscaFunctionType) {
        if (!subPropertyToscaFunction){
          subPropertyToscaFunction = new SubPropertyToscaFunction();
          inputOperationParameter.subPropertyToscaFunctions.push(subPropertyToscaFunction);
        }
        subPropertyToscaFunction.toscaFunction = value;
        $event.value[key] = (value as ToscaFunction).buildValueObject();
        let array: string[] = [];
        array.push(key)
        subPropertyToscaFunction.subPropertyPath = array;
      } else if (subPropertyToscaFunction && (!valueKeys || !valueKeys.every(value => value.toUpperCase() in ToscaFunctionType))) {
        inputOperationParameter.subPropertyToscaFunctions.splice(inputOperationParameter.subPropertyToscaFunctions.indexOf(subPropertyToscaFunction), 1)
      }
    })
}

  private areEqual(array1: string[], array2: string[]): boolean {
    return array1 && array2 && array1.length === array2.length && array1.every(function(value, index) { return value === array2[index]})
}

  onDelete(inputName: string) {
    this.inputDeleteEvent.emit(inputName);
  }

}
