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

describe('TypeWorkspaceGeneralComponent', () => {
  let component: TypeWorkspaceGeneralComponent;
  let fixture: ComponentFixture<TypeWorkspaceGeneralComponent>;
  let translateServiceMock: Partial<TranslateService> = {
    'translate': jest.fn()
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TypeWorkspaceGeneralComponent ],
      imports: [
        ReactiveFormsModule,
        TranslateModule
      ],
      providers: [
        {provide: TranslateService, useValue: translateServiceMock}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TypeWorkspaceGeneralComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
