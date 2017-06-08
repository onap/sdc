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
import {mount} from 'enzyme';
import VersionController from 'nfvo-components/panel/versionController/VersionController.jsx';
import {actionsEnum, statusEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import {scryRenderedDOMComponentsWithTestId} from 'test-utils/Util.js';
import {VSPComponentsVersionControllerFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsNetworkFactories.js';

describe('versionController UI Component', () => {
	let onSave, onClose, onVersionSwitching = onSave = onClose = () => {return Promise.resolve();};
	const versionData = VSPComponentsVersionControllerFactory.build();
	const isFormDataValid = true;
	const viewableVersions = versionData.viewableVersions;
	const version = versionData.version;
	const props = {onSave, onClose, isFormDataValid, viewableVersions, version, onVersionSwitching};

	it('function does exist', () => {
		var renderer = TestUtils.createRenderer();
		renderer.render(<VersionController isCheckedOut={false} status={statusEnum.CHECK_OUT_STATUS} {...props} />);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

	it('validating checkin function', () => {
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={true} status={statusEnum.CHECK_OUT_STATUS} {...props} />);
		let cb = action => expect(action).toBe(actionsEnum.CHECK_IN);
		versionController.checkin(cb);
	});

	it('validating checkout function', () => {
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={false} status={statusEnum.CHECK_IN_STATUS} {...props} />);
		let cb = action => expect(action).toBe(actionsEnum.CHECK_OUT);
		versionController.checkout(cb);
	});

	it('validating submit function', () => {
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={false} status={statusEnum.CHECK_IN_STATUS} {...props} />);
		let cb = action => expect(action).toBe(actionsEnum.SUBMIT);
		versionController.submit(cb);
	});

	it('validating revert function', () => {
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={true} status={statusEnum.CHECK_OUT_STATUS} {...props} />);
		let cb = action => expect(action).toBe(actionsEnum.UNDO_CHECK_OUT);
		versionController.revertCheckout(cb);
	});

	it('does not show the save button when no onSave available', () => {
		let noSaveProps = {...props, onSave: null };
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={true} status={statusEnum.CHECK_OUT_STATUS} {...noSaveProps} />);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-save-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(0);
	});

	it('does not show the submit button when no callVCAction available', () => {
		let callVCActionProps = {...props, callVCAction: null};
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={false} status={statusEnum.CHECK_IN_STATUS} {...callVCActionProps} />);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-submit-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(0);
	});

	it('does not show the revert button when no callVCAction available', () => {
		let callVCActionProps = {...props, callVCAction: null};
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={true} status={statusEnum.CHECK_OUT_STATUS} {...callVCActionProps} />);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-revert-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(0);
	});

	it('Shows the save button when onSave available', () => {
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={true} status={statusEnum.CHECK_OUT_STATUS} {...props} />);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-save-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

	it('Shows the submit button when callVCAction available', () => {
		let callVCActionProps = { ...props, callVCAction: function(){} };
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={false} status={statusEnum.CHECK_IN_STATUS} {...callVCActionProps} />);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-submit-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

	it('Shows the revert button when callVCAction available', () => {
		let callVCActionProps = { ...props, callVCAction: function(){} };
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={true} status={statusEnum.CHECK_OUT_STATUS} {...callVCActionProps} />);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-revert-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

	it('Shows the checkin button', () => {
		let callVCActionProps = { ...props, callVCAction: function(){} };
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={true} status={statusEnum.CHECK_OUT_STATUS} {...callVCActionProps} />);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-checkout-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

	it('Shows the checkout button', () => {
		let callVCActionProps = { ...props, callVCAction: function(){} };
		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={false} status={statusEnum.CHECK_IN_STATUS} {...callVCActionProps} />);
		let elem = scryRenderedDOMComponentsWithTestId(versionController,'vc-checkout-btn');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);

	});

	it('Doesn\'t show the checkin button for prev version', () => {
		let callVCActionProps = { ...props, version: '1.0', callVCAction: function(){} };
		let versionController = mount(<VersionController isCheckedOut={true} status={statusEnum.CHECK_OUT_STATUS} {...callVCActionProps} />);
		let elem = versionController.find('[data-test-id="vc-checkout-btn"]');
		let svgIcon = versionController.find('.version-controller-lock-closed');

		expect(elem).toBeTruthy();
		expect(elem.length).toEqual(1);
		expect(svgIcon.hasClass('disabled')).toBe(true);
	});

	it('Doesn\'t show the checkout button', () => {
		let callVCActionProps = { ...props, version: '1.0', callVCAction: function(){} };
		let versionController = mount(<VersionController isCheckedOut={false} status={statusEnum.CHECK_IN_STATUS} {...callVCActionProps} />);
		let elem = versionController.find('[data-test-id="vc-checkout-btn"]');
		let svgIcon = versionController.find('.version-controller-lock-closed');

		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
		expect(svgIcon.hasClass('disabled')).toBe(true);

	});

});
