/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
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

import {ToscaConcatFunctionComponent} from '../tosca-concat-function/tosca-concat-function.component';
import {ToscaCustomFunctionComponent} from './tosca-custom-function.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ToscaFunctionComponent} from "../tosca-function.component";
import {TranslateModule} from "../../../../shared/translator/translate.module";
import {ToscaGetFunctionComponent} from "../tosca-get-function/tosca-get-function.component";
import {UiElementsModule} from "../../../../components/ui/ui-elements.module";
import {YamlFunctionComponent} from "../yaml-function/yaml-function.component";
import {TopologyTemplateService} from "../../../../services/component-services/topology-template.service";
import {Observable} from "rxjs/Observable";
import {defaultCustomFunctionsMock} from "../../../../../../jest/mocks/default-custom-tosca-function.mock";

describe('ToscaCustomFunctionComponent', () => {
    let component: ToscaCustomFunctionComponent;
    let fixture: ComponentFixture<ToscaCustomFunctionComponent>;
    let topologyTemplateServiceMock: Partial<TopologyTemplateService>;

    beforeEach(async(() => {
        topologyTemplateServiceMock = {
            getDefaultCustomFunction: jest.fn().mockImplementation(() => Observable.of(defaultCustomFunctionsMock))
        };
        TestBed.configureTestingModule({
            declarations: [
                ToscaCustomFunctionComponent,
                ToscaConcatFunctionComponent,
                ToscaFunctionComponent,
                ToscaGetFunctionComponent,
                YamlFunctionComponent],
            imports: [
                FormsModule,
                ReactiveFormsModule,
                TranslateModule,
                UiElementsModule
            ],
            providers: [
                {provide: TopologyTemplateService, useValue: topologyTemplateServiceMock}
            ]
        })
        .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ToscaCustomFunctionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
