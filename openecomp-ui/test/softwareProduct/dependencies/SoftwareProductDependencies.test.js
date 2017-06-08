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
import VersionControllerUtilsFactory from 'test-utils/factories/softwareProduct/VersionControllerUtilsFactory.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import {cloneAndSet} from 'test-utils/Util.js';
import mockRest from 'test-utils/MockRest.js';

import SoftwareProductComponentsActionHelper from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsActionHelper.js';
import SoftwareProductDependenciesActionHelper from 'sdc-app/onboarding/softwareProduct/dependencies/SoftwareProductDependenciesActionHelper.js';
import SoftwareProductDependenciesView from 'sdc-app/onboarding/softwareProduct/dependencies/SoftwareProductDependenciesView.jsx';
import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';

describe('Software Product Dependencies Module Tests', function () {
	const softwareProductId = '555';
	const version = VersionControllerUtilsFactory.build().version;

	it('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});
	
	it('Get Software Product Dependencies List', () => {
		const store = storeCreator();
		const dispatch = store.dispatch;

		let DependenciesListResponse = SoftwareProductDependenciesResponseFactory.buildList(2);
		let DependenciesListStore = DependenciesListResponse.map(dependency => SoftwareProductDependenciesStoreFactory.build(dependency));
		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductDependencies', DependenciesListStore);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependency-model`);
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

	it('Update Software Product Dependencies List', () => {
		const store = storeCreator();
		const dispatch = store.dispatch;

		let DependenciesListResponse = SoftwareProductDependenciesResponseFactory.buildList(3);
		let DependenciesListStore = DependenciesListResponse.map(dependency => SoftwareProductDependenciesStoreFactory.build(dependency));
		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductDependencies', DependenciesListStore);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependency-model`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: DependenciesListResponse};
		});

		return SoftwareProductDependenciesActionHelper.fetchDependencies(dispatch, {softwareProductId, version}).then(() => {
			
			const state = store.getState();
			const depndenciesWithGeneratedId = state.softwareProduct.softwareProductDependencies;
			const currentDependencies = expectedStore.softwareProduct.softwareProductDependencies;
			let expectedStoreDependencies = currentDependencies.map((dependency, index) => ({...dependency, id: depndenciesWithGeneratedId[index].id}));
			
			let newDependency = SoftwareProductDependenciesStoreFactory.build();
			expectedStoreDependencies.push(newDependency);

			const newExpectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductDependencies', expectedStoreDependencies);

			SoftwareProductDependenciesActionHelper.updateDependencyList(dispatch, {dependenciesList: expectedStoreDependencies});
			const newState = store.getState();
			expect(newState).toEqual(newExpectedStore);
		});
	});

	it('Add And Save Software Product Dependencies List', () => {
		const store = storeCreator();
		const dispatch = store.dispatch;

		let mockServerDependencies = [];

		SoftwareProductDependenciesActionHelper.addDependency(dispatch);
		let state = store.getState();
		let dependencies = state.softwareProduct.softwareProductDependencies;
		expect(dependencies.length).toEqual(1);
		expect(dependencies[0].sourceId).toEqual(null);
		expect(dependencies[0].targetId).toEqual(null);

		let newDependencies = SoftwareProductDependenciesStoreFactory.buildList(1);
		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductDependencies', newDependencies);
		SoftwareProductDependenciesActionHelper.updateDependencyList(dispatch, {dependenciesList: newDependencies});

		mockRest.addHandler('post', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependency-model`);
			expect(data).toEqual({componentDependencyModels: newDependencies.map(item => ({sourceId: item.sourceId, targetId: item.targetId, relationType: item.relationType}) )});
			expect(options).toEqual(undefined);
			mockServerDependencies = [...data.componentDependencyModels];
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependency-model`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: mockServerDependencies};
		});

		return SoftwareProductDependenciesActionHelper.saveDependencies(dispatch, {softwareProductId, version, dependenciesList: newDependencies}).then(() => {
			return SoftwareProductDependenciesActionHelper.fetchDependencies(dispatch, {softwareProductId, version});
		}).then(() => {
			const state = store.getState();
			const depndenciesWithGeneratedId = state.softwareProduct.softwareProductDependencies;
			const currentDependencies = expectedStore.softwareProduct.softwareProductDependencies;
			let expectedStoreDependencies = currentDependencies.map((dependency, index) => ({...dependency, id: depndenciesWithGeneratedId[index].id}));

			const newExpectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductDependencies', expectedStoreDependencies);

			expect(state).toEqual(newExpectedStore);
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
		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductDependencies', DependenciesListStore);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/component-dependency-model`);
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