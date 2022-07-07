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
import {ToscaGetFunctionComponent} from './tosca-get-function.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {TranslateModule} from "../../../../shared/translator/translate.module";
import {UiElementsModule} from "../../../../components/ui/ui-elements.module";
import {TopologyTemplateService} from "../../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../../../workspace/workspace.service";
import {PropertiesService} from "../../../../services/properties.service";
import {DataTypeService} from "../../../../services/data-type.service";
import {TranslateService} from "../../../../shared/translator/translate.service";
import {ComponentMetadata} from "../../../../../models/component-metadata";

describe('ToscaGetFunctionComponent', () => {
    let component: ToscaGetFunctionComponent;
    let fixture: ComponentFixture<ToscaGetFunctionComponent>;
    let topologyTemplateServiceMock: Partial<TopologyTemplateService>;
    let workspaceServiceMock: Partial<WorkspaceService> = {
        metadata: new ComponentMetadata()
    };
    let propertiesServiceMock: Partial<PropertiesService>;
    let dataTypeServiceMock: Partial<DataTypeService>;
    let translateServiceMock: Partial<TranslateService>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [ToscaGetFunctionComponent],
            imports: [
                FormsModule,
                ReactiveFormsModule,
                TranslateModule,
                UiElementsModule
            ],
            providers: [
                {provide: TopologyTemplateService, useValue: topologyTemplateServiceMock},
                {provide: WorkspaceService, useValue: workspaceServiceMock},
                {provide: PropertiesService, useValue: propertiesServiceMock},
                {provide: DataTypeService, useValue: dataTypeServiceMock},
                {provide: TranslateService, useValue: translateServiceMock}
            ]
        })
        .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ToscaGetFunctionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
