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

import { PropertiesUtils } from './properties.utils';
import { DataTypeService } from 'app/ng2/services/data-type.service';
import { PropertiesService } from 'app/ng2/services/properties.service';
import { PropertyBEModel, PropertyFEModel, DerivedPropertyType } from 'app/models';

describe('PropertiesUtils characterization (engine used by dynamic-property)', () => {
    let utils: PropertiesUtils;
    let dataTypeServiceMock: Partial<DataTypeService>;
    let propertiesServiceMock: Partial<PropertiesService>;

    beforeEach(() => {
        // Registry returns a truthy data-type for simple 'map'/'list'/'string' so props are kept.
        dataTypeServiceMock = {
            getDataTypeByModelAndTypeName: jest.fn().mockReturnValue({ name: 'string' }),
            getDataTypeByTypeName: jest.fn().mockReturnValue({ name: 'string', properties: [] }),
            getDerivedDataTypeProperties: jest.fn().mockReturnValue(undefined),
            checkForCustomBehavior: jest.fn()
        };
        propertiesServiceMock = {
            disableRelatedProperties: jest.fn(),
            undoDisableRelatedProperties: jest.fn()
        };
        utils = new PropertiesUtils(dataTypeServiceMock as DataTypeService, propertiesServiceMock as PropertiesService);
    });

    it('builds flattened children for a map<string,string> property from its value', () => {
        const beProp = new PropertyBEModel({
            name: 'myMap',
            type: 'map',
            schema: { property: { type: 'string', isSimpleType: true } },
            value: JSON.stringify({ a: '1', b: '2' })
        } as any);

        const feProp: PropertyFEModel = utils.convertAddPropertyBAToPropertyFE(beProp);

        // OBSERVE (delete after locking): console.log(JSON.stringify(feProp.flattenedChildren.map(c => ({ n: c.propertiesName, k: (c as any).mapKey, v: c.valueObj }))));
        expect(feProp.derivedDataType).toBe(DerivedPropertyType.MAP);
        expect(feProp.flattenedChildren.length).toBe(2);
        expect(feProp.flattenedChildren.map(c => (c as any).mapKey).sort()).toEqual(['a', 'b']);
        expect(feProp.flattenedChildren.map(c => c.valueObj).sort()).toEqual(['1', '2']);
    });

    it('builds flattened children for a list<string> property from its value', () => {
        const beProp = new PropertyBEModel({
            name: 'myList',
            type: 'list',
            schema: { property: { type: 'string', isSimpleType: true } },
            value: JSON.stringify(['x', 'y', 'z'])
        } as any);

        const feProp: PropertyFEModel = utils.convertAddPropertyBAToPropertyFE(beProp);

        expect(feProp.derivedDataType).toBe(DerivedPropertyType.LIST);
        expect(feProp.flattenedChildren.length).toBe(3);
        expect(feProp.flattenedChildren.map(c => c.valueObj)).toEqual(['x', 'y', 'z']);
    });

    it('builds flattened children for a map<string,map<string,string>> nested map from its value', () => {
        const beProp = new PropertyBEModel({
            name: 'myNestedMap',
            type: 'map',
            schemaType: 'map',
            schema: { property: { type: 'map', isSimpleType: false } },
            value: JSON.stringify({ outer: { inner: 'v' } })
        } as any);

        const feProp: PropertyFEModel = utils.convertAddPropertyBAToPropertyFE(beProp);

        expect(feProp.derivedDataType).toBe(DerivedPropertyType.MAP);
        // The nested-map branch (properties.utils.ts:163-168) fires when schemaType == 'map',
        // creating one child per outer key and then one per inner key.
        // Real output locked: outer key + inner key = 2 children total.
        expect(feProp.flattenedChildren.length).toBe(2);
        // propertiesName contains UUIDs — assert only on the stable mapKey values.
        expect(feProp.flattenedChildren.map(c => (c as any).mapKey)).toEqual(['outer', 'inner']);
        // outer child carries the nested object; inner child carries the leaf value.
        expect(feProp.flattenedChildren[0].valueObj).toEqual({ inner: 'v' });
        expect(feProp.flattenedChildren[1].valueObj).toBe('v');
    });
});
