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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TypeWorkspaceGeneralComponent} from './type-workspace-general.component';
import {ReactiveFormsModule} from "@angular/forms";
import {TranslateModule} from "../../../shared/translator/translate.module";
import {TranslateService} from "../../../shared/translator/translate.service";
import {SdcUiComponentsModule} from "onap-ui-angular/dist";
import {Observable} from "rxjs/Observable";
import {DataTypeModel} from "../../../../models/data-types";
import {DataTypeService} from "../../../services/data-type.service";
import {ModelService} from "../../../services/model.service";
import {IWorkspaceViewModelScope} from "../../../../view-models/workspace/workspace-view-model";
import {IScope} from "angular";
import {States} from "../../../../utils/constants";

describe('TypeWorkspaceGeneralComponent', () => {

  let component: TypeWorkspaceGeneralComponent;
  let fixture: ComponentFixture<TypeWorkspaceGeneralComponent>;
  let dataTypeServiceMock: Partial<DataTypeService>;
  let modelServiceMock: Partial<ModelService>;
  let translateServiceMock: Partial<TranslateService> = {
    'translate': jest.fn()
  };

    let importedFileMock: File = null;
    let stateParamsMock: Partial<ng.ui.IStateParamsService> = {
        'importedFile': importedFileMock
    };
  let resolveMock = {"$stateParams": stateParamsMock};
    let parentScopeMock: Partial<IScope> = {
        '$resolve': resolveMock
    };
    let scopeMock_: Partial<IWorkspaceViewModelScope> = {
        '$parent': parentScopeMock,
        'current': {
            'name': States.TYPE_WORKSPACE
        }
    }

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TypeWorkspaceGeneralComponent ],
      imports: [
        ReactiveFormsModule,
        SdcUiComponentsModule,
        TranslateModule
      ],
      providers: [
        {provide: TranslateService, useValue: translateServiceMock},
        {provide: "$scope", useValue: scopeMock_ },
        {provide: "$state", useValue: {}},
        {provide: DataTypeService, useValue: dataTypeServiceMock},
        {provide: ModelService, useValue: modelServiceMock},
        {provide: TranslateService, useValue: translateServiceMock}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TypeWorkspaceGeneralComponent);
    component = fixture.componentInstance;
    component.dataTypeMap$ = new Observable<Map<string, DataTypeModel>>();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
