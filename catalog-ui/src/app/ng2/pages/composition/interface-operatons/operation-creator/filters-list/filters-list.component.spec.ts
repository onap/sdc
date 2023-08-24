/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation
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
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FiltersListComponent } from './filters-list.component';
import { TranslateModule } from '../../../../../shared/translator/translate.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ToscaFunctionModule } from '../../../../properties-assignment/tosca-function/tosca-function.module';
import { SdcUiComponentsModule } from "onap-ui-angular/dist";

describe('FiltersListComponent', () => {
  let component: FiltersListComponent;
  let fixture: ComponentFixture<FiltersListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FiltersListComponent ],
      imports: [
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        SdcUiComponentsModule,
        ToscaFunctionModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FiltersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
