/*
 * Copyright (C) 2026 Deutsche Telekom AG.
 *
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
 */
import {PropertyBEModel} from 'app/models/properties-inputs/property-be-model';
import {States} from 'app/utils/constants';
import {SubstitutionFilterComponent} from './substitution-filter.component';

describe('SubstitutionFilterComponent without properties', () => {
    let navigationService: any;
    let component: SubstitutionFilterComponent;

    beforeEach(() => {
        navigationService = {navigate: jest.fn()};
        component = new SubstitutionFilterComponent(<any>{}, <any>{}, <any>{}, navigationService);
    });

    it('is not empty while the properties are still loading', () => {
        component.selectedInstanceProperties = undefined;

        expect(component.hasNoProperties()).toBe(false);
    });

    it('is empty when the component has no properties', () => {
        component.selectedInstanceProperties = [];

        expect(component.hasNoProperties()).toBe(true);
    });

    it('is not empty when the component has a property', () => {
        component.selectedInstanceProperties = [new PropertyBEModel(<any>{name: 'name', type: 'string'})];

        expect(component.hasNoProperties()).toBe(false);
    });

    it('opens the Properties Assignment tab', () => {
        component.goToPropertiesAssignment();

        expect(navigationService.navigate).toHaveBeenCalledWith(States.WORKSPACE_PROPERTIES_ASSIGNMENT);
    });
});
