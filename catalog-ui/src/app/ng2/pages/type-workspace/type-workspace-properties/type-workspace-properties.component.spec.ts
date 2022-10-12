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

import {TypeWorkspacePropertiesComponent} from './type-workspace-properties.component';
import {FormsModule} from "@angular/forms";
import {TranslateModule} from "../../../shared/translator/translate.module";
import {TranslateService} from "../../../shared/translator/translate.service";
import {DataTypeService} from "../../../services/data-type.service";
import {Observable} from "rxjs/Observable";
import {DataTypeModel} from "../../../../models/data-types";
import {Component, ViewChild} from "@angular/core";
import {PropertyBEModel} from "../../../../models/properties-inputs/property-be-model";
import {ModalService} from "../../../services/modal.service";

describe('TypeWorkspacePropertiesComponent', () => {
    const messages = require("../../../../../assets/languages/en_US.json");
    let modalService: Partial<ModalService> = {};
    let testHostComponent: TestHostComponent;
    let testHostFixture: ComponentFixture<TestHostComponent>;
    let dataTypeServiceMock: Partial<DataTypeService> = {
        findAllProperties: jest.fn( (dataTypeId) => {
            if (dataTypeId === 'dataTypeId') {
                const property1 = new PropertyBEModel();
                property1.name = 'property1'
                property1.type = 'string'
                return Observable.of([property1]);
            }
            return Observable.of([]);
        })
    };

    let translateServiceMock: Partial<TranslateService> = {
        'translate': jest.fn( (translateKey: string) => {
            return messages[translateKey];
        })
    };

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [TypeWorkspacePropertiesComponent, TestHostComponent],
            imports: [
                TranslateModule,
                FormsModule
            ],
            providers: [
                {provide: DataTypeService, useValue: dataTypeServiceMock},
                {provide: TranslateService, useValue: translateServiceMock},
                {provide: ModalService, useValue: modalService}
            ]
        })
        .compileComponents();
    }));

    beforeEach(() => {
        testHostFixture = TestBed.createComponent(TestHostComponent);
        testHostComponent = testHostFixture.componentInstance;
        testHostFixture.detectChanges();
    });

    it('should create', () => {
        expect(testHostComponent).toBeTruthy();
    });

    it('empty property list', () => {
        const element = testHostFixture.nativeElement;
        const div: HTMLDivElement = element.querySelector('.no-row-text');
        expect(div.textContent).toContain(messages.PROPERTY_LIST_EMPTY_MESSAGE);
    });

    it('test property list', () => {
        testHostFixture = TestBed.createComponent(TestHostComponent);
        testHostComponent = testHostFixture.componentInstance;
        const dataType = new DataTypeModel();
        dataType.uniqueId = 'dataTypeId';
        testHostComponent.typeWorkspacePropertiesComponent.dataType = dataType;
        testHostFixture.detectChanges();

        const element = testHostFixture.nativeElement;
        expect(element.querySelector('.no-row-text')).toBeFalsy();
        const expectedPropertyName = 'property1';
        const propertyNameLink: HTMLAnchorElement = element.querySelector(`a[data-tests-id^="property-name-${expectedPropertyName}"]`);
        expect(propertyNameLink.textContent).toContain(expectedPropertyName);
    });

    @Component({
        selector: 'host-component',
        template: `<app-type-workspace-properties></app-type-workspace-properties>`
    })
    class TestHostComponent {
        @ViewChild(TypeWorkspacePropertiesComponent)
        public typeWorkspacePropertiesComponent: TypeWorkspacePropertiesComponent;
    }

});
