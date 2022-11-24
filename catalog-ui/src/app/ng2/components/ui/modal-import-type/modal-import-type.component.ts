/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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
import {Component, Inject, OnInit} from '@angular/core';

@Component({
  selector: 'app-modal-import-type',
  templateUrl: './modal-import-type.component.html',
  styleUrls: ['./modal-import-type.component.less']
})
export class ModalImportTypeComponent implements OnInit {

  file:File = null;

  constructor() {}

  ngOnInit() {
  }

  onFileChange(event: any) {
      this.file = event.target.files[0];
  }

  public onImportDataType(file: any): void {
    if (file && file.filename) {
        console.log("file: " + file.filename);
    }
  }

}
