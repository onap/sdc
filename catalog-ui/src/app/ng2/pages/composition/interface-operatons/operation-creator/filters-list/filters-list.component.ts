/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation
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

import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormControl,
  FormGroup, ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {
  FilterParameter,
  IFilterParameterList
} from '../../../../../../models/interfaceOperation';
import { InstanceFeDetails } from '../../../../../../models/instance-fe-details';
import { ConstraintTypes, ConstraintTypesMapping } from '../../../../../pages/properties-assignment/constraints/constraints.component';
import {CustomToscaFunction } from '../../../../../../models/default-custom-functions';
import { ToscaFunction } from '../../../../../../models/tosca-function';
import { PropertyBEModel } from '../../../../../../models/properties-inputs/property-be-model';
import { ToscaFunctionValidationEvent } from '../../../../properties-assignment/tosca-function/tosca-function.component';

@Component({
  selector: 'filters-list',
  templateUrl: './filters-list.component.html',
  styleUrls: ['./filters-list.component.less']
})
export class FiltersListComponent implements OnInit {

  @Input() customToscaFunctions: Array<CustomToscaFunction> = [];
  @Input() componentInstanceMap: Map<string, InstanceFeDetails> = new Map();
  @Input() activitiesExist: boolean;
  @Input() existingFilters: IFilterParameterList;
  @Input() isViewOnly: boolean;
  @Output('filtersChangeEvent') filtersChangeEvent: EventEmitter<any> = new EventEmitter<any>();

  isAProperty: Map<number, PropertyBEModel> = new Map();
  operatorTypes: any[];
  ConstraintTypesMapping = ConstraintTypesMapping;
  filters: FilterParameter[] = [];
  filterFormArray: FormArray = new FormArray([]);
  filterForm: FormGroup = new FormGroup (
    {
      'filterFormList': this.filterFormArray
    }
  );
  validationMessages = {
    filter: [
      { type: 'required', message: 'Filter name, constraint, and value is required'}
    ]
  };

  ngOnInit () {
    this.operatorTypes = Object.keys(ConstraintTypes).map((key) => ConstraintTypes[key]);
  }

  ngOnChanges (changes: SimpleChanges) {
    this.filterForm.valueChanges.subscribe(() => {
      this.emitOnFilterChange();
    });
    if (!changes.activitiesExist) {
      return;
    }
    if (changes.activitiesExist.currentValue) {
      this.initFilters();
    } else {
      this.filters = [];
      this.filterFormArray = new FormArray([]);
      this. filterForm = new FormGroup (
        {
          'filterFormList': this.filterFormArray
        }
      );
    }
  }

  private initFilters () {
    if (this.existingFilters && this.existingFilters.listToscaDataDefinition && this.existingFilters.listToscaDataDefinition.length > 0) {
      this.existingFilters.listToscaDataDefinition.forEach(val => {
        this.filters.push(val);
        this.filterFormArray.push(
          new FormControl(val, [Validators.required, this.formControlValidator()])
        );
      })
    }
  }

  private emitOnFilterChange (): void {
    this.filtersChangeEvent.emit({
      filters: this.filters,
      valid: this.filterForm.valid
    });
  }

  addFilter () {
    let filterParameter: FilterParameter = {
      name: null,
      constraint: null,
      filterValue: null
    }
    this.filters.push(filterParameter);
    this.filterFormArray.push(
      new FormControl(filterParameter, [Validators.required, this.formControlValidator()])
    );
  }

  private formControlValidator (): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const filter = control.value;

      if (!filter || !filter.name || !filter.constraint || !filter.filterValue) {
        return {required:true};
      }
      return null;
    }
  }

  removeFromFilters (index: number) {
    this.filters.splice(index, 1);
    this.filterFormArray.removeAt(index);
  }

  onFilterConstraintChange (type: string, index: number) {
    let filter = this.filterFormArray.controls[index].value;
    filter.constraint = type;
    if (ConstraintTypes.valid_values == type || ConstraintTypes.in_range == type) {
      filter.filterValue = [];
    } else {
      filter.filterValue = '';
    }
    this.filterFormArray.controls[index].setValue(filter);
  }

  onFilterValueChange (value: any, index: number) {
    let filter = this.filterFormArray.controls[index].value;
    filter.filterValue = value;
    this.filterFormArray.controls[index].setValue(filter);
  }

  onFilterNameChange (value: any, index: number) {
    let filter = this.filterFormArray.controls[index].value;
    filter.name = value;
    this.filterFormArray.controls[index].setValue(filter);
  }

  getInRangeValue (index: number, valueIndex: number): string {
    const value = this.filters[index].filterValue;

    if (!value || !value[valueIndex]) {
      return '';
    }

    return value[valueIndex];
  }

  onChangeConstrainValueIndex (index: number, newValue: any, valueIndex: number) {
    let filter = this.filterFormArray.controls[index].value;
    if (!filter.filterValue) {
      filter.filterValue = [];
    }
    filter.filterValue[valueIndex] = newValue;
    this.filterFormArray.controls[index].setValue(filter);
  }

  constraintValuesArray (index: number) {
    let filters = this.filterForm.get('filterFormList') as FormArray;
    return filters.at(index).value.filterValue;
  }

  addToList (filterIndex: number) {
    this.constraintValuesArray(filterIndex).push('');
  }

  removeFromList (filterIndex: number, valueIndex: number) {
    this.constraintValuesArray(filterIndex).splice(valueIndex, 1);
  }

  onValueTypeChange (value: string, index: number) {
    if (value === 'true') {
      let filter = this.filterFormArray.controls[index].value;
      if (!filter.toscaFunction) {
        filter.toscaFunction = {} as ToscaFunction;
      }
      filter.filterValue = '';
      this.filterFormArray.controls[index].setValue(filter);
    } else {
      let filter = this.filterFormArray.controls[index].value;
      filter.toscaFunction = undefined;
      this.filterFormArray.controls[index].setValue(filter);
      if (this.isAProperty.has(index)) {
        this.isAProperty.delete(index);
      }
    }
  }

  getAsProperty(index: number) {
    if (!this.isAProperty.has(index)) {
      let filter = this.filterFormArray.controls[index].value;
      let property = new PropertyBEModel();
      property.type = 'any';
      property.toscaFunction = filter.toscaFunction;
      property.value = filter.filterValue;
      this.isAProperty.set(index, property);
      return property;
    }
    return this.isAProperty.get(index);
  }

  onToscaFunctionValidityChange(validationEvent: ToscaFunctionValidationEvent, index: number):void {
    if (validationEvent.isValid) {
      let filter = this.filterFormArray.controls[index].value;
      filter.toscaFunction = validationEvent.toscaFunction;
      filter.filterValue = validationEvent.toscaFunction.buildValueString();
      this.filterFormArray.controls[index].setValue(filter);
    }
  }

  isToscaFunction(index: number): boolean {
    let filter = this.filterFormArray.controls[index].value;
    let toscaFunction = filter.toscaFunction;
    return toscaFunction;
  }

}
