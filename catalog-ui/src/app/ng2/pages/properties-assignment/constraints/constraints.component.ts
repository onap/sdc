/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  AbstractControl, FormArray,
  FormBuilder,
  FormControl,
  FormGroup, ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { PROPERTY_DATA, PROPERTY_TYPES } from 'app/utils/constants';

@Component({
  selector: 'app-constraints',
  templateUrl: './constraints.component.html',
  styleUrls: ['./constraints.component.less']
})
export class ConstraintsComponent implements OnInit {

  @Input() propertyConstraints: any[];
  @Input() propertyType: string;
  @Input() isViewOnly: boolean = false;
  @Output() onConstraintChange: EventEmitter<any> = new EventEmitter<any>();

  constraintTypes: string[];
  ConstraintTypesMapping = ConstraintTypesMapping;
  valid: boolean = false;
  constraintForm: FormGroup;
  validationMessages;
  init: boolean = true;

  constructor(private formBuilder: FormBuilder) {}

  get constraintsArray() {
    return this.constraintForm.get('constraint') as FormArray;
  }

  get constraintValidators(): ValidatorFn {
    switch (this.propertyType) {
      case PROPERTY_TYPES.INTEGER:
        console.warn('Add int validator');
        return Validators.compose([
          Validators.required,
          intValidator()
        ]);
      case PROPERTY_TYPES.FLOAT:
        console.warn('Add float validator');
        return Validators.compose([
          Validators.required,
          floatValidator()
        ]);
      default:
        console.warn('Only required validator');
        return Validators.compose([
          Validators.required
        ]);
    }
}

  public constraintValuesArray(index: number): FormArray {
    return this.constraintsArray.at(index).get('value') as FormArray;
  }

  ngOnInit() {
    console.groupEnd();
    this.constraintTypes = Object.keys(ConstraintTypes).map((key) => ConstraintTypes[key]);

    // This is only used by the spec test
    if (!this.constraintForm) {
      this.constraintForm = this.formBuilder.group({
        constraint: this.formBuilder.array([])
      });
    }

    this.validationMessages = {
      constraint: [
        { type: 'required', message: 'Constraint value is required'},
        { type: 'invalidInt', message: 'Constraint value is not a valid integer'},
        { type: 'invalidFloat', message: 'Constraint value is not a valid floating point value'},
        { type: 'invalidString', message: 'String contains invalid characters'}
      ],
      type : [
        { type: 'required', message: 'Constraint type is required'}
      ]
    };

    this.init = false;
  }

  ngOnChanges(changes): void {
    console.groupEnd();

    // Changes fires before init so form has to be initialised here
    if (this.init) {
      this.constraintForm = this.formBuilder.group({
        constraint: this.formBuilder.array([])
      });

      if (changes.propertyConstraints && changes.propertyConstraints.currentValue) {
        changes.propertyConstraints.currentValue.forEach((constraint: any) => {
          const prop = this.getConstraintFromPropertyBEModel(constraint);
          console.log('constraint from BE model', prop);
          this.constraintsArray.push(prop);
        });
      }
    }

    if (changes.propertyType) {
      if (!this.init) {
        // Reset constraints on property type change
        console.warn('Property type changed. Resetting constraints');
        this.constraintForm = this.formBuilder.group({
          constraint: this.formBuilder.array([])
        });
      }

      if (!this.propertyType || changes.propertyType.currentValue == this.propertyType) {
        this.propertyType = changes.propertyType.currentValue;
      } else {
        this.propertyType = changes.propertyType;
        this.emitOnConstraintChange();
      }

      this.constraintsArray.controls.forEach((control: AbstractControl) => {
        control.get('value').setValidators(this.constraintValidators);
      });
    }

    console.log('constraints', this.constraintsArray);
  }

  removeFromList(constraintIndex: number, valueIndex: number) {
    this.constraintsArray.at(constraintIndex).get('value').value.splice(valueIndex, 1);
    this.emitOnConstraintChange();
  }

  addToList(constraintIndex: number) {
    const newConstraint = new FormControl('', this.constraintValidators);

    this.constraintValuesArray(constraintIndex).push(newConstraint);
    console.log('constraintsArray', this.constraintsArray);
    console.log('constraintValuesArray', this.constraintValuesArray(constraintIndex));
    this.emitOnConstraintChange();
  }

  onChangeConstraintType(constraintIndex: number, newType: ConstraintTypes) {
    if ((newType == ConstraintTypes.valid_values)) {
      const newConstraint = this.formBuilder.group({
        type: new FormControl({
          value: newType,
          disabled: this.isViewOnly
        }, Validators.required),
        value: this.formBuilder.array([])});

      this.constraintsArray.removeAt(constraintIndex);
      this.constraintsArray.push(newConstraint);
    } else if (newType == ConstraintTypes.in_range) {
      const newConstraint = this.formBuilder.group({
        type: new FormControl({
          value: newType,
          disabled: this.isViewOnly
        }, Validators.required),
        value: this.formBuilder.array([])});

      const valRef = newConstraint.get('value') as FormArray;
      valRef.push(new FormControl('', this.constraintValidators));
      valRef.push(new FormControl('', this.constraintValidators));

      this.constraintsArray.removeAt(constraintIndex);
      this.constraintsArray.push(newConstraint);
    } else {
      this.constraintsArray.at(constraintIndex).value.type = newType;
    }
    this.emitOnConstraintChange();
  }

  onChangeConstraintValue(constraintIndex: number, newValue: any) {
    this.constraintsArray.at(constraintIndex).get('value').setValue(newValue);
    this.emitOnConstraintChange();
  }

  onChangeConstrainValueIndex(constraintIndex: number, newValue: any, valueIndex: number) {
    this.constraintValuesArray(constraintIndex).controls[valueIndex].setValue(newValue);
    this.emitOnConstraintChange();
  }

  removeConstraint(constraintIndex: number) {
    this.constraintsArray.removeAt(constraintIndex);
    this.emitOnConstraintChange();
}

  addConstraint() {
    const newConstraint = this.formBuilder.group({
      type: new FormControl({
        value: ConstraintTypes.null,
        disabled: this.isViewOnly
      }, Validators.required),
      value: new FormControl({
        value: '',
        disabled: this.isViewOnly
      }, this.constraintValidators)
    });
    this.constraintsArray.push(newConstraint);
    this.valid = false;
    this.emitOnConstraintChange();
  }

  getInRangeValue(constraintIndex: number, valueIndex: number): string {
    const value = this.constraintsArray.at(constraintIndex).get('value').value;

    if (!value || !value[valueIndex]) {
      return '';
    }

    return value[valueIndex];
  }

  disableConstraint(optionConstraintType: ConstraintTypes): boolean {
    const invalid = this.notAllowedConstraint(optionConstraintType);
    return invalid ? invalid : this.getConstraintTypeIfPresent(optionConstraintType) ? true : false;
  }

  notAllowedConstraint(optionConstraintType: ConstraintTypes): boolean {
    switch (optionConstraintType) {
      case ConstraintTypes.less_or_equal:
      case ConstraintTypes.less_than:
      case ConstraintTypes.greater_or_equal:
      case ConstraintTypes.greater_than:
      case ConstraintTypes.in_range:
        if (this.isComparable(this.propertyType)) {
          return false;
        }
        break;
      case ConstraintTypes.length:
      case ConstraintTypes.max_length:
      case ConstraintTypes.min_length:
        if (this.propertyType == PROPERTY_TYPES.STRING || this.propertyType == PROPERTY_TYPES.MAP || this.propertyType == PROPERTY_TYPES.LIST) {
          return false;
        }
        break;
      case ConstraintTypes.pattern:
        if (this.propertyType == PROPERTY_TYPES.STRING) {
          return false;
        }
        break;
      case ConstraintTypes.valid_values:
      case ConstraintTypes.equal:
        return false;
    }
    return true;
  }

  getConstraintTypeIfPresent(constraintType: ConstraintTypes): AbstractControl {
    return this.constraintsArray.controls.find((control: AbstractControl) => {
      const type = control.get('type').value;
      return type == constraintType;
    });
  }

  trackByFn(index) {
    return index;
  }

  isComparable(propType: string): boolean {
    if (PROPERTY_DATA.COMPARABLE_TYPES.indexOf(propType) >= 0) {
      return true;
    }
    return false;
  }

  private getConstraintFromPropertyBEModel(constraint: any): AbstractControl {
    console.log('be model constraints', constraint);
    let constraintType: ConstraintTypes;
    let constraintValue: any;
    if (!constraint) {
      constraintType = ConstraintTypes.null;
      constraintValue = '';
    } else if (constraint.hasOwnProperty(ConstraintTypes.valid_values)) {
      constraintType = ConstraintTypes.valid_values;
    } else if (constraint.hasOwnProperty(ConstraintTypes.equal)) {
      constraintType = ConstraintTypes.equal;
      constraintValue = constraint.equal;
    } else if (constraint.hasOwnProperty(ConstraintTypes.greater_than)) {
      constraintType = ConstraintTypes.greater_than;
      constraintValue = constraint.greaterThan;
    } else if (constraint.hasOwnProperty(ConstraintTypes.greater_or_equal)) {
      constraintType = ConstraintTypes.greater_or_equal;
      constraintValue = constraint.greaterOrEqual;
    } else if (constraint.hasOwnProperty(ConstraintTypes.less_than)) {
      constraintType = ConstraintTypes.less_than;
      constraintValue = constraint.lessThan;
    } else if (constraint.hasOwnProperty(ConstraintTypes.less_or_equal)) {
      constraintType = ConstraintTypes.less_or_equal;
      constraintValue = constraint.lessOrEqual;
    } else if (constraint.hasOwnProperty(ConstraintTypes.in_range)) {
      constraintType = ConstraintTypes.in_range;
      constraintValue = new Array(constraint.inRange[0], constraint.inRange[1]);
    } else if (constraint.rangeMaxValue || constraint.rangeMinValue) {
      constraintType = ConstraintTypes.in_range;
      constraintValue = new Array(constraint.rangeMinValue, constraint.rangeMaxValue);
    } else if (constraint.hasOwnProperty(ConstraintTypes.length)) {
      constraintType = ConstraintTypes.length;
      constraintValue = constraint.length;
    } else if (constraint.hasOwnProperty(ConstraintTypes.min_length)) {
      constraintType = ConstraintTypes.min_length;
      constraintValue = constraint.minLength;
    } else if (constraint.hasOwnProperty(ConstraintTypes.max_length)) {
      constraintType = ConstraintTypes.max_length;
      constraintValue = constraint.maxLength;
    } else if (constraint.hasOwnProperty(ConstraintTypes.pattern)) {
      constraintType = ConstraintTypes.pattern;
      constraintValue = constraint.pattern;
    }

    if (!constraint.hasOwnProperty(ConstraintTypes.valid_values) && !constraint.hasOwnProperty(ConstraintTypes.in_range)) {
      return this.formBuilder.group({
        type: new FormControl({
          value: constraintType,
          disabled: this.isViewOnly
        }, Validators.required),
        value: new FormControl({
          value: constraintValue,
          disabled: this.isViewOnly
        }, this.constraintValidators)
      });
    } else {
      const newForm = this.formBuilder.group({
        type: new FormControl({
          value: constraintType,
          disabled: this.isViewOnly
        }, Validators.required),
        value: this.formBuilder.array([])
      });

      const valRef = newForm.get('value') as FormArray;
      if (constraint.hasOwnProperty(ConstraintTypes.valid_values)) {
        constraint.validValues.forEach((val) => {
          valRef.push(new FormControl({value: val, disabled: this.isViewOnly}, this.constraintValidators));
        });
      } else {
        constraint.inRange.forEach((val) => {
          valRef.push(new FormControl({value: val, disabled: this.isViewOnly}, this.constraintValidators));
        });
      }

      console.log('new form', newForm);
      return newForm;
    }
  }

  private getConstraintsFormat(): any[] {
    const constraintArray = new Array();
    this.constraintsArray.controls.forEach((control: AbstractControl) => {
      const type = control.get('type').value;
      let constraint: Constraint;

      if (type != ConstraintTypes.valid_values && type != ConstraintTypes.in_range) {
        constraint = {
          type,
          value: control.get('value').value
        };
      } else {
        const valArray = [];

        control.get('value').value.forEach((val) => {
          valArray.push(val);
        });

        constraint = {
          type,
          value: valArray
        };
      }

      console.log('New constraint object', constraint);
      constraintArray.push(this.getConstraintFormat(constraint));
    });
    return constraintArray;
  }

  private getConstraintFormat(constraint: Constraint): any {
    switch (constraint.type) {
      case ConstraintTypes.equal:
        return {
          [ConstraintTypes.equal]: constraint.value
        };
      case ConstraintTypes.less_or_equal:
        return {
          [ConstraintTypes.less_or_equal]: constraint.value
        };
      case ConstraintTypes.less_than:
        return {
          [ConstraintTypes.less_than]: constraint.value
        };
      case ConstraintTypes.greater_or_equal:
        return {
          [ConstraintTypes.greater_or_equal]: constraint.value
        };
      case ConstraintTypes.greater_than:
        return {
          [ConstraintTypes.greater_than]: constraint.value
        };
      case ConstraintTypes.in_range:
        return {
          [ConstraintTypes.in_range]: constraint.value
        };
      case ConstraintTypes.length:
        return {
          [ConstraintTypes.length]: constraint.value
        };
      case ConstraintTypes.max_length:
        return {
          [ConstraintTypes.max_length]: constraint.value
        };
      case ConstraintTypes.min_length:
        return {
          [ConstraintTypes.min_length]: constraint.value
        };
      case ConstraintTypes.pattern:
        return {
          [ConstraintTypes.pattern]: constraint.value
        };
      case ConstraintTypes.valid_values:
        return {
          [ConstraintTypes.valid_values]: constraint.value
        };
      default:
        return;
    }
  }

  private validateConstraints(): void {
    this.valid = this.constraintsArray.controls.every((control: AbstractControl) => {
      const value = control.get('value').value;
      const type = control.get('type').value;
      control.updateValueAndValidity();

      if (Array.isArray(value)) {
        return !(value.length == 0 || this.doesArrayContaintEmptyValues(value));
      }
      if (type == ConstraintTypes.pattern) {
        try {
          new RegExp(value);
          this.valid = true;
        } catch (e) {
          this.valid = false;
        }
      } else {
        this.valid = this.constraintForm.valid;
      }

      return value && type != ConstraintTypes.null;
    });
  }

  private doesArrayContaintEmptyValues(arr) {
    for (const element of arr) {
      if (element === '') { return true; }
    }
    return false;
  }

  private emitOnConstraintChange(): void {
    console.log('constraints', this.constraintsArray);

    this.validateConstraints();
    const newConstraints = this.getConstraintsFormat();

    this.valid = this.constraintForm.valid;
    console.log('emitOnConstraintChange.valid', this.valid);

    this.onConstraintChange.emit({
      constraints: newConstraints,
      valid: this.valid
    });
  }

}

export enum ConstraintTypes {
  null = '',
  equal= 'equal',
  greater_than = 'greaterThan',
  greater_or_equal = 'greaterOrEqual',
  less_than = 'lessThan',
  less_or_equal = 'lessOrEqual',
  in_range = 'inRange',
  valid_values = 'validValues',
  length = 'length',
  min_length = 'minLength',
  max_length = 'maxLength',
  pattern = 'pattern'
}

export const ConstraintTypesMapping = {
  [ConstraintTypes.equal]: 'equal',
  [ConstraintTypes.greater_than]: 'greater_than',
  [ConstraintTypes.greater_or_equal]: 'greater_or_equal',
  [ConstraintTypes.less_than]: 'less_than',
  [ConstraintTypes.less_or_equal]: 'less_or_equal',
  [ConstraintTypes.in_range]: 'in_range',
  [ConstraintTypes.valid_values]: 'valid_values',
  [ConstraintTypes.length]: 'length',
  [ConstraintTypes.min_length]: 'min_length',
  [ConstraintTypes.max_length]: 'max_length',
  [ConstraintTypes.pattern]: 'pattern'
};

export interface Constraint {
  type: ConstraintTypes;
  value: any;
}

export function intValidator(): ValidatorFn {
  const intRegex = /^[-+]?\d+$/;
  return (control: AbstractControl): ValidationErrors | null => {
    if (control.value && !intRegex.test(control.value)) {
      return {invalidInt: true};
    }

    return null;
  };
}

export function floatValidator(): ValidatorFn {
  const floatRegex = /^[-+]?\d+(\.\d+)?$/;

  return (control: AbstractControl): ValidationErrors | null => {
    if (control.value && !floatRegex.test(control.value)) {
      return {invalidFloat: true};
    }

    return null;
  };
}

export function stringValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (control.value) {
      const value: string = control.value;
      const checks: string[] = ['\'', '"', '`', '[', '{', '}', ']'];

      for (const check of checks) {
        if (value.startsWith(check) && !(value.length >= 2 && value.endsWith(check))) {
          return {invalidString: true};
        }
      }

      return null;
    } else {
      return null;
    }
  };
}
