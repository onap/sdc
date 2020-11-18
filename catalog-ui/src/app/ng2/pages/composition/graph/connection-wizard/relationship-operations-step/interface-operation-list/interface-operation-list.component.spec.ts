/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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

import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {InterfaceOperationListComponent} from './interface-operation-list.component';
import {UiElementsModule} from "../../../../../../components/ui/ui-elements.module";
import {SdcUiComponentsModule} from "onap-ui-angular/dist";
import {TranslatePipe} from "../../../../../../shared/translator/translate.pipe";
import {TranslateService} from "../../../../../../shared/translator/translate.service";

describe('InterfaceOperationListComponent', () => {
  let component: InterfaceOperationListComponent;
  let fixture: ComponentFixture<InterfaceOperationListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [InterfaceOperationListComponent, TranslatePipe],
      imports: [SdcUiComponentsModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InterfaceOperationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
