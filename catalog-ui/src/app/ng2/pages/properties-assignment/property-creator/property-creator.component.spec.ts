/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2026 Deutsche Telekom AG.
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

import {DataTypeService} from 'app/ng2/services/data-type.service';
import {PropertyCreatorComponent} from './property-creator.component';
import {WorkspaceService} from '../../workspace/workspace.service';

describe('PropertyCreatorComponent', () => {

    const workspaceServiceMock = {metadata: {model: null}} as WorkspaceService;

    const componentWithDataTypes = (dataTypes: any): PropertyCreatorComponent => {
        const dataTypeServiceMock = {getDataTypeByModel: () => dataTypes} as any as DataTypeService;
        return new PropertyCreatorComponent(dataTypeServiceMock, workspaceServiceMock);
    };

    it('populates the type dropdown from the data-type cache', () => {
        const component = componentWithDataTypes({'org.openecomp.datatypes.heat.MyType': {}});

        const labels = component.typesProperties.map((dropdown) => dropdown.label);
        expect(labels).toContain('string');
        expect(labels).toContain('MyType');
    });

    // DataTypesService.getAllDataTypesFromModel fires its async cache load without awaiting it, so
    // getDataTypeByModel returns undefined until /dataTypes resolves. This component is a
    // constructor-injected provider of PropertiesAssignmentComponent, so a throw here escapes route
    // activation and the Angular router resetStateAndUrl()s — the Properties Assignment tab never
    // opens. Construction must therefore survive an unresolved cache.
    it('constructs with only the primitive types when the data-type cache is not yet loaded', () => {
        let component: PropertyCreatorComponent;
        expect(() => { component = componentWithDataTypes(undefined); }).not.toThrow();

        const labels = component.typesProperties.map((dropdown) => dropdown.label);
        expect(labels).toContain('string');
        expect(labels).toContain('Select Type...');
    });
});
