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

import {Component, ElementRef, EventEmitter, Inject, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {DataTypeModel} from "../../../../models/data-types";
import {DEFAULT_MODEL_NAME} from "app/utils/constants";
import {IWorkspaceViewModelScope} from "../../../../view-models/workspace/workspace-view-model";
import {ServiceDataTypeReader} from "../../../../utils/service-data-type-reader";
import {TranslateService} from "../../../shared/translator/translate.service";
import {SdcUiServices} from "onap-ui-angular/dist";
import {ModelService} from "../../../services/model.service";
import {Model} from "../../../../models/model";
import {DataTypesMap} from "../../../../models/data-types-map";
import {DataTypeService} from "../../../services/data-type.service";
import {Observable} from "rxjs/Observable";
import {IDropDownOption} from "onap-ui-angular/dist/form-elements/dropdown/dropdown-models";
import {ComboBoxComponent} from "onap-ui-angular/dist/autocomplete/combo-box.component";

@Component({
  selector: 'app-type-workspace-general',
  templateUrl: './type-workspace-general.component.html',
  styleUrls: ['./type-workspace-general.component.less']
})
export class TypeWorkspaceGeneralComponent implements OnInit {

  @Input() isViewOnly = true;
  @Input() dataType: DataTypeModel = new DataTypeModel();
  @Output() onImportedType = new EventEmitter<any>();
  importedFile: File;
  models: Array<Model>;
  selectedModelName: string;
  dataTypes: DataTypesMap;
  derivedFromName: string;
  dataTypeMap$: Observable<Map<string, DataTypeModel>>;
  dataTypeMap: Map<string, DataTypeModel>;
  typeOptions: Array<IDropDownOption>;
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

  constructor(@Inject('$scope') private $scope: IWorkspaceViewModelScope,
              @Inject('$state') private $state: ng.ui.IStateService,
              protected dataTypeService: DataTypeService,
              private modalServiceSdcUI: SdcUiServices.ModalService,
              private modelService: ModelService,
              private translateService: TranslateService) {
      this.typeOptions = [];
  }

  ngOnInit(): void {
      this.getImportedFile();

      if (!this.isViewOnly) {
          console.log("file size: " + this.importedFile.size);
          console.log("file type: " + this.importedFile.type);
          console.log("file lastModifiedDate: " + this.importedFile.lastModifiedDate);

          new ServiceDataTypeReader().read(this.importedFile).then(
              (serviceType) => {
                  this.dataType = serviceType;
                  this.dataType.modificationTime = this.importedFile.lastModifiedDate;
                  this.dataType.creationTime = this.importedFile.lastModifiedDate;
                  this.derivedFromName = serviceType.derivedFromName;
                  this.dataType.uniqueId = this.dataType.model ? this.dataType.model + "." + this.dataType.name : this.dataType.name + ".datatype";
                  this.$scope.dataType = this.dataType;
                  this.onImportedType.emit(this.dataType);
              },
              (error) => {
                  const errorMsg = this.translateService.translate('IMPORT_DATA_TYPE_FAILURE_MESSAGE_TEXT');
                  console.error(errorMsg, error);
                  const errorDetails = {
                      'Error': error.reason,
                      'Details': error.message
                  };
                  console.error(error.reason);
                  this.modalServiceSdcUI.openErrorDetailModal('Error', errorMsg,
                      'error-modal', errorDetails);
                  this.$state.go('dashboard');
              });
          this.models = [];
          this.modelService.getModels().subscribe((modelsFound: Model[]) => {
              modelsFound.sort().forEach(model => {
                  this.models.push(model);
              })
              this.dataType.model = new Model({"name": DEFAULT_MODEL_NAME, "derivedFrom": "", "modelType": "normative"});
              this.$scope.dataType = this.dataType;
              this.models.push(this.dataType.model);
              this.resolveTypesForModel();
          });
      }
    this.initForm();
  }

    onModelChange(): void {
        this.selectedModelName = this.models.filter(x => x.name == this.model.value).pop().name;
        console.log("selected model: " + this.selectedModelName);
        this.dataType.model = new Model({"name": this.selectedModelName, "derivedFrom": "", "modelType": "normative"});
        this.dataType.uniqueId = this.dataType.model.name === DEFAULT_MODEL_NAME ?
            this.dataType.name + ".datatype" : this.dataType.model.name + "." + this.dataType.name + ".datatype";
        this.dataType.derivedFrom = undefined;
        this.$scope.dataType.derivedFromName = this.derivedFromName;
        this.$scope.dataType = this.dataType;
        this.$scope.dataType.model = this.dataType.model;
        this.resolveTypesForModel();
        this.derivedFromName = undefined;
        this.$scope.dataType.derivedFromName = this.derivedFromName;
        this.derivedFrom.setValue(null);
    }

    onDerivedFromChange(derived): void {
      this.derivedFromName = derived;
      this.$scope.dataType.derivedFromName = this.derivedFromName;
    }

    resolveTypesForModel(): void {
        this.dataTypeMap$ = new Observable<Map<string, DataTypeModel>>(subscriber => {
        this.dataTypeService.findAllDataTypesByModelIncludingRoot(this.selectedModelName === DEFAULT_MODEL_NAME ? null : this.selectedModelName)
                .then((dataTypesMap: Map<string, DataTypeModel>) => {
                    subscriber.next(dataTypesMap);
                });
        });
        this.dataTypeMap$.subscribe(value => {
            this.dataTypeMap = value;
        });

        this.dataTypeMap$.subscribe((dataTypesMap: Map<string, DataTypeModel>) => {
            this.dataTypeMap = dataTypesMap;
            this.typeOptions = [];
            let isDerivedFromInModel: boolean = false;
            dataTypesMap.forEach((value, key) => {
                const entry = {label: key, value: value.name};
                if (this.derivedFromName === key) {
                    this.dataType.derivedFrom = value;
                    isDerivedFromInModel = true;
                }
                this.typeOptions.push(entry);
            });
            if (!isDerivedFromInModel) {
                this.derivedFromName = undefined;
            }
        });
    }

  private getImportedFile(): void {
      let importedFile = this.$scope["$parent"]["$resolve"]["$stateParams"]["importedFile"];
      this.importedFile = <File>importedFile;
      if (this.importedFile) {
          this.isViewOnly = false;
      }
  }

  private initForm(): void {
    if (!this.dataType) {
      return;
    }
    this.type.setValue(this.dataType.name);
    this.description.setValue(this.dataType.description);
    this.model.setValue(this.dataType.model ? this.dataType.model : this.$scope.dataType && this.$scope.dataType.model ? this.$scope.dataType.model : DEFAULT_MODEL_NAME);
    this.derivedFrom.setValue(this.dataType.derivedFromName);
  }
}
