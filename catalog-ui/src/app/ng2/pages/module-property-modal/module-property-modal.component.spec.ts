/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2026 Deutsche Telekom AG
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
import {ModulePropertyModalComponent} from './module-property-modal.component';
import {UNIQUE_GROUP_PROPERTIES_NAME} from 'app/utils/constants';

describe('ModulePropertyModalComponent', () => {
    let comp: ModulePropertyModalComponent;
    let saveServiceMock: any;
    let validationUtilsMock: any;

    const build = (property: any, component: any, moduleProps: any[] = [property]) => {
        saveServiceMock = {save: jest.fn(() => ({subscribe: jest.fn()}))};
        // Integer values must be all digits; JSON must parse. Mirrors the ValidationUtils behaviour the modal relies on.
        validationUtilsMock = {
            getValidationPattern: jest.fn((type: string) => type === 'integer' ? /^\d+$/ : null),
            validateJson: jest.fn((v: string) => { try { JSON.parse(v); return true; } catch (e) { return false; } }),
            validateIntRange: jest.fn(() => true)
        };
        comp = new ModulePropertyModalComponent(saveServiceMock, validationUtilsMock);
        comp.input = {
            property,
            component,
            selectedModule: {properties: moduleProps} as any,
            filteredProperties: moduleProps
        } as any;
        comp.ngOnInit();
    };
    const resource = {isService: () => false, isResource: () => true};
    const service = {isService: () => true, isResource: () => false};

    it('marks isBase / vf_module_type / volume_group / vf_module_label readonly', () => {
        [UNIQUE_GROUP_PROPERTIES_NAME.IS_BASE, UNIQUE_GROUP_PROPERTIES_NAME.VF_MODULE_TYPE,
         UNIQUE_GROUP_PROPERTIES_NAME.VOLUME_GROUP, UNIQUE_GROUP_PROPERTIES_NAME.VF_MODULE_LABEL].forEach((name) => {
            build({name, type: 'string', value: 'x', uniqueId: name}, resource);
            expect(comp.property.readonly).toBe(true);
        });
    });

    it('vf_module_description is readonly for a service but editable for a resource', () => {
        build({name: UNIQUE_GROUP_PROPERTIES_NAME.VF_MODULE_DESCRIPTION, type: 'string', value: 'x', uniqueId: 'd'}, service);
        expect(comp.property.readonly).toBe(true);
        build({name: UNIQUE_GROUP_PROPERTIES_NAME.VF_MODULE_DESCRIPTION, type: 'string', value: 'x', uniqueId: 'd'}, resource);
        expect(comp.property.readonly).toBe(false);
    });

    it('onValueChange resets an emptied resource value to the property defaultValue', () => {
        build({name: 'plain', type: 'string', value: '', defaultValue: 'dv', uniqueId: 'p'},
              {isService: () => false, isResource: () => true, selectedInstance: {}});
        comp.property.value = '';
        comp.onValueChange();
        expect(comp.property.value).toBe('dv');
    });

    const N = UNIQUE_GROUP_PROPERTIES_NAME;
    const withSiblings = (min: string, max: string, init: string) => ([
        {name: N.MIN_VF_MODULE_INSTANCES, type: 'integer', value: min, uniqueId: 'min'},
        {name: N.MAX_VF_MODULE_INSTANCES, type: 'integer', value: max, uniqueId: 'max'},
        {name: N.INITIAL_COUNT, type: 'integer', value: init, uniqueId: 'init'}
    ]);

    it('flags maxValidation when min > initial (resource, no max)', () => {
        const props = withSiblings('5', '', '2');
        build(props[0], resource, props);   // editing MIN, value 5, initial 2, no max
        comp.property.value = '5';
        comp.onValueChange();
        expect(comp.errors['maxValidation']).toBe(true);
        expect(comp.isValid()).toBe(false);
    });

    it('passes min <= initial (resource, no max)', () => {
        const props = withSiblings('1', '', '2');
        build(props[0], resource, props);
        comp.property.value = '1';
        comp.onValueChange();
        expect(comp.errors['maxValidation']).toBeFalsy();
        expect(comp.isValid()).toBe(true);
    });

    it('flags minOrMaxValidation when initial is outside [min,max]', () => {
        const props = withSiblings('2', '4', '9');
        build(props[2], resource, props);   // editing INITIAL_COUNT = 9, min 2 max 4
        comp.property.value = '9';
        comp.onValueChange();
        expect(comp.errors['minOrMaxValidation']).toBe(true);
    });

    it('for a service, flags minValidationVfLevel when min < parentValue', () => {
        const props = withSiblings('1', '', '5');
        const svc = {isService: () => true, isResource: () => false, selectedInstance: {}};
        build({...props[0], parentValue: '3'}, svc, props);
        comp.property.value = '1';
        comp.property.parentValue = '3';
        comp.onValueChange();
        expect(comp.errors['minValidationVfLevel']).toBe(true);
    });

    it('flags a pattern error for a non-integer value on an integer property and blocks isValid', () => {
        build({name: 'plain', type: 'integer', value: '1', uniqueId: 'p'}, resource);
        comp.property.value = 'abc';
        comp.onValueChange();
        expect(comp.errors['pattern']).toBe(true);
        expect(comp.isValid()).toBe(false);
    });

    it('accepts a valid integer value (no pattern error)', () => {
        build({name: 'plain', type: 'integer', value: '1', uniqueId: 'p'}, resource);
        comp.property.value = '42';
        comp.onValueChange();
        expect(comp.errors['pattern']).toBeFalsy();
        expect(comp.isValid()).toBe(true);
    });

    it('does not run the pattern check for a boolean property (edited via select)', () => {
        build({name: 'plain', type: 'boolean', value: 'true', uniqueId: 'p'}, resource);
        comp.property.value = 'true';
        comp.onValueChange();
        expect(comp.errors['pattern']).toBeFalsy();
        expect(validationUtilsMock.getValidationPattern).not.toHaveBeenCalled();
    });

    it('save() skips the BE call for a read-only property', () => {
        build({name: N.IS_BASE, type: 'string', value: 'x', uniqueId: 'p'}, resource);   // IS_BASE -> readonly
        comp.save();
        expect(saveServiceMock.save).not.toHaveBeenCalled();
    });

    it('save() skips the BE call when the value is unchanged', () => {
        build({name: 'plain', type: 'string', value: 'same', uniqueId: 'p'}, resource);
        comp.save();   // property.value still 'same', equal to input.property.value
        expect(saveServiceMock.save).not.toHaveBeenCalled();
    });

    it('save() issues the BE call when an editable value actually changed', () => {
        build({name: 'plain', type: 'string', value: 'old', uniqueId: 'p'}, resource);
        comp.property.value = 'new';
        comp.save();
        expect(saveServiceMock.save).toHaveBeenCalled();
    });
});
