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

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RelationshipOperationsStepComponent } from './relationship-operations-step.component';
import {CreateInterfaceOperationComponent} from "../create-interface-operation/create-interface-operation.component";
import {InterfaceOperationListComponent} from "./interface-operation-list/interface-operation-list.component";
import {TranslatePipe} from "../../../../../shared/translator/translate.pipe";
import {TranslateService} from "../../../../../shared/translator/translate.service";
import {SdcUiComponentsModule} from "onap-ui-angular/dist";
import {CreateInputRowComponent} from "../create-interface-operation/create-input-row/create-input-row.component";
import {ReactiveFormsModule} from "@angular/forms";
import {TabModule} from "../../../../../components/ui/tabs/tabs.module";
import {UiElementsModule} from "../../../../../components/ui/ui-elements.module";
import {RouterModule} from "@angular/router";
import {APP_BASE_HREF} from "@angular/common";
import {ConnectionWizardService} from "../connection-wizard.service";
import {ComponentService} from "../../../../../services/component-services/component.service";
import {WorkspaceService} from "../../../../workspace/workspace.service";
import {Subject} from "rxjs";

describe('RelationshipOperationsStepComponent', () => {
  let component: RelationshipOperationsStepComponent;
  let fixture: ComponentFixture<RelationshipOperationsStepComponent>;
  let interfaceTypes: Subject<{ [id: string]: Array<string> }>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RelationshipOperationsStepComponent, CreateInterfaceOperationComponent,
          CreateInputRowComponent, InterfaceOperationListComponent, TranslatePipe ],
      providers: [
        {provide: TranslateService, useValue: {translate: (phrase: string) => phrase}},
        // The component reads the asset off WorkspaceService (written by the route resolver) rather
        // than out of a route param, so no NavigationService is involved any more.
        {provide: WorkspaceService, useValue: {component: {uniqueId: 'id-1'}}},
        {provide: ConnectionWizardService, useValue: {selectedMatch: {}}},
        {provide: ComponentService, useValue: {getInterfaceTypes: () => interfaceTypes}},
      ],
      imports: [SdcUiComponentsModule, ReactiveFormsModule, TabModule, UiElementsModule]


    })
    .compileComponents();
  }));

  beforeEach(() => {
    interfaceTypes = new Subject();
    fixture = TestBed.createComponent(RelationshipOperationsStepComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('offers the interface types in an operation form opened before they loaded', () => {
    fixture.detectChanges();
    fixture.nativeElement.querySelector('[data-tests-id="addInterfaceOperation"]').click();
    fixture.detectChanges();

    interfaceTypes.next({'tosca.interfaces.node.lifecycle.Standard': ['create', 'delete']});
    fixture.detectChanges();
    fixture.nativeElement.querySelector('[data-tests-id="interface-name-icon"]').click();
    fixture.detectChanges();

    const options = Array.from(fixture.nativeElement.querySelectorAll('.sdc-dropdown__option'))
        .map((option: HTMLElement) => option.textContent.trim());
    expect(options).toContain('Standard');
  });
});
