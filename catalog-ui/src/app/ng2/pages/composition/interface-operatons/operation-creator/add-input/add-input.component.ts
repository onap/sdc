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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {InputOperationParameter} from '../../../../../../models/interfaceOperation';
import {IDropDownOption} from 'onap-ui-angular/dist/form-elements/dropdown/dropdown-models';
import {Observable} from 'rxjs/Observable';
import {AbstractControl, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators} from '@angular/forms';
import {PROPERTY_TYPES} from '../../../../../../utils/constants';
import {SchemaProperty, SchemaPropertyGroupModel} from '../../../../../../models/schema-property';
import {DataTypeModel} from "../../../../../../models/data-types";

@Component({
  selector: 'app-add-input',
  templateUrl: './add-input.component.html',
  styleUrls: ['./add-input.component.less']
})
export class AddInputComponent implements OnInit {

  @Input('dataTypeMap') dataTypeMap$: Observable<Map<string, DataTypeModel>>;
  @Input('isView') isView: boolean;
  @Input() existingInputNames: Array<string> = [];
  @Output('onAddInput') onAddInputEvent: EventEmitter<InputOperationParameter>;

  dataTypeMap: Map<string, DataTypeModel>;
  inputToAdd: InputOperationParameter;
  inputTypeOptions: Array<IDropDownOption>;
  inputSchemaOptions: Array<IDropDownOption>;
  showForm: boolean = false;
  showAddLink: boolean = true;
  showInputSchema: boolean = false;

  inputForm: FormGroup;

  constructor() {
    this.onAddInputEvent = new EventEmitter<InputOperationParameter>();
    this.inputTypeOptions = [];
    this.inputSchemaOptions = [];
    this.inputToAdd = new InputOperationParameter();
  }

  schemaValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const type = control.get('type');
    const schema = control.get('schema');
    return (type.value === 'list' || type.value === 'map') && !schema.value ? { schemaRequired: true } : null;
  };

  uniqueNameValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const name = control.get('name');
    return this.existingInputNames.indexOf(name.value) === -1 ? null : { nameIsNotUnique: true };
  };

  ngOnInit() {
    this.initForm();
    this.initInputType();
  }

  private initForm() {
    this.inputForm = new FormGroup({
      name: new FormControl({value: '', disabled: this.isView}, [Validators.required, Validators.minLength(1)]),
      type: new FormControl({value: '', disabled: this.isView}, [Validators.required, Validators.minLength(1)]),
      schema: new FormControl({value: '', disabled: this.isView})
    }, { validators: [this.schemaValidator, this.uniqueNameValidator] });
  }

  private initInputType() {
    this.dataTypeMap$.subscribe((dataTypesMap: Map<string, DataTypeModel>) => {
      this.dataTypeMap = dataTypesMap;
      this.inputTypeOptions = [];
      this.inputSchemaOptions = [];
      console.log('typeof dataTypesMap', typeof dataTypesMap);
      dataTypesMap.forEach((value, key) => {
        const entry = {label: key, value: key};
        this.inputTypeOptions.push(entry);
        if (key != PROPERTY_TYPES.LIST && key != PROPERTY_TYPES.MAP) {
          this.inputSchemaOptions.push(entry);
        }
      });
    });
  }

  onChangeInputType(inputType) {
    const typeForm = this.inputForm.get('type');
    if (!inputType) {
      this.inputToAdd.type = undefined;
      typeForm.setValue(undefined);
      this.toggleInputSchema();
      return;
    }
    typeForm.setValue(inputType);
    this.inputToAdd.type = inputType;
    this.toggleInputSchema();
  }

  onChangeInputSchema(inputSchema: string) {
    const schemaForm = this.inputForm.get('schema');
    if (!inputSchema) {
      this.inputToAdd.schema = undefined;
      schemaForm.setValue(undefined);
      return;
    }
    schemaForm.setValue(inputSchema);
    this.inputToAdd.schema = new SchemaPropertyGroupModel();
    this.inputToAdd.schema.property = new SchemaProperty();
    this.inputToAdd.schema.property.type = inputSchema;
  }

  onSubmit() {
    this.trimForm();
    if (this.inputForm.valid) {
      const nameForm = this.inputForm.get('name');
      const typeForm = this.inputForm.get('type');
      const schemaForm = this.inputForm.get('schema');
      const input = new InputOperationParameter();
      input.name = nameForm.value;
      input.type = typeForm.value;
      if (this.typeHasSchema()) {
        input.schema = new SchemaPropertyGroupModel();
        input.schema.property = new SchemaProperty();
        input.schema.property.type = schemaForm.value;
      }
      input.inputId = this.generateUniqueId();
      this.onAddInputEvent.emit(input);
      this.hideAddInput();
      this.resetForm();
    }
  }

  showAddInput() {
    this.showForm = true;
    this.showAddLink = false;
  }

  hideAddInput() {
    this.showForm = false;
    this.showAddLink = true;
  }

  onCancel() {
    this.hideAddInput();
    this.resetForm();
  }

  private generateUniqueId(): string {
    let result = '';
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const charactersLength = characters.length;
    for (let i = 0; i < 36; i++ ) {
      result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
  }

  private resetForm() {
    this.inputForm.reset();
    this.showInputSchema = false;
    this.inputToAdd = new InputOperationParameter();
  }

  getSchemaType() {
    return this.inputToAdd.schema == undefined ? undefined : this.inputToAdd.schema.property.type;
  }

  getSchemaPlaceholder() {
    const schemaType = this.getSchemaType();
    return schemaType === undefined ? 'Select...' : schemaType;
  }

  private toggleInputSchema() {
    this.showInputSchema = this.typeHasSchema();
  }

  private typeHasSchema() {
    const typeForm = this.inputForm.get('type');
    return typeForm.value == PROPERTY_TYPES.LIST || typeForm.value == PROPERTY_TYPES.MAP;
  }

  private trimForm() {
    const nameForm = this.inputForm.get('name');
    if (nameForm.value) {
      nameForm.setValue(nameForm.value.trim());
    }
    const typeForm = this.inputForm.get('type');
    if (typeForm.value) {
      typeForm.setValue(typeForm.value.trim());
    }
    const schemaForm = this.inputForm.get('schema');
    if (schemaForm.value) {
      schemaForm.setValue(schemaForm.value.trim());
    }
  }

}
