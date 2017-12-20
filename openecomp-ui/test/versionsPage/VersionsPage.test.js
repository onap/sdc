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
import TestUtils from 'react-addons-test-utils';
import deepFreeze from 'deep-freeze';
import mockRest from 'test-utils/MockRest.js';
import Configuration from 'sdc-app/config/Configuration.js';
import {storeCreator} from 'sdc-app/AppStore.js';

import {mapStateToProps, mapActionsToProps} from 'sdc-app/onboarding/versionsPage/VersionsPage.js';
import VersionsPageView from 'sdc-app/onboarding/versionsPage/VersionsPage.jsx';
import VersionsPageActionHelper from 'sdc-app/onboarding/versionsPage/VersionsPageActionHelper.js';
import VersionsPageCreationActionHelper from 'sdc-app/onboarding/versionsPage/creation/VersionsPageCreationActionHelper.js';
import {itemTypes as versionPageItemTypes} from 'sdc-app/onboarding/versionsPage/VersionsPageConstants.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import {VersionsPageCreationFactory} from 'test-utils/factories/versionsPage/VersionsPageCreationFactories.js';
import {VersionsPageAdditionalPropsFactory} from 'test-utils/factories/versionsPage/VersionsPageFactories.js';

describe('Versions Page Module Tests', () => {

	const state = {
		currentScreen: {
			itemPermission: {
				isCollaborator: true
			},
			props: {
				itemId: '23'
			}
		},
		users: {
			userInfo: 'user123'
		},
		versionsPage: {
			permissions: {},
			versionsList: {versions: []},
			versionCreation: {}
		}
	};

	it ('mapStateToProps mapper exists', () => {

		expect(mapStateToProps).toBeTruthy();

	});

	it ('should have state in props', () => {

		const props = mapStateToProps(state);
		expect(props.currentUser).toEqual('user123');

	});

	it('simple jsx test', () => {

		const store = storeCreator();
		const dispatch = store.dispatch;

		const additionalProps = VersionsPageAdditionalPropsFactory.build();

		const props = Object.assign({}, mapStateToProps(state), mapActionsToProps(dispatch, additionalProps));
		const renderer = TestUtils.createRenderer();
		renderer.render(<VersionsPageView {...props} />);

		const renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

	});

});

describe('Versions Page Actions', () => {

	let store, dispatch, restPrefix = '', version;

	beforeAll(() => {
		restPrefix = Configuration.get('restPrefix');
		store = storeCreator();
		dispatch = store.dispatch;
		deepFreeze(store.getState());
		version = VersionFactory.build();
	});

	it('Select and deselect version', () => {

		let selectedVersionOf = (state) => state.versionsPage.versionsList.selectedVersion;
		VersionsPageActionHelper.selectVersion(dispatch, {version});
		expect(selectedVersionOf(store.getState())).toEqual(version.id);
		VersionsPageActionHelper.selectNone(dispatch);
		expect(selectedVersionOf(store.getState())).toEqual(null);

	});

	it('Create version', () => {

		const {id, baseId} = version;

		mockRest.addHandler('post', ({baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/items/${id}/versions/${baseId}/`);
			return version;
		});

		mockRest.addHandler('fetch', ({baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/items/${id}/versions`);
			return {results: [version]};
		});

		return VersionsPageCreationActionHelper.createVersion(dispatch, {
			itemId: id,
			baseVersion: {id: baseId},
			payload: VersionsPageCreationFactory.build()
		})
		.then(result => {
			expect(result.id).toEqual(id);
		});

	});

	it('Fetch versions', () => {

		const {id} = version;

		mockRest.addHandler('fetch', ({baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/items/${id}/versions`);
			return {results: [version]};
		});

		return VersionsPageActionHelper.fetchVersions(dispatch, {
			itemType: versionPageItemTypes.SOFTWARE_PRODUCT,
			itemId: id
		})
		.then(() => {
			const {versionsPage: {versionsList}} = store.getState();
			expect(versionsList.versions[0].id).toEqual(id);
			expect(versionsList.itemId).toEqual(id);
			expect(versionsList.itemType).toEqual(versionPageItemTypes.SOFTWARE_PRODUCT);
		});

	});

});
