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

import {InputListComponent} from './input-list.component';
import {TranslateModule} from '../../../../../shared/translator/translate.module';
import {Component, Input} from '@angular/core';
import {DataTypeModel} from '../../../../../../models/data-types';
import {TranslateService} from '../../../../../shared/translator/translate.service';
import {ToscaFunction} from '../../../../../../models/tosca-function';

@Component({selector: 'app-input-list-item', template: ''})
class InputListItemStubComponent {
  @Input() name: string;
  @Input() type: DataTypeModel;
  @Input() dataTypeMap: any;
  @Input() valueObjRef: any;
  @Input() schema: any;
  @Input() allowDeletion: any;
  @Input() isViewOnly: boolean;
  @Input() toscaFunction: ToscaFunction;
  @Input() showToscaFunctionOption: boolean;
}

const translateServiceMock: Partial<TranslateService> = {
  translate: jest.fn((str: string) => {
  })
};

describe('InputListComponent', () => {
  let component: InputListComponent;
  let fixture: ComponentFixture<InputListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ InputListComponent, InputListItemStubComponent ],
      imports: [ 
        TranslateModule
      ],
      providers: [
        { provide: TranslateService, useValue: translateServiceMock }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(InputListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeDefined();
  });
});
