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
    this.constraints = new Array();
    if(property.constraints) {
      this._property = property;
      property.constraints.forEach((constraint: any) => {
        this.constraints.push(this.getConstraintFromPropertyBEModel(constraint));
      });
    }
  }
  @Input() isViewOnly: boolean = false;
  @Output() onConstraintChange: EventEmitter<any> = new EventEmitter<any>();

  constraints: Constraint[] = new Array();
  constraintTypes: string[];
  ConstraintTypesMapping = ConstraintTypesMapping;
  valid: boolean = true;
  _property: PropertyBEModel;

  ngOnInit() {
    this.constraintTypes = this.toArray(ConstraintTypes);
  }

  private toArray(enumme: typeof ConstraintTypes): string[] {
    return Object.keys(enumme)
        .map(key => enumme[key]);
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
          "equal": constraint.value
        }
      case ConstraintTypes.less_or_equal:
        return {
          "lessOrEqual": constraint.value
        }
      case ConstraintTypes.less_than:
        return {
          "lessThan": constraint.value
        }
      case ConstraintTypes.greater_or_equal:
        return {
          "greaterOrEqual": constraint.value
        }
      case ConstraintTypes.greater_than:
        return {
          "greaterThan": constraint.value
        }
      case ConstraintTypes.in_range:
        return {
          "inRange": constraint.value
        }
      case ConstraintTypes.length:
        return {
          "length": constraint.value
        }
      case ConstraintTypes.max_length:
        return {
          "maxLength": constraint.value
        }
      case ConstraintTypes.min_length:
        return {
          "minLength": constraint.value
        }
      case ConstraintTypes.pattern:
        return {
          "pattern": constraint.value
        }
      case ConstraintTypes.valid_values:
        return {
          "validValues": constraint.value
        }
      default:
        return;
    }
  }

  private areConstraintsValid(): void {
    this.valid = true;
    this.constraints.forEach((constraint: Constraint) => {
      if (!constraint.value || constraint.type == ConstraintTypes.null) {
        this.valid = false;
      }
    });
  }

  private emitOnConstraintChange(): void {
    this.areConstraintsValid();
    let newConstraints = this.getConstraintsFormat();
    this.onConstraintChange.emit({
      constraints: newConstraints,
      valid: this.valid
    });
  }

  removeFromList(constraint: Constraint, value: any){
    let index = this.constraints.indexOf(constraint);
    let valueIndex = this.constraints[index].value.indexOf(value);
    this.constraints[index].value.splice(valueIndex, 1);
    this.emitOnConstraintChange();
  }

  addToList(constraint: Constraint){
    let index = this.constraints.indexOf(constraint);
    if (!this.constraints[index].value) {
      this.constraints[index].value = new Array();
    }
    this.constraints[index].value.push("");
  }

  changeConstraintType(constraint: Constraint, newType: ConstraintTypes) {
    let index = this.constraints.indexOf(constraint);
    this.constraints[index].type = newType;
    if (newType == ConstraintTypes.in_range || newType == ConstraintTypes.valid_values) {
      this.constraints[index].value = new Array()
    }
    this.emitOnConstraintChange();
  }

  changeConstraintValue(constraint: Constraint, newValue: any) {
    let index = this.constraints.indexOf(constraint);
    this.constraints[index].value = newValue;
    this.emitOnConstraintChange();
  }

  changeConstrainValueIndex(constraint: Constraint, newValue: any, valueIndex: number) {
    let index = this.constraints.indexOf(constraint);
    if(!this.constraints[index].value) {
      this.constraints[index].value = new Array();
    }
    this.constraints[index].value[valueIndex] = newValue;
    this.emitOnConstraintChange();
  }

  removeConstraint(constraint: Constraint) {
    let index = this.constraints.indexOf(constraint);
    this.constraints.splice(index, 1);
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

  getInRangeValue(constraint: Constraint, valueIndex: number): string {
    let index = this.constraints.indexOf(constraint);
    if(!this.constraints[index].value || !this.constraints[index].value[valueIndex]) {
      return "";
    }
    return this.constraints[index].value[valueIndex];
  }

  disableConstraint(optionConstraintType: ConstraintTypes, constraintType: ConstraintTypes): boolean {
    return this.getConstraintTypeIfPresent(optionConstraintType) ? true : false;
  }

  getConstraintTypeIfPresent(constraintType: ConstraintTypes): Constraint {
    return this.constraints.find((constraint) => {
      return constraint.type == constraintType ? true : false;
    })
  }

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