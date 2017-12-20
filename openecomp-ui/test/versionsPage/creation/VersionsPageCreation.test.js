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
import {mapStateToProps, mapActionsToProps} from 'sdc-app/onboarding/versionsPage/creation/VersionsPageCreation.js';
import {VERSION_CREATION_FORM_NAME} from 'sdc-app/onboarding/versionsPage/creation/VersionsPageCreationConstants.js';
import VersionsPageCreationActionHelper from 'sdc-app/onboarding/versionsPage/creation/VersionsPageCreationActionHelper.js';
import VersionsPageCreationView from 'sdc-app/onboarding/versionsPage/creation/VersionsPageCreationView.jsx';
import {VersionsPageCreationFactory,
				VersionsPageCreationFieldInfoFactory} from 'test-utils/factories/versionsPage/VersionsPageCreationFactories.js';
import {VersionsPageAdditionalPropsFactory} from 'test-utils/factories/versionsPage/VersionsPageFactories.js';
import {storeCreator} from 'sdc-app/AppStore.js';

describe('Versions Page Creation Module Tests', function() {

	let versionCreationData, genericFieldInfo;
	let store, dispatch;

	beforeAll(() => {
		store = storeCreator();
		dispatch = store.dispatch;
		versionCreationData = VersionsPageCreationFactory.build();
		genericFieldInfo = VersionsPageCreationFieldInfoFactory.build();
	});

	const additionalProps = VersionsPageAdditionalPropsFactory.build();

	it ('mapStateToProps mapper exists', () => {

		expect(mapStateToProps).toBeTruthy();

	});

	it ('should return empty data', () => {

		const state = {
			versionsPage: {
				versionCreation: {
					data: {},
					genericFieldInfo: {}
				}
			}
		};

		const props = mapStateToProps(state);
		expect(props.data).toEqual({});
		expect(props.isFormValid).toEqual(true);

	});

	it ('should have state in props', () => {

		const state = {
			versionsPage: {
				versionCreation: {
					data: versionCreationData,
					genericFieldInfo
				}
			}
		};

		const props = mapStateToProps(state);
		expect(props.isFormValid).toEqual(true);
		expect(props.data.description).toEqual(versionCreationData.description);
		expect(props.genericFieldInfo).toEqual(genericFieldInfo);

	});

	it('simple jsx test', () => {

		const state = {
			versionsPage: {
				versionCreation: {
					data: versionCreationData,
					genericFieldInfo
				}
			}
		};

		const props = Object.assign({}, mapStateToProps(state), mapActionsToProps(dispatch, additionalProps), additionalProps);
		const renderer = TestUtils.createRenderer();
		renderer.render(<VersionsPageCreationView {...props} />);

		const renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

	});

	it('open/close form actions', () => {

		VersionsPageCreationActionHelper.open(dispatch, additionalProps);
		expect(store.getState().versionsPage.versionCreation.formName).toEqual(VERSION_CREATION_FORM_NAME);

		VersionsPageCreationActionHelper.close(dispatch);
		expect(store.getState().versionsPage.versionCreation).toEqual({});

	});

});