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
import {of} from 'rxjs';
import {PropertyBEModel} from 'app/models/properties-inputs/property-be-model';
import {ConstraintOperatorType} from 'app/utils/filter-constraint-helper';
import {FilterType, ServiceDependenciesEditorComponent} from './service-dependencies-editor.component';

describe('ServiceDependenciesEditorComponent operator', () => {

    const property = (name: string, type: string): PropertyBEModel =>
        new PropertyBEModel(<any>{name, type, uniqueId: name});

    const createEditor = (filterConstraint?: any): ServiceDependenciesEditorComponent => {
        const topologyTemplateService: any = {getDefaultCustomFunction: () => of([])};
        const editor = new ServiceDependenciesEditorComponent(<any>{initValueObjectRef: jest.fn()}, <any>{}, topologyTemplateService);
        editor.filterType = FilterType.PROPERTY;
        editor.selectedInstanceProperties = [property('name', 'string'), property('count', 'integer')];
        editor.parentServiceProperties = [];
        editor.parentServiceInputs = [];
        editor.filterConstraint = filterConstraint;
        editor.ngOnInit();
        return editor;
    };

    it('shows the placeholder until a property is selected', () => {
        const editor = createEditor();

        expect(editor.currentRule.constraintOperator).toBeUndefined();
        expect(editor.operatorTypes.map(operator => operator.label)).toEqual(['Select a Property']);
    });

    it('defaults to equal once a property is selected', () => {
        const editor = createEditor();

        editor.currentRule.servicePropertyName = 'name';
        editor.onPropertyChange();

        expect(editor.currentRule.constraintOperator).toBe(ConstraintOperatorType.EQUAL);
        expect(editor.operatorTypes.some(operator => operator.value === ConstraintOperatorType.EQUAL)).toBe(true);
    });

    it('keeps the operator of an edited rule', () => {
        const editor = createEditor({
            servicePropertyName: 'count',
            constraintOperator: ConstraintOperatorType.GREATER_THAN,
            sourceType: 'static',
            sourceName: 'static',
            value: 3
        });

        expect(editor.currentRule.constraintOperator).toBe(ConstraintOperatorType.GREATER_THAN);
    });
});
