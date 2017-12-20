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
import {scryRenderedDOMComponentsWithTestId} from 'test-utils/Util.js';
import deepFreeze from 'deep-freeze';
import mockRest from 'test-utils/MockRest.js';
import {cloneAndSet} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';

import {actionsEnum as vcActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import Configuration from 'sdc-app/config/Configuration.js';
import {mapStateToProps, mapActionsToProps} from 'sdc-app/onboarding/revisions/Revisions.js';
import RevisionsView from 'sdc-app/onboarding/revisions/RevisionsView.jsx';
import RevisionsActionHelper from 'sdc-app/onboarding/revisions/RevisionsActionHelper.js';
import {RevisionsPagePropsFactory} from 'test-utils/factories/revisions/RevisionsFactories.js';
import {UserFactory} from 'test-utils/factories/users/UsersFactories.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import {screenTypes} from 'sdc-app/onboarding/OnboardingConstants.js';
import ReactTestUtils from 'react-addons-test-utils';
import {enums} from 'sdc-app/onboarding/OnboardingConstants.js';

const state = {};
state.revisions = RevisionsPagePropsFactory.buildList(2);
state.users = {usersList : UserFactory.buildList(2)};
state.revisions[0].user = state.users.usersList[0].userId;
state.revisions[1].user = state.users.usersList[1].userId;


describe('Revision List Tests', () => {
	/*
	it ('mapStateToProps mapper exists', () => {

		expect(mapStateToProps).toBeTruthy();

	});

	it ('should have state in props', () => {

		const props = mapStateToProps(state);
		expect(props.revisions.length).toEqual(2);

	});

	it('simple jsx test', () => {

		const store = storeCreator();
		const dispatch = store.dispatch;

		const props = Object.assign({}, mapStateToProps(state), mapActionsToProps(dispatch, {}));
		const renderer = TestUtils.createRenderer();
		renderer.render(<RevisionsView {...props} />);

		const renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

	});

	it('get list data', () => {

		const store = storeCreator();
		const dispatch = store.dispatch;

		const props = Object.assign({}, mapStateToProps(state), mapActionsToProps(dispatch, {}));

		let revisionsView = TestUtils.renderIntoDocument(
			<RevisionsView {...props} />
		);
		let list = scryRenderedDOMComponentsWithTestId(revisionsView,'revision-list-item');
		expect(list.length).toEqual(props.revisions.length);
		let revert = scryRenderedDOMComponentsWithTestId(revisionsView,'form-submit-button');
		expect(revert[0].innerHTML).toEqual('Revert');
		let date = scryRenderedDOMComponentsWithTestId(revisionsView,'revision-date');
		expect(date.length).toEqual(props.revisions.length);
		expect(date[0].children[0].className).toEqual('revision-date');
		expect(date[0].children[1].className).toEqual('revision-time');
		let user = ReactTestUtils.scryRenderedDOMComponentsWithClass(revisionsView, 'svg-icon-label');
		expect(user[0].innerHTML).toEqual(state.users.usersList[0].fullName);
		expect(user[1].innerHTML).toEqual(state.users.usersList[1].fullName);
		let message = scryRenderedDOMComponentsWithTestId(revisionsView,'revision-message');
		expect(message[0].children[0].innerHTML).toEqual('Show Message With More Mock');
		expect(message[1].children[0].innerHTML).toEqual('Show Message Mock');
	});
*/

});


describe('Revisions Action Helper', () => {
	let store, dispatch, restPrefix = '', revisions, version;
	let itemId = 'testRevisionId';

	beforeAll(() => {
		restPrefix = Configuration.get('restPrefix');
		store = storeCreator();
		dispatch = store.dispatch;
		deepFreeze(store.getState());
		revisions = RevisionsPagePropsFactory.buildList(2);
		version = VersionFactory.build();
	});

	beforeEach(() => {
		mockRest.resetQueue();
	});



	it('Get revisions list', () => {

		const expectedStore = cloneAndSet(store.getState(), 'revisions', revisions);
		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/items/${itemId}/versions/${version.id}/revisions`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: revisions};
		});

		return RevisionsActionHelper.fetchRevisions(dispatch, {itemId, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
			expect(store.getState().revisions.length).toEqual(2);
		});
	});
/*
	it('Revert to revision software product model', () => {
		mockRest.resetQueue();
		let revisionId = revisions[1].id;
		mockRest.addHandler('put', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/items/${itemId}/versions/${version.id}/actions`);
			expect(data).toEqual({
				action: vcActionsEnum.REVERT,
				revisionRequest: {
					revisionId: revisionId
				}

			});
			expect(options).toEqual(undefined);
			return {results: {}};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/items/${itemId}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: {}};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${itemId}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: {}};
		});



		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${itemId}/versions/${version.id}/questionnaire`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {data: JSON.stringify({}), schema: JSON.stringify({})};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${itemId}/versions/${version.id}/components`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: {}};
		});


		return RevisionsActionHelper.revertToRevision(dispatch, {itemId, version,  revisionId, itemType: screenTypes.SOFTWARE_PRODUCT}).then(() => {
		});

	});
*/
	it('Revert to revision license model', () => {

		let revisionId = revisions[0].id;
		mockRest.addHandler('put', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/items/${itemId}/versions/${version.id}/actions`);
			expect(data).toEqual({
				action: vcActionsEnum.REVERT,
				revisionRequest: {
					revisionId: revisionId
				}

			});
			expect(options).toEqual(undefined);
			return {results: {}};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/items/${itemId}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: {}};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-license-models/${itemId}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: {}};
		});


		let vlmFetched = ['license-agreements', 'feature-groups', 'entitlement-pools', 'license-key-groups'];
		vlmFetched.forEach(fetchCall => {
			mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
				expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-license-models/${itemId}/versions/${version.id}/` + fetchCall);
				expect(data).toEqual(undefined);
				expect(options).toEqual(undefined);
				return {results: {}};
			});
		});


		return RevisionsActionHelper.revertToRevision(dispatch, {itemId, version,  revisionId, itemType: screenTypes.LICENSE_MODEL}).then(() => {
		});

	});




});
