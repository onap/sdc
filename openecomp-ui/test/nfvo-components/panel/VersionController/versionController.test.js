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
import VersionController from 'nfvo-components/panel/versionController/VersionController.jsx';
import {actionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import {scryRenderedDOMComponentsWithTestId} from 'test-utils/Util.js';
import {VSPComponentsVersionControllerFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsNetworkFactories.js';
import { Provider } from 'react-redux';
import {storeCreator} from 'sdc-app/AppStore.js';

describe('versionController UI Component', () => {
	let onSave, onClose, onVersionSwitching = onSave = onClose = () => {return Promise.resolve();};
	const versionData = VSPComponentsVersionControllerFactory.build();
	const isFormDataValid = true;
	const viewableVersions = versionData.viewableVersions;
	const version = versionData.version;
	const itemPermission = {isCertified: false, isCollaborator: true, isDirty: false};
	const props = {onSave, onClose, isFormDataValid, viewableVersions, version, onVersionSwitching, itemPermission};
	const store = storeCreator();

	it('function does exist', () => {
		var renderer = TestUtils.createRenderer();

		renderer.render(<Provider store={store}><VersionController {...props} /></Provider>);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

	it('validating submit function', () => {
		let provider = TestUtils.renderIntoDocument(<Provider store={store}>
			<VersionController {...props} /></Provider>);
		let versionController = TestUtils.findRenderedComponentWithType(
			provider,
			VersionController
		);
		let cb = action => expect(action).toBe(actionsEnum.SUBMIT);
		versionController.submit(cb);
	});

	it('validating revert function', () => {
		let provider = TestUtils.renderIntoDocument(<Provider store={store}><VersionController {...props} /></Provider>);
		let versionController = TestUtils.findRenderedComponentWithType(
			provider,
			VersionController
		);
		let cb = action => expect(action).toBe(actionsEnum.REVERT);
		versionController.revert(cb);
	});

	it('does not show the save button when no onSave available', () => {
		let noSaveProps = {...props, onSave: null };
		let versionController = TestUtils.renderIntoDocument(<Provider store={store}><VersionController {...noSaveProps} /></Provider>);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-save-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(0);
	});

	it('does not show the submit button when no callVCAction available', () => {
		let callVCActionProps = {...props, callVCAction: null};
		let versionController = TestUtils.renderIntoDocument(<Provider store={store}><VersionController {...callVCActionProps} /></Provider>);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-submit-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(0);
	});

	it('does not show the revert button when no callVCAction available', () => {
		let callVCActionProps = {...props, callVCAction: null};
		let versionController = TestUtils.renderIntoDocument(<Provider store={store}><VersionController {...callVCActionProps} /></Provider>);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-revert-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(0);
	});

	it('Shows the save button when onSave available', () => {
		let versionController = TestUtils.renderIntoDocument(<Provider store={store}><VersionController {...props} /></Provider>);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-save-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

	it('Shows the submit button when callVCAction available and user is owner', () => {
		const permissions = {owner: {userId: '111'}},
			userInfo = {userId: '111'};
		let callVCActionProps = { ...props, callVCAction: function(){}, permissions, userInfo};
		let versionController = TestUtils.renderIntoDocument(<Provider store={store}><VersionController {...callVCActionProps} /></Provider>);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-submit-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

	it('Doesn\'t show the submit button when user is not owner', () => {
		const permissions = {owner: {userId: '111'}},
			userInfo = {userId: '222'};
		let callVCActionProps = { ...props, callVCAction: function(){}, permissions, userInfo};
		let versionController = TestUtils.renderIntoDocument(<Provider store={store}><VersionController {...callVCActionProps} /></Provider>);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-submit-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(0);
	});

	it('Shows the revert button when callVCAction available', () => {
		let callVCActionProps = { ...props, callVCAction: function(){} };
		let versionController = TestUtils.renderIntoDocument(<Provider store={store}><VersionController {...callVCActionProps} /></Provider>);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-revert-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

});
