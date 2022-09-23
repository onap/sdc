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

import {Component, Input, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {DataTypeModel} from "../../../../models/data-types";
import { DEFAULT_MODEL_NAME } from "app/utils/constants";

@Component({
  selector: 'app-type-workspace-general',
  templateUrl: './type-workspace-general.component.html',
  styleUrls: ['./type-workspace-general.component.less']
})
export class TypeWorkspaceGeneralComponent implements OnInit {
  @Input() isViewOnly = true;
  @Input() dataType: DataTypeModel = new DataTypeModel();

  DEFAULT_MODEL_NAME = DEFAULT_MODEL_NAME;

  type: FormControl = new FormControl(undefined, [Validators.required, Validators.minLength(1), Validators.maxLength(300)]);
  derivedFrom: FormControl = new FormControl(undefined, [Validators.required, Validators.minLength(1)]);
  description: FormControl = new FormControl(undefined, [Validators.required, Validators.minLength(1)]);
  model: FormControl = new FormControl(undefined, [Validators.required]);
  formGroup: FormGroup = new FormGroup({
    'type': this.type,
    'description': this.description,
    'model': this.model,
    'derivedFrom': this.derivedFrom
  });

  ngOnInit(): void {
    this.initForm();
  }

  private initForm(): void {
    if (!this.dataType) {
      return;
    }
    this.type.setValue(this.dataType.name);
    this.description.setValue(this.dataType.description);
    this.model.setValue(this.dataType.model);
    this.derivedFrom.setValue(this.dataType.derivedFrom);
  }
}
