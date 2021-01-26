/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 Nordix Foundation. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import {NO_ERRORS_SCHEMA, SimpleChange} from '@angular/core';
import {ComponentFixture} from '@angular/core/testing';
import {ConfigureFn, configureTests} from '../../../../../jest/test-config.helper';
import {ContentAfterLastDotPipe} from '../../../pipes/contentAfterLastDot.pipe';
import {KeysPipe} from '../../../pipes/keys.pipe';
import {SearchFilterPipe} from '../../../pipes/searchFilter.pipe';
import {ModalService} from '../../../services/modal.service';
import {AttributeRowSelectedEvent, AttributesTableComponent} from './attributes-table.component';
import {AttributesService} from "../../../services/attributes.service";
import {AttributeFEModel} from "../../../../models/attributes-outputs/attribute-fe-model";
import {AttributeBEModel} from "app/models/attributes-outputs/attribute-be-model";
import {DerivedFEAttribute} from "../../../../models/attributes-outputs/derived-fe-attribute";
import {PropertiesOrderByPipe} from "app/ng2/pipes/properties-order-by.pipe";

describe('attributes-table component', () => {

  let fixture: ComponentFixture<AttributesTableComponent>;
  let attributesServiceMock: Partial<AttributesService>;
  let modalServiceMock: Partial<ModalService>;

  beforeEach(
      () => {
        attributesServiceMock = {
          undoDisableRelatedAttributes: jest.fn(),
          disableRelatedAttributes: jest.fn()
        };
        modalServiceMock = {};

        const configure: ConfigureFn = (testBed) => {
          testBed.configureTestingModule({
            declarations: [
              AttributesTableComponent,
              KeysPipe,
              PropertiesOrderByPipe,
              SearchFilterPipe,
              ContentAfterLastDotPipe
            ],
            imports: [],
            schemas: [NO_ERRORS_SCHEMA],
            providers: [
              {provide: AttributesService, useValue: attributesServiceMock},
              {provide: ModalService, useValue: modalServiceMock}
            ],
          });
        };

        configureTests(configure).then((testBed) => {
          fixture = testBed.createComponent(AttributesTableComponent);
        });
      }
  );

  it('When Properties assignment page is loaded, it is sorted by attribute name (acsending)', () => {
    const fePropertiesMapValues = new SimpleChange('previousValue', 'currentValue', true);
    const changes = {
      fePropertiesMap: fePropertiesMapValues
    };

    // init values before ngOnChanges was called
    fixture.componentInstance.sortBy = 'existingValue';

    fixture.componentInstance.ngOnChanges(changes);

    expect(fixture.componentInstance.reverse).toEqual(true);
    expect(fixture.componentInstance.direction).toEqual(fixture.componentInstance.ascUpperLettersFirst);
    expect(fixture.componentInstance.sortBy).toEqual('name');
    expect(fixture.componentInstance.path.length).toEqual(1);
    expect(fixture.componentInstance.path[0]).toEqual('name');
  });

  it('When ngOnChanges is called without fePropertiesMap,' +
      ' sortBy will remain as it was', () => {
    const fePropertiesMapValues = new SimpleChange('previousValue', 'currentValue', true);
    const changes = {
      dummyKey: fePropertiesMapValues
    };

    // init values before ngOnChanges was called
    fixture.componentInstance.sortBy = 'existingValue';
    fixture.componentInstance.sort = jest.fn();

    fixture.componentInstance.ngOnChanges(changes);

    expect(fixture.componentInstance.sortBy).toEqual('existingValue');
  });

  it('When sort is called init this.direction to 1', () => {
    // init values
    fixture.componentInstance.reverse = false;
    fixture.componentInstance.direction = 0;
    fixture.componentInstance.sortBy = 'initialize.Value';
    fixture.componentInstance.path = [];

    // call sore function
    fixture.componentInstance.sort('initialize.Value');

    // expect that
    expect(fixture.componentInstance.reverse).toBe(true);
    expect(fixture.componentInstance.direction).toBe(fixture.componentInstance.ascUpperLettersFirst);
    expect(fixture.componentInstance.sortBy).toBe('initialize.Value');
    expect(fixture.componentInstance.path.length).toBe(2);
    expect(fixture.componentInstance.path[0]).toBe('initialize');
    expect(fixture.componentInstance.path[1]).toBe('Value');
  });

  it('When sort is called init this.direction to -1', () => {
    // init values
    fixture.componentInstance.reverse = true;
    fixture.componentInstance.direction = 0;
    fixture.componentInstance.sortBy = 'initialize.Value';
    fixture.componentInstance.path = [];

    // call sore function
    fixture.componentInstance.sort('initialize.Value');

    // expect that
    expect(fixture.componentInstance.reverse).toBe(false);
    expect(fixture.componentInstance.direction).toBe(fixture.componentInstance.descLowerLettersFirst);
  });

  it('When onPropertyChanged is called, event is emitted', () => {
    spyOn(fixture.componentInstance.emitter, 'emit');
    fixture.componentInstance.onAttributeChanged('testProperty');
    expect(fixture.componentInstance.emitter.emit).toHaveBeenCalledWith('testProperty');
  });

  it('When onClickPropertyRow is called, selectedPropertyId is updated and event is emitted.', () => {
    const attributeFEModel = new AttributeFEModel(new AttributeBEModel());
    attributeFEModel.name = 'attributeName';
    const attributeRowSelectedEvent: AttributeRowSelectedEvent = new AttributeRowSelectedEvent(attributeFEModel, 'instanceName');

    spyOn(fixture.componentInstance.selectAttributeRow, 'emit');
    fixture.componentInstance.onClickAttributeRow(attributeFEModel, 'instanceName');

    expect(fixture.componentInstance.selectedAttributeId).toBe('attributeName');
    expect(fixture.componentInstance.selectAttributeRow.emit).toHaveBeenCalledWith(attributeRowSelectedEvent);
  });

  it('When onClickPropertyInnerRow is called, event is emitted.', () => {
    const derivedFEProperty = new DerivedFEAttribute(new AttributeBEModel());
    const attributeRowSelectedEvent: AttributeRowSelectedEvent = new AttributeRowSelectedEvent(derivedFEProperty, 'instanceName');
    spyOn(fixture.componentInstance.selectAttributeRow, 'emit');
    fixture.componentInstance.onClickAttributeInnerRow(derivedFEProperty, 'instanceName');

    expect(fixture.componentInstance.selectAttributeRow.emit).toHaveBeenCalledWith(attributeRowSelectedEvent);
  });

  it('When attributeChecked is called, attributesService.undoDisableRelatedProperties is called and event is emitted.', () => {

    const attributeFEModel = new AttributeFEModel(new AttributeBEModel());
    attributeFEModel.isSelected = false;

    spyOn(fixture.componentInstance.updateCheckedAttributeCount, 'emit');
    fixture.componentInstance.attributeChecked(attributeFEModel);
    expect(attributesServiceMock.undoDisableRelatedAttributes).toHaveBeenCalledWith(attributeFEModel, undefined);
    expect(fixture.componentInstance.updateCheckedAttributeCount.emit).toHaveBeenCalledWith(false);
  });

  it('When attributeChecked is called, attributesService.disableRelatedProperties is called and event is emitted.', () => {

    const attributeFEModel = new AttributeFEModel(new AttributeBEModel());
    attributeFEModel.isSelected = true;

    spyOn(fixture.componentInstance.updateCheckedAttributeCount, 'emit');
    fixture.componentInstance.attributeChecked(attributeFEModel);
    expect(attributesServiceMock.disableRelatedAttributes).toHaveBeenCalledWith(attributeFEModel, undefined);
    expect(fixture.componentInstance.updateCheckedAttributeCount.emit).toHaveBeenCalledWith(true);
  });

});
