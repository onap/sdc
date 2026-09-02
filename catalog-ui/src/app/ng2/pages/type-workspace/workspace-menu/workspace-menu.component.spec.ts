/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  Modifications Copyright (C) 2026 Deutsche Telekom AG.
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
import {States} from "../../../utils/constants";
import {IAppMenu} from "../../../../models/app-config";
import {SdcMenuToken} from "../../../config/sdc-menu.config";
import {TypeWorkspaceService} from "../type-workspace.service";
import {NavigationService} from "../../../services/navigation.service";
import {Subject} from "rxjs/Subject";

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
  // The url getCurrentStateName() has to map back to States.TYPE_WORKSPACE.
  let routerMock: any = {
    'url': '/dashboard/' + States.TYPE_WORKSPACE + '/datatype/id-1/general',
    'events': new Subject<any>(),
    'navigateByUrl': jest.fn(() => Promise.resolve(true))
  };
  let routeMock: any = {
    'snapshot': {'params': {'type': 'datatype', 'id': 'id-1'}, 'queryParams': {}, 'firstChild': null}
  };
  // A REAL NavigationService over the mocked Router — the component reads the current state name
  // through the facade now instead of injecting $state itself.
  let navigationServiceMock = new NavigationService(routerMock, routeMock, {unsavedChanges: false} as any);

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkspaceMenuComponent ],
      providers: [
        {provide: CacheService, useValue: cacheService},
        {provide: NavigationService, useValue: navigationServiceMock},
        TypeWorkspaceService,
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
