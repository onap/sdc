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

import {TypeWorkspaceComponent} from './type-workspace.component';
import {ReactiveFormsModule} from "@angular/forms";
import {TranslateModule} from "../../shared/translator/translate.module";
import {UiElementsModule} from "../../components/ui/ui-elements.module";
import {DataTypeService} from "../../services/data-type.service";
import {TranslateService} from "../../shared/translator/translate.service";
import {WorkspaceMenuComponent} from "./workspace-menu/workspace-menu.component";
import {TypeWorkspaceGeneralComponent} from "./type-workspace-general/type-workspace-general.component";
import {LayoutModule} from "../../components/layout/layout.module";
import {CacheService} from "../../services/cache.service";
import {IAppMenu, SdcMenuToken} from "../../config/sdc-menu.config";
import {AuthenticationService} from "../../services/authentication.service";
import {ISdcConfig, SdcConfigToken} from "../../config/sdc-config.config";
import {States} from "../../../utils/constants";
import {IUserProperties} from "../../../models/user";
import {Observable} from "rxjs/Observable";

describe('TypeWorkspaceComponent', () => {
  let component: TypeWorkspaceComponent;
  let fixture: ComponentFixture<TypeWorkspaceComponent>;
  let translateServiceMock: Partial<TranslateService> = {
    'languageChangedObservable': Observable.of(),
    'translate': jest.fn()
  };
  let dataTypeServiceMock: Partial<DataTypeService>;
  let cacheService: Partial<CacheService> = {
    'get': jest.fn(param => {
      if (param === 'version') {
        return 'version';
      }
      if (param === 'user') {
        return {};
      }
    })
  };
  let stateMock: Partial<ng.ui.IStateService> = {
    'current': {
      'name': States.TYPE_WORKSPACE
    }
  };
  let stateParamsMock: Partial<ng.ui.IStateParamsService> = {};
  let injectorMock: Partial<ng.auto.IInjectorService> = {
    'get': jest.fn(param => {
      if (param === '$state') {
        return stateMock;
      }
    })
  };
  let sdcMenuMock: Partial<IAppMenu> = {
    'component_workspace_menu_option': {
      "DataType": [
          {"text": "General", "action": "onMenuItemPressed", "state": "general"}
      ]
    }
  };
  let sdcConfigMock: Partial<ISdcConfig>;
  let user: Partial<IUserProperties> = {
    'role': 'DESIGNER'
  }
  let authenticationService: Partial<AuthenticationService> = {
    'getLoggedinUser': jest.fn(() => {
      return user;
    })
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TypeWorkspaceComponent, WorkspaceMenuComponent, TypeWorkspaceGeneralComponent ],
      imports: [
        ReactiveFormsModule,
        TranslateModule,
        UiElementsModule,
        LayoutModule
      ],
      providers: [
        {provide: DataTypeService, useValue: dataTypeServiceMock},
        {provide: TranslateService, useValue: translateServiceMock},
        {provide: CacheService, useValue: cacheService},
        {provide: '$state', useValue: stateMock},
        {provide: '$stateParams', useValue: stateParamsMock},
        {provide: '$injector', useValue: injectorMock},
        {provide: SdcMenuToken, useValue: sdcMenuMock},
        {provide: SdcConfigToken, useValue: sdcConfigMock},
        {provide: AuthenticationService, useValue: authenticationService},
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TypeWorkspaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
