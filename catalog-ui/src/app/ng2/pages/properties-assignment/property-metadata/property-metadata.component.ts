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
  AbstractControl, FormArray,
  FormControl,
  FormGroup, ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';

@Component({
  selector: 'app-property-metadata',
  templateUrl: './property-metadata.component.html',
  styleUrls: ['./property-metadata.component.less']
})
export class PropertyMetadataComponent implements OnInit {

  @Input() propertyMetadata: any;
  @Input() isViewOnly: boolean = false;
  @Output() onPropertyMetadataChange: EventEmitter<any> = new EventEmitter<any>();

  propertyMetadataArray: Metadata[] = [];
  metadataFormArray: FormArray = new FormArray([]);
  metadataForm: FormGroup = new FormGroup (
    {
      'metadataFormList': this.metadataFormArray
    }
  );
  validationMessages = {
    metadata: [
      { type: 'required', message: 'Metadata key and value is required'}
    ]
  };

  ngOnInit() {
    this.initForm();
  }

  private initForm(): void {
    this.metadataForm.valueChanges.subscribe(() => {
      this.emitOnPropertyMetadataChange();
    });
    if (this.propertyMetadata) {
      for (const key in this.propertyMetadata) {
        const value = this.propertyMetadata[key];
        let metadata: Metadata = {
          key: key,
          value: value
        }
        this.propertyMetadataArray.push(metadata);
        this.metadataFormArray.push(
          new FormControl(metadata, [Validators.required, Validators.minLength(1)])
        );
      }
    }
  }

  addMetadataField() {
    let metadata: Metadata = {
      key: null,
      value: null,
    }
    this.propertyMetadataArray.push(metadata);
    this.metadataFormArray.push(
      new FormControl(metadata, [Validators.required, this.formControlValidator()])
    );
  }

  private formControlValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const metadata = control.value;

      if (!metadata || !metadata.key || !metadata.value) {
        return {required:true};
      }
      return null;
    }
  }

  removeFromList(index: number) {
    this.propertyMetadataArray.splice(index, 1);;
    this.metadataFormArray.removeAt(index);
  }

  private emitOnPropertyMetadataChange(): void {
    let newMetadata = new Object;
    for (const metadata of this.propertyMetadataArray) {
      const key = metadata.key;
      const value = metadata.value;
      newMetadata[key] = value;
    }

    this.onPropertyMetadataChange.emit({
      metadata: newMetadata,
      valid: this.metadataForm.valid
    });
  }

  onChangePropertyMetadataValue(index: number, newValue: any) {
    let metadata = this.metadataFormArray.controls[index].value;
    metadata.value = newValue;
    this.metadataFormArray.controls[index].setValue(metadata);
  }

  onChangePropertyMetadataKey(index: number, newKey: any) {
    let metadata = this.metadataFormArray.controls[index].value;
    metadata.key = newKey;
    this.metadataFormArray.controls[index].setValue(metadata);
  }
}

export interface Metadata {
  key: string,
  value: string
}
