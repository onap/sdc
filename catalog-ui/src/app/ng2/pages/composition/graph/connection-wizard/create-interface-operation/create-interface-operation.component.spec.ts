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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateInterfaceOperationComponent} from './create-interface-operation.component';
import {SdcUiComponentsModule} from "onap-ui-angular/dist";
import {CreateInputRowComponent} from "./create-input-row/create-input-row.component";
import {FormControl, ReactiveFormsModule} from "@angular/forms";
import {TabModule} from "../../../../../components/ui/tabs/tabs.module";
import {UiElementsModule} from "../../../../../components/ui/ui-elements.module";
import {TranslateService} from "../../../../../shared/translator/translate.service";
import {TranslatePipe} from "../../../../../shared/translator/translate.pipe";
import {Operation} from "./model/operation";
import {PropertyAssignment} from "../../../../../../models/properties-inputs/property-assignment";

describe('CreateInterfaceOperationComponent', () => {
  let component: CreateInterfaceOperationComponent;
  let fixture: ComponentFixture<CreateInterfaceOperationComponent>;
  const interfaceTypeFormName = 'interfaceType';
  const operationTypeFormName = 'operationType';
  const implementationFormName = 'implementation';

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CreateInterfaceOperationComponent, CreateInputRowComponent, TranslatePipe],
      imports: [SdcUiComponentsModule, ReactiveFormsModule, TabModule,
        UiElementsModule],
      providers: [
        {provide: TranslateService, useValue: {}}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateInterfaceOperationComponent);
    component = fixture.componentInstance;
    component.interfaceTypeMap = new Map<string, Array<string>>();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('form is invalid when empty', () => {
    component.ngOnInit();
    expect(component.form.valid).toBeFalsy();
  });

  it('interface type field validity', () => {
    component.ngOnInit();
    expect(component.form.valid).toBeFalsy();
    expect(component.form.contains(interfaceTypeFormName)).toBeTruthy();
    assertRequiredFormCtrl(component.interfaceTypeFormCtrl, 'anInterfaceType');
  });

  it('operation type field validity', () => {
    component.ngOnInit();
    expect(component.form.valid).toBeFalsy();
    expect(component.form.contains(operationTypeFormName)).toBeTruthy();
    assertRequiredFormCtrl(component.operationTypeFormCtrl, 'anOperationType');
  });

  it('implementation type field validity', () => {
    component.ngOnInit();
    expect(component.form.valid).toBeFalsy();
    expect(component.form.contains(implementationFormName)).toBeTruthy();
    assertRequiredFormCtrl(component.implementationFormCtrl, 'just/an/implementation.sh');
  });

  it('test loadInterfaceOptions on init', () => {
    const interfaceTypeMap = new Map<string, Array<string>>();
    interfaceTypeMap.set('interface3', new Array<string>());
    interfaceTypeMap.set('interface4', new Array<string>());
    interfaceTypeMap.set('interface2', new Array<string>());
    interfaceTypeMap.set('interface1', new Array<string>());
    component.interfaceTypeMap = interfaceTypeMap;
    component.ngOnInit();
    expect(component.interfaceTypeOptions.length).toBe(4);
    expect(component.interfaceTypeOptions[0].label).toBe('interface1');
    expect(component.interfaceTypeOptions[0].value).toBe('interface1');
    expect(component.interfaceTypeOptions[1].label).toBe('interface2');
    expect(component.interfaceTypeOptions[1].value).toBe('interface2');
    expect(component.interfaceTypeOptions[2].label).toBe('interface3');
    expect(component.interfaceTypeOptions[2].value).toBe('interface3');
    expect(component.interfaceTypeOptions[3].label).toBe('interface4');
    expect(component.interfaceTypeOptions[3].value).toBe('interface4');
  });

  it('test loadOperationOptions on init', () => {
    const interfaceTypeMap = new Map<string, Array<string>>();
    const interface1Operations = new Array<string>('interface1Operation3',
        'interface1Operation1', 'interface1Operation2', 'interface1Operation4');
    interfaceTypeMap.set('interface1', interface1Operations);
    component.interfaceTypeMap = interfaceTypeMap;
    component.selectedInterfaceType = {
      value: 'interface1',
      type: null,
      label: 'interface1',
    }
    component.ngOnInit();
    expect(component.operationOptions.length).toBe(4);
    expect(component.operationOptions[0].label).toBe('interface1Operation1');
    expect(component.operationOptions[0].value).toBe('interface1Operation1');
    expect(component.operationOptions[1].label).toBe('interface1Operation2');
    expect(component.operationOptions[1].value).toBe('interface1Operation2');
    expect(component.operationOptions[2].label).toBe('interface1Operation3');
    expect(component.operationOptions[2].value).toBe('interface1Operation3');
    expect(component.operationOptions[3].label).toBe('interface1Operation4');
    expect(component.operationOptions[3].value).toBe('interface1Operation4');
  });

  it('test onSelectInterfaceType', () => {
    const interfaceTypeMap = new Map<string, Array<string>>();
    interfaceTypeMap.set('interface1', new Array<string>('', '', '', ''));
    interfaceTypeMap.set('interface2', new Array<string>());
    component.interfaceTypeMap = interfaceTypeMap;
    component.ngOnInit();
    const selectedInterfaceType = {
      value: 'interface1',
      type: null,
      label: 'interface1',
    }
    component.onSelectInterfaceType(selectedInterfaceType);
    expect(component.selectedInterfaceType).toBe(selectedInterfaceType);
    expect(component.interfaceTypeFormCtrl.value).toBe(selectedInterfaceType.value);
    expect(component.operationOptions.length).toBe(4);
  });

  it('test onSelectOperation', () => {
    component.ngOnInit();

    const selectedOperationType = {
      value: 'operation1',
      type: null,
      label: 'operation1',
    }
    component.onSelectOperation(selectedOperationType);
    expect(component.selectedOperation).toBe(selectedOperationType);
    expect(component.operationTypeFormCtrl.value).toBe(selectedOperationType.value);
  });

  it('test onChangeImplementation', () => {
    component.ngOnInit();
    let implementation = null;
    component.onChangeImplementation(implementation);
    expect(component.implementation).toBe(implementation);
    implementation = '';
    component.onChangeImplementation(implementation);
    expect(component.implementation).toBe(null);
    implementation = '      ';
    component.onChangeImplementation(implementation);
    expect(component.implementation).toBe('');
    implementation = 'implementation';
    component.onChangeImplementation(implementation);
    expect(component.implementation).toBe(implementation);
  });

  it('test createOperation with valid operation', () => {
    component.ngOnInit();
    const interfaceType = 'interfaceType';
    const operationType = 'operationType';
    const implementation = 'implementation';
    component.interfaceTypeFormCtrl.setValue(interfaceType);
    component.operationTypeFormCtrl.setValue(operationType);
    component.implementationFormCtrl.setValue(implementation);
    const inputs = new Array<PropertyAssignment>();
    const input1 = new PropertyAssignment('string');
    input1.name = 'input1';
    input1.value = 'input1Value';
    inputs.push(input1)
    component.inputs = inputs;
    let operation: Operation = null;
    component.addOperation.subscribe((operation1) => {
      operation = operation1;
    });
    component.createOperation();
    expect(operation).toBeTruthy();
    expect(operation.interfaceType).toBe(interfaceType);
    expect(operation.operationType).toBe(operationType);
    expect(operation.implementation).toBe(implementation);
    expect(operation.inputs).toBe(inputs);
  });

  it('test createOperation with invalid operation', () => {
    component.ngOnInit();
    let operation: Operation = null;
    component.addOperation.subscribe((operation1) => {
      operation = operation1;
    });
    component.createOperation();
    expect(operation).toBe(null);
  });

  it('test onDeleteInput with not found input', () => {
    component.ngOnInit();
    const input1 = new PropertyAssignment('string');
    const input2 = new PropertyAssignment('string');
    input2.name = 'input2';
    const input3 = new PropertyAssignment('string');
    input3.name = 'input3';
    component.inputs = new Array<PropertyAssignment>(input1, input2, input3);
    component.onDeleteInput(input2);
    expect(component.inputs.length).toBe(2);
    expect(component.inputs.find(input => input.name === input2.name)).toBeFalsy();
  });

  function assertRequiredFormCtrl(formControl: FormControl, valueToSet: any): void {
    expect(formControl.valid).toBeFalsy();
    let validationErrors = formControl.errors || {};
    expect(validationErrors['required']).toBeTruthy();
    formControl.setValue('');
    expect(validationErrors['required']).toBeTruthy();
    formControl.setValue(valueToSet);
    expect(formControl.valid).toBeTruthy();
  }
});
