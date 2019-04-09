/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import {Component, EventEmitter, Input, Output, ViewChild, ViewEncapsulation} from "@angular/core";
import {PathsAndNamesDefinition} from "../../../../models/paths-and-names";

@Component({
  selector: 'gab-column-provider',
  templateUrl: './generic-artifact-browser-column-provider.component.html',
  styleUrls: ['./generic-artifact-browser-column-provider.component.less'],
  encapsulation: ViewEncapsulation.None
})
export class GenericArtifactBrowserColumnProviderComponent {
  @Input()
  pathsAndNames: PathsAndNamesDefinition[];

  @Output()
  onCancel = new EventEmitter();
  @Output()
  onSave = new EventEmitter();

  @ViewChild('generalForm') generalForm;
  name: string;
  path: string;

  constructor() {}

  checkNameDuplications(event) {
    const tmp = event.target.value;
    if (tmp && !this.columnsContainsName(tmp)) {
      this.name = tmp;
      this.generalForm.form.controls['name'].setErrors(null);
    } else {
      this.generalForm.form.controls['name'].setErrors({incorrect: true});
    }
  }

  checkPathDuplications(event) {
    const tmp = event.target.value;
    if (tmp && !this.columnsContainsPath(tmp)) {
      this.path = tmp;
      this.generalForm.form.controls['path'].setErrors(null);
    } else {
      this.generalForm.form.controls['path'].setErrors({incorrect: true});
    }
  }

  cancelAddingNewColumn() {
    this.onCancel.emit();
  }

  saveNewColumn() {
    this.onSave.emit();
  }

  addColumn() {
    this.updateColumnFilter(this.name, this.path);
    this.saveNewColumn();
  }

  private updateColumnFilter = (name: string, prop: string): void => {
    this.pathsAndNames.push(new PathsAndNamesDefinition(prop, name));
    this.generalForm.form.controls['name'].setValue("");
    this.generalForm.form.controls['path'].setValue("");
  }

  private columnsContainsName = (name: string): boolean => {
    const columnDefinitions = this.pathsAndNames.filter(column => column.friendlyName.toLowerCase() === name.toLowerCase());
    return columnDefinitions.length > 0;
  }

  private columnsContainsPath = (path: string): boolean => {
    const columnDefinitions = this.pathsAndNames.filter(column => column.path.toLowerCase() === path.toLowerCase());
    return columnDefinitions.length > 0;
  }

}
