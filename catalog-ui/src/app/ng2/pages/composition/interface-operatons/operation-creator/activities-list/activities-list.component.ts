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

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  ActivityParameter,
  IActivityParameterList,
  InputOperationParameter,
  IOperationParamsList,
  ActivityTypesEnum
} from "../../../../../../models/interfaceOperation";
import { DataTypeModel } from "../../../../../../models/data-types";
import { Observable } from "rxjs/Observable";
import { InstanceFeDetails } from "../../../../../../models/instance-fe-details";
import {
  AbstractControl,
  FormArray,
  FormControl,
  FormGroup, 
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';

@Component({
  selector: 'activities-list',
  templateUrl: './activities-list.component.html',
  styleUrls: ['./activities-list.component.less']
})
export class ActivitiesListComponent implements OnInit {

  @Input() componentInstanceMap: Map<string, InstanceFeDetails> = new Map();
  @Input() dataTypeMap: Map<string, DataTypeModel>;;
  @Input() dataTypeMap$: Observable<Map<string, DataTypeModel>>;
  @Input() existingActivities: IActivityParameterList;
  @Input() isViewOnly: boolean;
  @Output('activitiesChangeEvent') activitiesChangeEvent: EventEmitter<any> = new EventEmitter<any>();

  readonly DEFAULT_INPUT_TYPE: string = "tosca.dataTypes.tmf.milestoneJeopardyData";
  readonly DEFAULT_INPUT_NAME: string = "TMFMilestoneJeopardyData";
  activityTypes: string[] = [];
  activities: ActivityParameter[] = [];
  activityFormArray: FormArray = new FormArray([]);
  activityForm: FormGroup = new FormGroup (
    {
      'activityFormList': this.activityFormArray
    }
  );
  validationMessages = {
    activity: [
      { type: 'required', message: 'Activity type and value is required'}
    ]
  };

  ngOnInit() {
    Object.keys(ActivityTypesEnum).forEach(key => {
      this.activityTypes.push(ActivityTypesEnum[key])
    });
    this.activityForm.valueChanges.subscribe(() => {
      this.emitOnActivityChange();
    });
    if (this.existingActivities && this.existingActivities.listToscaDataDefinition && this.existingActivities.listToscaDataDefinition.length > 0) {
      this.existingActivities.listToscaDataDefinition.forEach(val => {
        this.activities.push(val);
        this.activityFormArray.push(
          new FormControl(val, [Validators.required, this.formControlValidator()])
        );
      })
    }
  }

  private emitOnActivityChange(): void {
    this.activitiesChangeEvent.emit({
      activities: this.activities,
      valid: this.activityForm.valid
    });
  }

  addActivity() {
    let input = new class implements IOperationParamsList {
      listToscaDataDefinition: Array<InputOperationParameter> = [];
    }
    let activityParameter: ActivityParameter = {
      type: null,
      workflow: null,
      inputs: input
    }
    this.activities.push(activityParameter);
    this.activityFormArray.push(
      new FormControl(activityParameter, [Validators.required, this.formControlValidator()])
    );

    let index = this.activities.indexOf(activityParameter);
    let inputOperationParameter: InputOperationParameter = new InputOperationParameter();
    inputOperationParameter.name = this.DEFAULT_INPUT_NAME;
    inputOperationParameter.type = this.DEFAULT_INPUT_TYPE;
    inputOperationParameter.valid = true;
    this.activities[index].inputs.listToscaDataDefinition.push(inputOperationParameter);
    this.activities[index].inputs.listToscaDataDefinition = Array.from(this.activities[index].inputs.listToscaDataDefinition);
  }

  private formControlValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const activity = control.value;
      if (!activity || !activity.type || !activity.workflow) {
        return {required:true};
      }
      return null;
    }
  }

  removeFromActivities (index: number) {
    this.activities.splice(index, 1);
    this.activityFormArray.removeAt(index);
  }

  onActivityTypeChange(type: string, index: number) {
    let activity = this.activityFormArray.controls[index].value;
    activity.type = type;
    this.activityFormArray.controls[index].setValue(activity);
  }

  onActivityValueChange (value: any, index: number) {
    let activity = this.activityFormArray.controls[index].value;
    activity.workflow = value;
    this.activityFormArray.controls[index].setValue(activity);
  }

  collectInputNames(index: number) {
    return this.activities[index].inputs.listToscaDataDefinition.map((input) => input.name);
}

  onAddInput(inputOperationParameter: InputOperationParameter, index: number) {
    this.activities[index].inputs.listToscaDataDefinition.push(inputOperationParameter);
    this.activities[index].inputs.listToscaDataDefinition = Array.from(this.activities[index].inputs.listToscaDataDefinition);
  }

  getInputs(index: number) {
    if (this.activities[index].inputs.listToscaDataDefinition) {
      let test: InputOperationParameter[] = this.activities[index].inputs.listToscaDataDefinition;
      return test;
    }
    return {};
  }

  onInputValueChange(changedInput: InputOperationParameter, index: number) {
    if (changedInput.value instanceof Object) {
        changedInput.value = JSON.stringify(changedInput.value);
    }
    const inputOperationParameter = this.activities[index].inputs.listToscaDataDefinition.find(value => value.name == changedInput.name);
    inputOperationParameter.toscaFunction = null;
    inputOperationParameter.value = changedInput.value;
    inputOperationParameter.subPropertyToscaFunctions = changedInput.subPropertyToscaFunctions;
    if (changedInput.isToscaFunction()) {
        inputOperationParameter.toscaFunction = changedInput.toscaFunction;
        inputOperationParameter.value = changedInput.toscaFunction.buildValueString();
    }
}

  onInputDelete(inputName: string, index: number) {
    const currentInputs = this.activities[index].inputs.listToscaDataDefinition;
    const input1 = currentInputs.find(value => value.name === inputName);
    const indexOfInput = currentInputs.indexOf(input1);
    if (indexOfInput === -1) {
        console.error(`Could not delete input '${inputName}'. Input not found.`);
        return;
    }
    currentInputs.splice(currentInputs.indexOf(input1), 1);
    this.activities[index].inputs.listToscaDataDefinition = Array.from(currentInputs);
  }
}
