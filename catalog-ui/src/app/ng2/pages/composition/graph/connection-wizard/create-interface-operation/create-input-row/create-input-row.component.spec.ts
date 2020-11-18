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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CreateInputRowComponent} from './create-input-row.component';
import {SdcUiComponentsModule} from "onap-ui-angular/dist";
import {ReactiveFormsModule} from "@angular/forms";
import {TranslatePipe} from "../../../../../../shared/translator/translate.pipe";
import {PropertyAssignment} from "../../../../../../../models/properties-inputs/property-assignment";

describe('CreateInputRowComponent', () => {
  let component: CreateInputRowComponent;
  let fixture: ComponentFixture<CreateInputRowComponent>;
  const nameField = 'name';
  const valueField = 'value';

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateInputRowComponent, TranslatePipe ],
      imports: [ SdcUiComponentsModule, ReactiveFormsModule ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateInputRowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('form is invalid when empty', () => {
    expect(component.formGroup.valid).toBeFalsy();
  });

  it('name field validity', () => {
    expect(component.formGroup.valid).toBe(false);
    expect(component.formGroup.contains(nameField)).toBe(true);
    const nameFormControl = component.formGroup.get(nameField);
    expect(nameFormControl.valid).toBeFalsy();
    let validationErrors = nameFormControl.errors || {};
    expect(validationErrors['required']).toBeTruthy();

    nameFormControl.setValue('');
    validationErrors = nameFormControl.errors || {};
    expect(validationErrors['required']).toBeTruthy();

    nameFormControl.setValue('a');
    expect(nameFormControl.valid).toBeTruthy();
  });

  it('value field validity', () => {
    expect(component.formGroup.valid).toBeFalsy();
    expect(component.formGroup.contains(valueField)).toBeTruthy();
    const valueFormControl = component.formGroup.get(valueField);
    expect(valueFormControl.valid).toBeTruthy();
  });

  it('test set value when form valid', () => {
    expect(component.formGroup.valid).toBeFalsy();
    expect(component.propertyAssignment.name).toBeFalsy();
    expect(component.propertyAssignment.value).toBeFalsy();
    const nameFormCtrl = component.formGroup.get(nameField);
    nameFormCtrl.setValue('aName');
    const valueFormCtrl = component.formGroup.get(valueField);
    valueFormCtrl.setValue('aValue');
    expect(component.formGroup.valid).toBeTruthy();
    expect(component.propertyAssignment.name).toBe('aName');
    expect(component.propertyAssignment.value).toBe('aValue');
  });

  it('test propertyAssignment initialization', () => {
    const propertyAssignment = new PropertyAssignment();
    propertyAssignment.name = 'aName';
    propertyAssignment.value = 'aValue';
    component.propertyAssignment = propertyAssignment;
    component.ngOnInit();
    expect(component.formGroup.valid).toBeTruthy();
    const nameFormCtrl = component.formGroup.get(nameField);
    expect(nameFormCtrl.value).toBe(propertyAssignment.name);
    const valueFormCtrl = component.formGroup.get(valueField);
    expect(valueFormCtrl.value).toBe(propertyAssignment.value);
  });

  it('test propertyAssignment form binding', () => {
    const propertyAssignment = new PropertyAssignment();
    component.propertyAssignment = propertyAssignment;
    component.ngOnInit();
    const nameFormCtrl = component.formGroup.get(nameField);
    nameFormCtrl.setValue('anotherName');
    const valueFormCtrl = component.formGroup.get(valueField);
    valueFormCtrl.setValue('anotherValue');
    expect(nameFormCtrl.value).toBe(propertyAssignment.name);
    expect(valueFormCtrl.value).toBe(propertyAssignment.value);
  });

  it('test input deletion', () => {
    const expectedPropertyAssignment = new PropertyAssignment();
    let actualPropertyAssignment = null;
    component.onDeleteEvent.subscribe((value) => actualPropertyAssignment = value);
    component.onDelete(expectedPropertyAssignment);
    expect(actualPropertyAssignment).toBe(expectedPropertyAssignment);
  });

});
