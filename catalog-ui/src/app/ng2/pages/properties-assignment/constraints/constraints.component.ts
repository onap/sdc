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
import { PropertyBEModel } from "app/models";

@Component({
  selector: 'app-constraints',
  templateUrl: './constraints.component.html',
  styleUrls: ['./constraints.component.less']
})
export class ConstraintsComponent implements OnInit {

  @Input() set property(property: PropertyBEModel) {
    this._propertyType = property.type;
    this.constraints = new Array();
    if(property.constraints) {
      this._property = property;
      property.constraints.forEach((constraint: any) => {
        this.constraints.push(this.getConstraintFromPropertyBEModel(constraint));
      });
    }
  }
  @Input() set propertyType(propertyType: string) {
    if (this._propertyType != propertyType) {
      this.constraints = new Array();
    }
    this._propertyType = propertyType;
    this.emitOnConstraintChange();
  }
  @Input() isViewOnly: boolean = false;
  @Output() onConstraintChange: EventEmitter<any> = new EventEmitter<any>();

  constraints: Constraint[] = new Array();
  constraintTypes: string[];
  ConstraintTypesMapping = ConstraintTypesMapping;
  valid: boolean = true;
  _property: PropertyBEModel;
  _propertyType: string;

  ngOnInit() {
    this.constraintTypes = Object.keys(ConstraintTypes).map(key => ConstraintTypes[key]);
  }

  private getConstraintFromPropertyBEModel(constraint: any):Constraint {
    let constraintType: ConstraintTypes;
    let constraintValue: any;
    if(constraint.validValues){
      constraintType = ConstraintTypes.valid_values;
      constraintValue = constraint.validValues;
    } else if(constraint.equal) {
      constraintType = ConstraintTypes.equal;
      constraintValue = constraint.equal;
    } else if(constraint.greaterThan) {
      constraintType = ConstraintTypes.greater_than;
      constraintValue = constraint.greaterThan;
    } else if(constraint.greaterOrEqual) {
      constraintType = ConstraintTypes.greater_or_equal;
      constraintValue = constraint.greaterOrEqual;
    } else if(constraint.lessThan) {
      constraintType = ConstraintTypes.less_than;
      constraintValue = constraint.lessThan;
    } else if(constraint.lessOrEqual) {
      constraintType = ConstraintTypes.less_or_equal;
      constraintValue = constraint.lessOrEqual;
    } else if(constraint.rangeMinValue || constraint.rangeMaxValue) {
      constraintType = ConstraintTypes.in_range;
      constraintValue = new Array(constraint.rangeMinValue, constraint.rangeMaxValue);
    } else if(constraint.length) {
      constraintType = ConstraintTypes.length;
      constraintValue = constraint.length;
    } else if(constraint.minLength) {
      constraintType = ConstraintTypes.min_length;
      constraintValue = constraint.minLength;
    } else if(constraint.maxLength) {
      constraintType = ConstraintTypes.max_length;
      constraintValue = constraint.maxLength;
    } else if(constraint.pattern) {
      constraintType = ConstraintTypes.pattern;
      constraintValue = constraint.pattern;
    }
    return {
      type:constraintType,
      value:constraintValue
    }
  }

  private getConstraintsFormat(): any[] {
    let constraintArray = new Array();
    this.constraints.forEach((constraint: Constraint) => {
      constraintArray.push(this.getConstraintFormat(constraint))
    });
    return constraintArray;
  }

  private getConstraintFormat(constraint: Constraint): any {
    switch (constraint.type) {
      case ConstraintTypes.equal:
        return {
          [ConstraintTypes.equal]: constraint.value
        }
      case ConstraintTypes.less_or_equal:
        return {
          [ConstraintTypes.less_or_equal]: constraint.value
        }
      case ConstraintTypes.less_than:
        return {
          [ConstraintTypes.less_than]: constraint.value
        }
      case ConstraintTypes.greater_or_equal:
        return {
          [ConstraintTypes.greater_or_equal]: constraint.value
        }
      case ConstraintTypes.greater_than:
        return {
          [ConstraintTypes.greater_than]: constraint.value
        }
      case ConstraintTypes.in_range:
        return {
          [ConstraintTypes.in_range]: constraint.value
        }
      case ConstraintTypes.length:
        return {
          [ConstraintTypes.length]: constraint.value
        }
      case ConstraintTypes.max_length:
        return {
          [ConstraintTypes.max_length]: constraint.value
        }
      case ConstraintTypes.min_length:
        return {
          [ConstraintTypes.min_length]: constraint.value
        }
      case ConstraintTypes.pattern:
        return {
          [ConstraintTypes.pattern]: constraint.value
        }
      case ConstraintTypes.valid_values:
        return {
          [ConstraintTypes.valid_values]: constraint.value
        }
      default:
        return;
    }
  }

  private validateConstraints(): void {
    this.valid = this.constraints.every((constraint: Constraint) => {
      if (Array.isArray(constraint.value)) {
        return !(constraint.value.length == 0 || this.doesArrayContaintEmptyValues(constraint.value));
      }
      return constraint.value && constraint.type != ConstraintTypes.null
    });
  }

  private doesArrayContaintEmptyValues(arr) {
    for(const element of arr) {
      if(element === "") return true;
    }
    return false;
  }

  private emitOnConstraintChange(): void {
    this.validateConstraints();
    const newConstraints = this.getConstraintsFormat();
    this.onConstraintChange.emit({
      constraints: newConstraints,
      valid: this.valid
    });
  }

  removeFromList(constraintIndex: number, valueIndex: number){
    this.constraints[constraintIndex].value.splice(valueIndex, 1);
    this.emitOnConstraintChange()
  }

  addToList(constraintIndex: number){
    if (!this.constraints[constraintIndex].value) {
      this.constraints[constraintIndex].value = new Array();
    }
    this.constraints[constraintIndex].value.push("");
    this.emitOnConstraintChange()
  }

  onChangeConstraintType(constraintIndex: number, newType: ConstraintTypes) {
    this.constraints[constraintIndex].type = newType;
    if ((newType == ConstraintTypes.in_range || newType == ConstraintTypes.valid_values) && !Array.isArray(this.constraints[constraintIndex].value)) {
      this.constraints[constraintIndex].value = new Array()
    }
    this.emitOnConstraintChange();
  }

  onChangeConstraintValue(constraintIndex: number, newValue: any) {
    this.constraints[constraintIndex].value = newValue;
    this.emitOnConstraintChange();
  }

  onChangeConstrainValueIndex(constraintIndex: number, newValue: any, valueIndex: number) {
    if(!this.constraints[constraintIndex].value) {
      this.constraints[constraintIndex].value = new Array();
    }
    this.constraints[constraintIndex].value[valueIndex] = newValue;
    this.emitOnConstraintChange();
  }

  removeConstraint(constraintIndex: number) {
    this.constraints.splice(constraintIndex, 1);
    this.emitOnConstraintChange();
}

  addConstraint() {
    let newConstraint: Constraint = {
      type: ConstraintTypes.null,
      value: ""
    }
    this.constraints.push(newConstraint);
    this.emitOnConstraintChange();
  }

  getInRangeValue(constraintIndex: number, valueIndex: number): string {
    if(!this.constraints[constraintIndex].value || !this.constraints[constraintIndex].value[valueIndex]) {
      return "";
    }
    return this.constraints[constraintIndex].value[valueIndex];
  }

  disableConstraint(optionConstraintType: ConstraintTypes, constraintType: ConstraintTypes): boolean {
    const invalid = this.notAllowedConstraint(optionConstraintType);
    return invalid ? invalid : this.getConstraintTypeIfPresent(optionConstraintType) ? true : false;
  }

  notAllowedConstraint(optionConstraintType: ConstraintTypes): boolean {
    switch (optionConstraintType) {
      case ConstraintTypes.less_or_equal:
      case ConstraintTypes.less_than:
      case ConstraintTypes.greater_or_equal:
      case ConstraintTypes.greater_than:
        if (this.isComparable(this._propertyType)){
          return false;
        }
        break;
      case ConstraintTypes.in_range:
        if (this.isComparable(this._propertyType) || this._propertyType == PropertyTypes.range){
          return false;
        }
        break;
      case ConstraintTypes.length:
      case ConstraintTypes.max_length:
      case ConstraintTypes.min_length:
        if (this._propertyType == PropertyTypes.string || this._propertyType == PropertyTypes.map || this._propertyType == PropertyTypes.list){
          return false;
        }
        break;
      case ConstraintTypes.pattern:
        if (this._propertyType == PropertyTypes.string){
          return false;
        }
        break;
      case ConstraintTypes.valid_values:
      case ConstraintTypes.equal:
        return false;
    }
    return true;
  }

  getConstraintTypeIfPresent(constraintType: ConstraintTypes): Constraint {
    return this.constraints.find((constraint) => {
      return constraint.type == constraintType ? true : false;
    })
  }

  trackByFn(index) {
    return index;
  }

  isComparable(propType: string): boolean {
    if (propType in ComparableTypes) {
      return true;
    }
    return false;
  }

}

export enum ComparableTypes {
  string = "string",
  integer = "integer",
  float = "float",
  timestamp = "timestamp",
  version = "version",
  scalarUnit = "scalar-unit"
}

export enum PropertyTypes {
  string = "string",
  integer = "integer",
  float = "float",
  timestamp = "timestamp",
  version = "version",
  scalarUnit = "scalar-unit",
  range = "range",
  list = "list",
  map = "map"
}

export enum ConstraintTypes {
  null = "",
  equal= "equal",
  greater_than = "greaterThan",
  greater_or_equal = "greaterOrEqual",
  less_than = "lessThan",
  less_or_equal = "lessOrEqual",
  in_range = "inRange",
  valid_values = "validValues",
  length = "length",
  min_length = "minLength",
  max_length = "maxLength",
  pattern = "pattern"
}

export const ConstraintTypesMapping = {
  [ConstraintTypes.equal]: "equal",
  [ConstraintTypes.greater_than]: "greater_than",
  [ConstraintTypes.greater_or_equal]: "greater_or_equal",
  [ConstraintTypes.less_than]: "less_than",
  [ConstraintTypes.less_or_equal]: "less_or_equal",
  [ConstraintTypes.in_range]: "in_range",
  [ConstraintTypes.valid_values]: "valid_values",
  [ConstraintTypes.length]: "length",
  [ConstraintTypes.min_length]: "min_length",
  [ConstraintTypes.max_length]: "max_length",
  [ConstraintTypes.pattern]: "pattern"
};

export interface Constraint {
  type:ConstraintTypes,
  value:any
}