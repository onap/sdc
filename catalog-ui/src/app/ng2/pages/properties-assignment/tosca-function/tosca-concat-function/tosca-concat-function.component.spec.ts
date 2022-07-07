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

import {ToscaConcatFunctionComponent} from './tosca-concat-function.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ToscaFunctionComponent} from "../tosca-function.component";
import {TranslateModule} from "../../../../shared/translator/translate.module";
import {ToscaGetFunctionComponent} from "../tosca-get-function/tosca-get-function.component";
import {UiElementsModule} from "../../../../components/ui/ui-elements.module";

describe('ToscaConcatFunctionComponent', () => {
    let component: ToscaConcatFunctionComponent;
    let fixture: ComponentFixture<ToscaConcatFunctionComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [ToscaConcatFunctionComponent, ToscaFunctionComponent, ToscaGetFunctionComponent],
            imports: [
                FormsModule,
                ReactiveFormsModule,
                TranslateModule,
                UiElementsModule
            ]
        })
        .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ToscaConcatFunctionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
