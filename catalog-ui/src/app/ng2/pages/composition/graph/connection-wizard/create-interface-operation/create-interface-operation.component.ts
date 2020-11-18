/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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

import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {IDropDownOption} from "onap-ui-angular/dist/form-elements/dropdown/dropdown-models";
import {DropDownComponent} from "onap-ui-angular/dist/components";
import {PropertyAssignment} from "../../../../../../models/properties-inputs/property-assignment";
import {Observable} from "rxjs";
import {Operation} from "./model/operation";
import {FormControl, FormGroup, Validators} from "@angular/forms";

@Component({
  selector: 'app-create-interface-operation',
  templateUrl: './create-interface-operation.component.html',
  styleUrls: ['./create-interface-operation.component.less']
})
export class CreateInterfaceOperationComponent implements OnInit {
  @Input('interfaceTypeMap') interfaceTypeMap: Map<string, Array<string>>;
  @Input('operation') private operation: Operation;
  @Output('addOperation') addOperation: EventEmitter<Operation> = new EventEmitter<Operation>();
  @ViewChild('operationDropdown') operationDropdown: DropDownComponent;

  form: FormGroup;
  isLoading: boolean;
  isReadOnly: boolean;
  currentInOutTab: string;
  interfaceTypeOptions: Array<TypedDropDownOption>;
  operationOptions: Array<TypedDropDownOption>;
  selectedInterfaceType: TypedDropDownOption;
  selectedOperation: TypedDropDownOption;
  implementation: string;
  inputs$: Observable<Array<PropertyAssignment>>;
  inputs: Array<PropertyAssignment>;
  inputErrorMap: Map<string, boolean>;
  validationMessageList: Array<string>;
  interfaceTypeFormCtrl: FormControl;
  operationTypeFormCtrl: FormControl;
  implementationFormCtrl: FormControl;

  TYPE_INPUT = 'Inputs';
  TYPE_OUTPUT = 'Outputs';


  constructor() {
    this.currentInOutTab = this.TYPE_INPUT;
    this.inputErrorMap = new Map<string, boolean>();
  }

  ngOnInit() {
    if (!this.operation) {
      this.operation = new Operation();
    }

    this.interfaceTypeFormCtrl = new FormControl(this.operation.interfaceType, [
      Validators.required
    ]);
    this.operationTypeFormCtrl = new FormControl(this.operation.operationType, [
      Validators.required
    ]);
    this.implementationFormCtrl = new FormControl(this.operation.implementation, [
      Validators.required
    ]);
    this.form = new FormGroup({
      interfaceType: this.interfaceTypeFormCtrl,
      operationType: this.operationTypeFormCtrl,
      implementation: this.implementationFormCtrl
    });

    this.isLoading = true;
    this.isReadOnly = false;
    this.loadInterfaceOptions();
    this.loadOperationOptions();
    this.loadOperationInputs();
    this.isLoading = false;
  }

  private loadInterfaceOptions() {
    this.interfaceTypeOptions = new Array<TypedDropDownOption>();
    const interfaceTypeList = Array.from(this.interfaceTypeMap.keys());
    interfaceTypeList.sort();
    interfaceTypeList.forEach(interfaceType => {
      this.interfaceTypeOptions.push(this.createInterfaceDropdownOption(interfaceType));
    });
  }

  private loadOperationOptions() {
    this.operationOptions = new Array<TypedDropDownOption>();
    if (!this.selectedInterfaceType) {
      return;
    }

    const operationArray: Array<string> = this.interfaceTypeMap.get(this.selectedInterfaceType.value);
    operationArray.sort();
    operationArray.forEach(operationName =>
        this.operationOptions.push(new TypedDropDownOption(operationName, operationName))
    );
    this.operationDropdown.allOptions = <IDropDownOption[]> this.operationOptions;
  }

  private loadOperationInputs() {
    this.inputs = new Array<PropertyAssignment>();
    this.inputs$ = Observable.of(this.inputs);
  }

  descriptionValue(): string {
    return this.operation.description;
  }

  addInput() {
    this.inputs.push(new PropertyAssignment('string'));
  }

  onSelectInterfaceType(selectedOption: IDropDownOption) {
    this.selectedInterfaceType = <TypedDropDownOption> selectedOption;
    this.operation.interfaceType = selectedOption.value;
    this.interfaceTypeFormCtrl.setValue(this.operation.interfaceType);
    this.loadOperationOptions();
  }

  private createInterfaceDropdownOption(type: string) {
    let label = type;
    const lastDot = label.lastIndexOf('.');
    if (lastDot > -1) {
      label = label.substr(lastDot + 1);
    }
    return new TypedDropDownOption(type, label);
  }

  onSelectOperation(selectedOption: IDropDownOption) {
    this.selectedOperation = <TypedDropDownOption> selectedOption;
    this.operation.operationType = selectedOption.value;
    this.operationTypeFormCtrl.setValue(this.operation.operationType);
  }

  onChangeImplementation(implementation: string) {
    this.implementation = implementation ? implementation.trim() : null;
    this.operation.implementation = this.implementation;
  }

  onDeleteInput(input: PropertyAssignment): void {
    const index = this.inputs.indexOf(input);
    this.inputs.splice(index, 1);
  }

  createOperation() {
    this.form.updateValueAndValidity();
    if (this.isValid()) {
      this.operation.interfaceType = this.interfaceTypeFormCtrl.value;
      this.operation.operationType = this.operationTypeFormCtrl.value;
      this.operation.implementation = this.implementationFormCtrl.value;
      if (this.inputs) {
        this.operation.inputs = this.inputs;
      }
      this.addOperation.emit(this.operation);
    }
  }

  onClickCancel() {
    this.addOperation.emit(null);
  }

  private isValid(): boolean {
    if (this.form.invalid) {
      return false;
    }

    return this.validateInputs();
  }

  validateInputs(): boolean {
    this.inputErrorMap = new Map<string, boolean>();
    if (!this.inputs) {
      return true;
    }

    const inputNameSet = new Set<string>();
    this.inputs.forEach(value => {
      if (value.name) {
        value.name = value.name.trim();
        if (!value.name) {
          this.inputErrorMap.set('invalidName', true);
        }
      } else {
        this.inputErrorMap.set('invalidName', true);
      }
      if (value.value) {
        value.value = value.value.trim();
      }
      //for later check of duplicate input name
      inputNameSet.add(value.name);
    });

    if (inputNameSet.size != this.inputs.length) {
      this.inputErrorMap.set('duplicatedName', true);
    }

    return this.inputErrorMap.size == 0;
  }

}

class DropDownOption implements IDropDownOption {
  value: string;
  label: string;

  constructor(value: string, label?: string) {
    this.value = value;
    this.label = label || value;
  }
}

class TypedDropDownOption extends DropDownOption {
  type: string;

  constructor(value: string, label?: string, type?: string) {
    super(value, label);
    this.type = type;
  }
}
