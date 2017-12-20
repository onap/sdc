/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import React from 'react';
import {mount} from 'enzyme';
import {mapStateToProps} from 'sdc-app/onboarding/softwareProduct/dependencies/SoftwareProductDependencies.js';
import {
	SoftwareProductDependenciesResponseFactory,
	SoftwareProductDependenciesStoreFactory} from 'test-utils/factories/softwareProduct/SoftwareProductDependenciesFactories.js';
import {VSPComponentsFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsFactories.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import {cloneAndSet} from 'test-utils/Util.js';
import mockRest from 'test-utils/MockRest.js';

import SoftwareProductComponentsActionHelper from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsActionHelper.js';
import {relationTypes, NEW_RULE_TEMP_ID}  from 'sdc-app/onboarding/softwareProduct/dependencies/SoftwareProductDependenciesConstants.js';
import SoftwareProductDependenciesActionHelper from 'sdc-app/onboarding/softwareProduct/dependencies/SoftwareProductDependenciesActionHelper.js';
import SoftwareProductDependenciesView from 'sdc-app/onboarding/softwareProduct/dependencies/SoftwareProductDependenciesView.jsx';
import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';

function addNewRowElement(arr, data) {
	if (data === undefined) {
		arr.push({id: NEW_RULE_TEMP_ID, targetId: null, sourceId: null, relationType: relationTypes.DEPENDS_ON});
	} else {
		arr.push(data);
	}
}

describe('Software Product Dependencies Module Tests', function () {
	const softwareProductId = '555';
	const version = VersionFactory.build();


	it('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('Get Software Product Dependencies List', () => {
		const store = storeCreator();
		const dispatch = store.dispatch;

		let DependenciesListResponse = SoftwareProductDependenciesResponseFactory.buildList(2);
		let DependenciesListStore = DependenciesListResponse.map(dependency => SoftwareProductDependenciesStoreFactory.build(dependency));
		addNewRowElement(DependenciesListStore);
		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductDependencies', DependenciesListStore);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependencies`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: DependenciesListResponse};
		});

		return SoftwareProductDependenciesActionHelper.fetchDependencies(dispatch, {softwareProductId, version}).then(() => {
			const state = store.getState();
			const depndenciesWithGeneratedId = state.softwareProduct.softwareProductDependencies;
			const currentDependencies = expectedStore.softwareProduct.softwareProductDependencies;
			let expectedStoreDependencies = currentDependencies.map((dependency, index) => ({...dependency, id: depndenciesWithGeneratedId[index].id}));
			const newExpectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductDependencies', expectedStoreDependencies);
			expect(state).toEqual(newExpectedStore);
		});
	});

	/*
	Test update:
	- fetch initial item
	- update existing item
	- auto fetch again
	 */
	it('Update Software Product Dependency', () => {
		const store = storeCreator();
		const dispatch = store.dispatch;

		let DependenciesListResponse = SoftwareProductDependenciesResponseFactory.buildList(1);
		let DependenciesListStore = DependenciesListResponse.map(dependency => SoftwareProductDependenciesStoreFactory.build(dependency));
		addNewRowElement(DependenciesListStore);
		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductDependencies', DependenciesListStore);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependencies`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: DependenciesListResponse};
		});
		return SoftwareProductDependenciesActionHelper.fetchDependencies(dispatch, {softwareProductId, version}).then(() => {

			const state = store.getState();
			const depndenciesWithGeneratedId = state.softwareProduct.softwareProductDependencies;
			const currentDependencies = expectedStore.softwareProduct.softwareProductDependencies;
			let expectedStoreDependencies = currentDependencies.map((dependency, index) => ({...dependency, id: depndenciesWithGeneratedId[index].id}));
			let item = expectedStoreDependencies.find((dep) => dep.id !== NEW_RULE_TEMP_ID);
			item.targetId = 'testChangeTarget';
			item.sourceId = 'testChangesource';
			// removing 'new row' from response
			expectedStoreDependencies = expectedStoreDependencies.slice(0, expectedStoreDependencies.length - 1);
			let expDependenciesListStore = expectedStoreDependencies.map(dependency => SoftwareProductDependenciesStoreFactory.build(dependency));

			mockRest.addHandler('put', ({data, options, baseUrl}) => {
				expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependencies/${item.id}`);
				expect(data.targetId).toEqual('testChangeTarget');
				expect(data.sourceId).toEqual('testChangesource');
				expect(options).toEqual(undefined);
				return {results: null};
			});
			mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
				expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependencies`);
				expect(data).toEqual(undefined);
				expect(options).toEqual(undefined);
				return {results: expDependenciesListStore};
			});

			addNewRowElement(expectedStoreDependencies);
			const newExpectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductDependencies', expectedStoreDependencies);
			return SoftwareProductDependenciesActionHelper.updateDependency(dispatch, {softwareProductId, version, item}).then(() => {
				const newState = store.getState();
				expect(newState).toEqual(newExpectedStore);

			});
		});
	});

	/*
	- Fetch item list
	- Delete item from list
	- Fetch again
	 */
	it('Delete Software Product Dependency', () => {
		const store = storeCreator();
		const dispatch = store.dispatch;
		let DependenciesListResponse = SoftwareProductDependenciesResponseFactory.buildList(1);
		let DependenciesListStore = DependenciesListResponse.map(dependency => SoftwareProductDependenciesStoreFactory.build(dependency));
		addNewRowElement(DependenciesListStore);
		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductDependencies', DependenciesListStore);

		let deleteItem = DependenciesListStore.find((dep) => dep.id !== NEW_RULE_TEMP_ID);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependencies`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: DependenciesListResponse};
		});
		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependencies/${deleteItem.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: null};
		});
		return SoftwareProductDependenciesActionHelper.fetchDependencies(dispatch, {softwareProductId, version}).then(() => {
			const state = store.getState();
			const depndenciesWithGeneratedId = state.softwareProduct.softwareProductDependencies;
			const currentDependencies = expectedStore.softwareProduct.softwareProductDependencies;
			let expectedStoreDependencies = currentDependencies.map((dependency, index) => ({...dependency, id: depndenciesWithGeneratedId[index].id}))

			const newExpectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductDependencies', expectedStoreDependencies);
			expect(state).toEqual(newExpectedStore);

			expectedStoreDependencies = expectedStoreDependencies.filter((dep) => dep.id !== deleteItem.id);
			const postDeleteExpectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductDependencies', expectedStoreDependencies);

			mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
				expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependencies`);
				expect(data).toEqual(undefined);
				expect(options).toEqual(undefined);
				return {results: []};
			});

			return SoftwareProductDependenciesActionHelper.removeDependency(dispatch, {softwareProductId, version, item: deleteItem}).then(() => {
				const state = store.getState();
				const depndenciesWithGeneratedId = state.softwareProduct.softwareProductDependencies;
				const currentDependencies = postDeleteExpectedStore.softwareProduct.softwareProductDependencies;
				expect(depndenciesWithGeneratedId).toEqual(currentDependencies);
			});
		});
	});

	/*
	- Create initial list
	- Update the new row and make sure there is no API call
	- Submit the new row
	- Getch data with reset new row and new entity with info from the new item
	 */

	it('Create Software Product Dependency', () => {
		const store = storeCreator();
		const dispatch = store.dispatch;

		let DependenciesListResponse = SoftwareProductDependenciesResponseFactory.buildList(1);
		let DependenciesListStore = DependenciesListResponse.map(dependency => SoftwareProductDependenciesStoreFactory.build(dependency));
		addNewRowElement(DependenciesListStore);
		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductDependencies', DependenciesListStore);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependencies`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: DependenciesListResponse};
		});
		return SoftwareProductDependenciesActionHelper.fetchDependencies(dispatch, {softwareProductId, version}).then(() => {
			// setting properties on the 'new role' should not call an API
			let addItem = {id: NEW_RULE_TEMP_ID, sourceId: 'sId', targetId : 'tId',relationType: relationTypes.DEPENDS_ON};
			try {
				SoftwareProductDependenciesActionHelper.updateDependency(dispatch, {softwareProductId, version, item: addItem}).then(()=> {
					//go to error that fetch was not defined
				});
			} catch (error) {
				if(error.name === 'TypeError') {
					// Expected error because we expected there is no promise
				} else {
					fail('Error:' + error);
				}
			}

			const state = store.getState();
			const depndenciesWithGeneratedId = state.softwareProduct.softwareProductDependencies;
			const currentDependencies = expectedStore.softwareProduct.softwareProductDependencies;
			let expectedStoreDependencies = currentDependencies.map((dependency, index) => ({...dependency, id: depndenciesWithGeneratedId[index].id}));
			// creating the new item
			let item = SoftwareProductDependenciesResponseFactory.buildList(1,
				{sourceId: 'sId', targetId : 'tId',relationType: relationTypes.DEPENDS_ON})[0];
			addNewRowElement(expectedStoreDependencies, item);
			expectedStoreDependencies = expectedStoreDependencies.filter((dep) => dep.id !== NEW_RULE_TEMP_ID);

			mockRest.addHandler('post', ({data, options, baseUrl}) => {
				expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependencies`);
				expect(data.targetId).toEqual('tId');
				expect(data.sourceId).toEqual('sId');
				expect(data.id).toEqual(undefined);
				expect(options).toEqual(undefined);
				return {results: item.id};
			});
			mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
				expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependencies`);
				expect(data).toEqual(undefined);
				expect(options).toEqual(undefined);
				return {results: expectedStoreDependencies};
			});

			let PostCreateItemListStore = expectedStoreDependencies.map(dependency => SoftwareProductDependenciesStoreFactory.build(dependency));
			addNewRowElement(PostCreateItemListStore);
			const newExpectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductDependencies', PostCreateItemListStore);

			return SoftwareProductDependenciesActionHelper.createDependency(dispatch, {softwareProductId, version, item: addItem}).then(() => {
				const newState = store.getState();
				expect(newState.softwareProduct.softwareProductDependencies.length).toEqual(3);
				expect(newState).toEqual(newExpectedStore);
			});

		});

	});


	it('Get Software Product Dependencies List with loop, and render to JSX', () => {
		const store = storeCreator();
		const dispatch = store.dispatch;

		let components = VSPComponentsFactory.buildList(2);
		let vspEditor = VSPEditorFactory.build({id: softwareProductId, version});

		let DependenciesListResponse = SoftwareProductDependenciesResponseFactory.buildList(2);
		let firstDependecy = DependenciesListResponse[0];
		let secondDependency = DependenciesListResponse[1];
		firstDependecy.sourceId = components[0].id;
		secondDependency.sourceId = components[1].id;
		firstDependecy.targetId = secondDependency.sourceId;
		secondDependency.targetId = firstDependecy.sourceId;

		let DependenciesListStore = DependenciesListResponse.map(dependency => SoftwareProductDependenciesStoreFactory.build({...dependency, hasCycle: true}));
		addNewRowElement(DependenciesListStore);
		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductDependencies', DependenciesListStore);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependencies`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: DependenciesListResponse};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: components};
		});

		return SoftwareProductDependenciesActionHelper.fetchDependencies(dispatch, {softwareProductId, version}).then(() => {
			return SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(dispatch, {softwareProductId, version});
		}).then(() => {
			const state = store.getState();
			state.softwareProduct.softwareProductEditor = {data: vspEditor};
			const depndenciesWithGeneratedId = state.softwareProduct.softwareProductDependencies;
			const currentDependencies = expectedStore.softwareProduct.softwareProductDependencies;
			let expectedStoreDependencies = currentDependencies.map((dependency, index) => ({...dependency, id: depndenciesWithGeneratedId[index].id}));

			const newExpectedStore = {
				...expectedStore,
				softwareProduct: {
					...expectedStore.softwareProduct,
					softwareProductDependencies: expectedStoreDependencies,
					softwareProductEditor: {data: vspEditor},
					softwareProductComponents: {
						...expectedStore.softwareProduct.softwareProductComponents,
						componentsList: components
					}
				}
			};

			expect(state).toEqual(newExpectedStore);

			const props = mapStateToProps(state);
			expect(props.softwareProductDependencies).toEqual(expectedStoreDependencies);
			const wrapper = mount(<SoftwareProductDependenciesView {...props}/>);
			expect(wrapper).toBeTruthy();
		});
	});

});
