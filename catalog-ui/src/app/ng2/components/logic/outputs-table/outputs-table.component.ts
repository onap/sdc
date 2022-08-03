/*
* ============LICENSE_START=======================================================
*  Copyright (C) 2021 Nordix Foundation. All rights reserved.
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

import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {InstanceFeDetails} from "../../../../models/instance-fe-details";
import {ModalService} from "../../../services/modal.service";
import {DataTypeService} from "../../../services/data-type.service";
import {TranslateService} from "../../../shared/translator/translate.service";
import {InstanceFeAttributesMap} from "app/models/attributes-outputs/attribute-fe-map";
import {Select} from "@ngxs/store";
import {WorkspaceState} from "../../../store/states/workspace.state";
import {OutputFEModel} from "app/models/attributes-outputs/output-fe-model";

@Component({
  selector: 'outputs-table',
  templateUrl: './outputs-table.component.html',
  styleUrls: ['./outputs-table.component.less']
})
export class OutputsTableComponent implements OnInit {
  @Select(WorkspaceState.isViewOnly)
  isViewOnly$: boolean;

  @ViewChild('componentOutputsTable')
  private table: any;

  @Input() outputs: Array<OutputFEModel>;
  @Input() instanceNamesMap: Map<string, InstanceFeDetails>;
  @Input() readonly: boolean;
  @Input() isLoading: boolean;
  @Input() componentType: string;
  @Output() outputChanged: EventEmitter<any> = new EventEmitter<any>();
  @Output() deleteOutput: EventEmitter<any> = new EventEmitter<any>();
  @Input() feAttributesMap: InstanceFeAttributesMap;

  deleteMsgTitle: string;
  modalDeleteBtn: string;
  modalCancelBtn: string;
  sortBy: string;
  reverse: boolean;
  selectedOutputToDelete: OutputFEModel;

  constructor(private modalService: ModalService,
              private dataTypeService: DataTypeService,
              private translateService: TranslateService) {
  }

  ngOnInit() {
    this.translateService.languageChangedObservable.subscribe((lang) => {
      this.deleteMsgTitle = this.translateService.translate('DELETE_OUTPUT_TITLE');
      this.modalDeleteBtn = this.translateService.translate('MODAL_DELETE');
      this.modalCancelBtn = this.translateService.translate('MODAL_CANCEL');

    });
  }

  sort = (sortBy) => {
    this.reverse = (this.sortBy === sortBy) ? !this.reverse : true;
    let reverse = this.reverse ? 1 : -1;
    this.sortBy = sortBy;
    let instanceNameMapTemp = this.instanceNamesMap;
    let itemIdx1Val = "";
    let itemIdx2Val = "";
    this.outputs.sort(function (itemIdx1, itemIdx2) {
      if (sortBy == 'instanceUniqueId') {
        itemIdx1Val = (itemIdx1[sortBy] && instanceNameMapTemp[itemIdx1[sortBy]] !== undefined) ? instanceNameMapTemp[itemIdx1[sortBy]].name : "";
        itemIdx2Val = (itemIdx2[sortBy] && instanceNameMapTemp[itemIdx2[sortBy]] !== undefined) ? instanceNameMapTemp[itemIdx2[sortBy]].name : "";
      } else {
        itemIdx1Val = itemIdx1[sortBy];
        itemIdx2Val = itemIdx2[sortBy];
      }
      if (itemIdx1Val < itemIdx2Val) {
        return -1 * reverse;
      } else if (itemIdx1Val > itemIdx2Val) {
        return 1 * reverse;
      } else {
        return 0;
      }
    });
  };

  onOutputChanged = (output, event) => {
    output.updateDefaultValueObj(event.value, event.isValid);
    this.outputChanged.emit(output);
  };

  onRequiredChanged = (output: OutputFEModel, event) => {
    this.outputChanged.emit(output);
  }

  onDeleteOutput = () => {
    this.deleteOutput.emit(this.selectedOutputToDelete);
    this.modalService.closeCurrentModal();
  };

  openDeleteModal = (output: OutputFEModel) => {
    this.selectedOutputToDelete = output;
    this.modalService.createActionModal("Delete Output", "Are you sure you want to delete this output?", "Delete", this.onDeleteOutput, "Close").instance.open();
  }

  getConstraints(output: OutputFEModel): string[] {
    if (output.outputPath) {
      const pathValuesName = output.outputPath.split('#');
      const rootPropertyName = pathValuesName[0];
      const propertyName = pathValuesName[1];
      let filteredRootPropertyType = _.values(this.feAttributesMap)[0].filter(property =>
          property.name == rootPropertyName);
      if (filteredRootPropertyType.length > 0) {
        let rootPropertyType = filteredRootPropertyType[0].type;
        return this.dataTypeService.getConstraintsByParentTypeAndUniqueID(rootPropertyType, propertyName);
      } else {
        return null;
      }

    } else {
      return null;
    }
  }

  checkInstanceFeAttributesMapIsFilled() {
    return _.keys(this.feAttributesMap).length > 0
  }

}
