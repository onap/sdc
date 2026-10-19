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

import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {SvgIconModule} from "onap-ui-angular/dist/svg-icon/svg-icon.module";
import { PropertyMetadataComponent } from './property-metadata.component';

describe('PropertyMetadataComponent', () => {
  let component: PropertyMetadataComponent;
  let fixture: ComponentFixture<PropertyMetadataComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PropertyMetadataComponent ],
      imports: [FormsModule, ReactiveFormsModule, SvgIconModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PropertyMetadataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders Add Metadata Entry as a tertiary button that does not submit the form', () => {
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('[data-tests-id="add-metadata-entry-button"]');
    expect(button.tagName).toBe('BUTTON');
    expect(button.type).toBe('button');
    expect(button.classList).toContain('sdc-button__tertiary');
    expect(button.textContent.trim()).toBe('Add Metadata Entry');

    const before = component.propertyMetadataArray.length;
    button.click();
    expect(component.propertyMetadataArray.length).toBe(before + 1);
  });
});
