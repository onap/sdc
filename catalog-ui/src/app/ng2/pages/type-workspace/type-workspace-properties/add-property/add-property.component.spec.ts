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

import {AddPropertyComponent} from './add-property.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {TranslateModule} from "../../../../shared/translator/translate.module";
import {UiElementsModule} from "../../../../components/ui/ui-elements.module";
import {Component, Input} from "@angular/core";
import {DataTypeModel} from "../../../../../models/data-types";
import {SchemaPropertyGroupModel} from "../../../../../models/schema-property";
import {DataTypeService} from "../../../../services/data-type.service";
import {TranslateService} from "../../../../shared/translator/translate.service";

@Component({selector: 'app-input-list-item', template: ''})
class InputListItemStubComponent {
    @Input() valueObjRef: any;
    @Input() name: string;
    @Input() dataTypeMap: Map<string, DataTypeModel>;
    @Input() type: DataTypeModel;
    @Input() schema: SchemaPropertyGroupModel;
    @Input() nestingLevel: number;
    @Input() isExpanded: boolean = false;
    @Input() isListChild: boolean = false;
    @Input() isMapChild: boolean = false;
    @Input() listIndex: number;
    @Input() isViewOnly: boolean;
    @Input() allowDeletion: boolean = false;
}

describe('AddPropertyComponent', () => {
    let dataTypeServiceMock: Partial<DataTypeService> = {
        findAllDataTypesByModel: jest.fn(args => {
            return Promise.resolve(new Map());
        })
    };
    let translateServiceMock: Partial<TranslateService> = {
        translate: jest.fn((str: string) => {})
    };
    let component: AddPropertyComponent;
    let fixture: ComponentFixture<AddPropertyComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [AddPropertyComponent, InputListItemStubComponent],
            imports: [
                FormsModule,
                ReactiveFormsModule,
                TranslateModule,
                UiElementsModule
            ],
            providers: [
                {provide: DataTypeService, useValue: dataTypeServiceMock},
                {provide: TranslateService, useValue: translateServiceMock}
            ]
        })
        .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(AddPropertyComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
