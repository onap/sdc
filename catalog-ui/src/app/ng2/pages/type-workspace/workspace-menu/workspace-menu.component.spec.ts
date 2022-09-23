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

import {WorkspaceMenuComponent} from './workspace-menu.component';
import {CacheService} from "../../../services/cache.service";
import {States} from "../../../../utils/constants";
import {IAppMenu} from "../../../../models/app-config";
import {SdcMenuToken} from "../../../config/sdc-menu.config";

describe('WorkspaceMenuComponent', () => {
  let component: WorkspaceMenuComponent;
  let fixture: ComponentFixture<WorkspaceMenuComponent>;
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
  let sdcMenuMock: Partial<IAppMenu> = {
    'component_workspace_menu_option': {
      "DataType": [
        {"text": "General", "action": "onMenuItemPressed", "state": "general"}
      ]
    }
  };
  let stateMock: Partial<ng.ui.IStateService> = {
    'current': {
      'name': States.TYPE_WORKSPACE
    }
  };
  let injectorMock: Partial<ng.auto.IInjectorService> = {
    'get': jest.fn(param => {
      if (param === '$state') {
        return stateMock;
      }
    })
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkspaceMenuComponent ],
      providers: [
        {provide: CacheService, useValue: cacheService},
        {provide: '$injector', useValue: injectorMock},
        {provide: SdcMenuToken, useValue: sdcMenuMock}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkspaceMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
