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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PropertyAssignment} from "../../../../../../../models/properties-inputs/property-assignment";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {filter} from "rxjs/operators";

@Component({
  selector: 'app-create-input-row',
  templateUrl: './create-input-row.component.html',
  styleUrls: ['./create-input-row.component.less']
})
export class CreateInputRowComponent implements OnInit {

  @Input() propertyAssignment: PropertyAssignment;
  @Input() isReadOnly: boolean;
  @Output('onDelete') onDeleteEvent: EventEmitter<PropertyAssignment> = new EventEmitter();
  formGroup: FormGroup;

  constructor() { }

  ngOnInit() {
    if (!this.propertyAssignment) {
      this.propertyAssignment = new PropertyAssignment();
    }
    this.formGroup = new FormGroup({
      name: new FormControl(this.propertyAssignment.name, [
        Validators.required
      ]),
      value: new FormControl(this.propertyAssignment.value)
    });

    this.formGroup.statusChanges
    .pipe(
        filter(() => this.formGroup.valid))
    .subscribe(() => this.onFormValid());
  }

  onDelete(propertyAssignment: PropertyAssignment) {
    this.onDeleteEvent.emit(propertyAssignment);
  }

  get formName() {
    return this.formGroup.get('name');
  }

  get formValue() {
    return this.formGroup.get('value');
  }

  private onFormValid() {
    this.propertyAssignment.name = this.formName.value;
    this.propertyAssignment.value = this.formValue.value;
  }
}
