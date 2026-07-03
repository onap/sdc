/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2026 Deutsche Telekom AG
 */

import * as _ from 'lodash';
(window as any)._ = _;

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture } from '@angular/core/testing';
import { ConfigureFn, configureTests } from '../../../../../../jest/test-config.helper';
import { DynamicPropertyComponent } from './dynamic-property.component';
import { PropertiesUtils } from '../../../../pages/properties-assignment/services/properties.utils';
import { DataTypeService } from '../../../../services/data-type.service';
import { PropertyFEModel, PropertyBEModel } from 'app/models';
import { PROPERTY_TYPES } from 'app/utils';
import { ContentAfterLastDotPipe } from '../../../../pipes/contentAfterLastDot.pipe';
import { FilterChildPropertiesPipe } from '../pipes/filterChildProperties.pipe';

describe('dynamic-property component', () => {
    let fixture: ComponentFixture<DynamicPropertyComponent>;
    let propertiesUtilsMock: Partial<PropertiesUtils>;
    let dataTypeServiceMock: Partial<DataTypeService>;

    beforeEach(() => {
        propertiesUtilsMock = { createListOrMapChildren: jest.fn().mockReturnValue([]) };
        dataTypeServiceMock = { getConstraintsByParentTypeAndUniqueID: jest.fn().mockReturnValue(null) };
        const configure: ConfigureFn = (testBed) => {
            testBed.configureTestingModule({
                declarations: [DynamicPropertyComponent, ContentAfterLastDotPipe, FilterChildPropertiesPipe],
                schemas: [NO_ERRORS_SCHEMA],
                providers: [
                    { provide: PropertiesUtils, useValue: propertiesUtilsMock },
                    { provide: DataTypeService, useValue: dataTypeServiceMock }
                ]
            });
        };
        configureTests(configure).then((testBed) => {
            fixture = testBed.createComponent(DynamicPropertyComponent);
        });
    });

    it('getPropertyTestsId joins root name with parent names', () => {
        const rootProp = new PropertyFEModel({ name: 'root', type: 'string' } as PropertyBEModel);
        const cmp = fixture.componentInstance;
        cmp.property = rootProp;
        cmp.rootProperty = rootProp;
        cmp.ngOnInit();
        expect(cmp.getPropertyTestsId()).toContain('root');
    });

    it('preventInsertItem returns true for a MAP prop whose valueObj has an empty key', () => {
        const cmp = fixture.componentInstance;
        const prop: any = { type: PROPERTY_TYPES.MAP, valueObj: { '': null } };
        expect(cmp.preventInsertItem(prop)).toBe(true);
    });

    it('preventInsertItem returns false for a MAP prop with only non-empty keys', () => {
        const cmp = fixture.componentInstance;
        const prop: any = { type: PROPERTY_TYPES.MAP, valueObj: { k: '1' } };
        expect(cmp.preventInsertItem(prop)).toBe(false);
    });

    it('onElementChanged clears checkboxDisabled when value emptied and emits', () => {
        const cmp = fixture.componentInstance;
        const rootProp = new PropertyFEModel({ name: 'p', type: 'string' } as PropertyBEModel);
        cmp.property = rootProp;
        cmp.rootProperty = rootProp;
        cmp.ngOnInit();
        spyOn(cmp.emitter, 'emit');
        cmp.onElementChanged({ value: '', isValid: true } as any);
        expect(cmp.checkboxDisabled).toBe(false);
        expect(cmp.emitter.emit).toHaveBeenCalled();
    });
});
