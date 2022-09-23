/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

import { TestBed } from "@angular/core/testing";
import { EntityFilterPipe } from './entity-filter.pipe';
import { IEntityFilterObject } from './entity-filter.pipe';
import {Component, DataTypeModel} from "app/models";
import { ISearchFilter } from './entity-filter.pipe';

describe('EntityFilterPipe', () => {
    let entityFilterPipe: EntityFilterPipe;
    let entityFilterMock: Partial<IEntityFilterObject>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [],
            providers: [EntityFilterPipe],
        });
        entityFilterPipe = TestBed.get(EntityFilterPipe);
    });

    it('EntityFilterPipe should be created', () => {
        expect(entityFilterPipe).toBeTruthy();
    });

    it('Transform method should filter objects by type matching with selectedComponentTypes', () => {
        let componentList: Array<Component | DataTypeModel> = [];

        const mockComponent1 = {
            componentType: 'SERVICE',
            isResource: jest.fn().mockImplementation(() => false),
        } as Partial<Component> as Component;
        componentList.push(mockComponent1);

        const mockComponent2 = {
            componentType: 'RESOURCE',
            isResource: jest.fn().mockImplementation(() => true),
            getComponentSubType: jest.fn().mockImplementation(() => 'subComponent')
        } as Partial<Component> as Component;
        componentList.push(mockComponent2);

        const mockComponent3 = {
            componentType: 'TOSCA_TYPE',
            isDataType: jest.fn().mockImplementation(() => true),
            getSubToscaType: jest.fn().mockImplementation(() => 'toscaSubComponent')
        } as Partial<DataTypeModel> as DataTypeModel;
        componentList.push(mockComponent3);

        entityFilterMock = {
            selectedComponentTypes: ['Service', 'RESOURCE'],
            selectedResourceSubTypes: ['subComponent'],
            selectedToscaSubTypes: ['toscaSubComponent']
        };
        let response: Array<Component | DataTypeModel> = entityFilterPipe.transform(componentList, entityFilterMock);
        expect(response).toHaveLength(3);
        expect(response[0]).toEqual(mockComponent1);
        expect(response[1]).toEqual(mockComponent2);
        expect(response[2]).toEqual(mockComponent3);
    });

    it('Transform method should filter objects by categories & subcategories matching with selectedCategoriesModel', () => {
        let componentList: Array<Component> = [];
        const mockComponent = {
            componentType: 'newtesttype',
            categoryNormalizedName: 'categoryname',
            subCategoryNormalizedName: 'subcategoryname',
        } as Partial<Component> as Component;
        componentList.push(mockComponent);

        const mockComponent1 = {
            componentType: 'newtesttype',
            categoryNormalizedName: 'name',
            subCategoryNormalizedName: 'subname',
        } as Partial<Component> as Component;
        componentList.push(mockComponent1);

        const mockComponent2 = {
            componentType: 'RESOURCE',
            categoryNormalizedName: 'name'
        } as Partial<Component> as Component;
        componentList.push(mockComponent2);

        const mockComponent3 = {
            componentType: 'SERVICE',
            categoryNormalizedName: 'name'
        } as Partial<Component> as Component;
        componentList.push(mockComponent3);

        entityFilterMock = {
            selectedCategoriesModel: ['categoryname.subcategoryname', 'resourceNewCategory.name', 'serviceNewCategory.name']
        };
        let response: Array<Component | DataTypeModel> = entityFilterPipe.transform(componentList, entityFilterMock);
        expect(response).toHaveLength(3);
        expect(response[0]).toEqual(mockComponent);
        expect(response[1]).toEqual(mockComponent2);
        expect(response[2]).toEqual(mockComponent3);
    });

    it('Transform method should filter objects by statuses matching with selectedStatuses', () => {
        let componentList: Array<Component> = [];
        const mockComponent = {
            lifecycleState: 'lifecyclestatus'
        } as Partial<Component> as Component;
        componentList.push(mockComponent);

        const mockComponent1 = {
            lifecycleState: 'lifecycleteststatus'
        } as Partial<Component> as Component;
        componentList.push(mockComponent1);

        const mockComponent2 = {
            lifecycleState: 'CERTIFIED',
            distributionStatus: 'DISTRIBUTED'
        } as Partial<Component> as Component;
        componentList.push(mockComponent2);

        entityFilterMock = {
            selectedStatuses: ['lifecyclestatus', 'DISTRIBUTED']
        };
        let response: Array<Component | DataTypeModel> = entityFilterPipe.transform(componentList, entityFilterMock);
        expect(response).toHaveLength(2);
        expect(response[0]).toEqual(mockComponent);
        expect(response[1]).toEqual(mockComponent2);
    });

    it('Transform method should filter objects by statuses and distributed matching with selected distributed', () => {
        let componentList: Array<Component> = [];
        const mockComponent = {
            distributionStatus: 'diststatus'
        } as Partial<Component> as Component;
        componentList.push(mockComponent);

        const mockComponent1 = {
            distributionStatus: 'testdiststatus'
        } as Partial<Component> as Component;
        componentList.push(mockComponent1);

        entityFilterMock = {
            distributed: ['diststatus', 'localstatus']
        };
        let response: Array<Component | DataTypeModel> = entityFilterPipe.transform(componentList, entityFilterMock);
        expect(response).toHaveLength(1);
        expect(response[0]).toEqual(mockComponent);
    });

    it('Transform method should filter objects by model matching with selectedModels', () => {
        let componentList: Array<Component> = [];
        const mockComponent = {
            model: 'testModel'
        } as Partial<Component> as Component;
        componentList.push(mockComponent);

        const mockComponent1 = {
            model: 'testModelNegative'
        } as Partial<Component> as Component;
        componentList.push(mockComponent1);

        const mockComponent2 = {
            distributionStatus: 'testdiststatus'
        } as Partial<Component> as Component;
        componentList.push(mockComponent2);

        entityFilterMock = {
            selectedModels: ['testModel', 'localTest', 'SDC AID']
        };
        let response: Array<Component | DataTypeModel> = entityFilterPipe.transform(componentList, entityFilterMock);
        expect(response).toHaveLength(2);
        expect(response[0]).toEqual(mockComponent);
        expect(response[1]).toEqual(mockComponent2);
    });

    it('Transform method should filter objects by custom search matching with given keys', () => {
        let componentList: Array<Component> = [];
        const mockComponent = {
            distributionStatus: 'distributionStatus',
            model: 'testModel'
        } as Partial<Component> as Component;
        componentList.push(mockComponent);

        const mockComponent1 = {
            distributionStatus: 'testDiststatus',
            model: 'mockModel'
        } as Partial<Component> as Component;
        componentList.push(mockComponent1);

        const searchFilter: ISearchFilter = {
            distributionStatus: 'distributionStatus'
        }
        entityFilterMock = {
            search: searchFilter
        };
        let response: Array<Component | DataTypeModel> = entityFilterPipe.transform(componentList, entityFilterMock);
        expect(response).toHaveLength(1);
        expect(response[0]).toEqual(mockComponent);
    });

});
