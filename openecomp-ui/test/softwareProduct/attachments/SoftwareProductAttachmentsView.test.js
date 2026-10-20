/*
 * Copyright © 2016-2018 European Support Limited
 * Modifications Copyright (C) 2026 Deutsche Telekom AG
 *
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
 */

import React from 'react';
import ShallowRenderer from 'react-test-renderer/shallow';
import TestUtils from 'react-dom/test-utils';
import {VSPAttachmentTreeNodeWithChildrenFactory, VSPAttachmentDetailedError} from 'test-utils/factories/softwareProduct/SoftwareProductAttachmentsFactories.js';
import {defaultStoreFactory} from 'test-utils/factories/onboard/OnboardingCatalogFactories.js';

import {mapStateToProps} from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachments.js';
import SoftwareProductAttachmentsView from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachmentsView.jsx';
import {tabsMapping} from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachmentsConstants.js';
import VersionControllerUtilsFactory from 'test-utils/factories/softwareProduct/VersionControllerUtilsFactory.js';

describe('SoftwareProduct Attachments - View: ', function () {
	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('should mapper return default data', () => {

		let attachmentsTree = VSPAttachmentTreeNodeWithChildrenFactory.build();
		let errorList = VSPAttachmentDetailedError.buildList(3);
		let versionControllerData = VersionControllerUtilsFactory.build();

		let softwareProductAttachments = {
			attachmentsDetails: {activeTab: tabsMapping.SETUP},
			heatSetup: {},
			heatValidation: {
				attachmentsTree,
				errorList
			},
			heatSetupCache: {}
		};
		let data = defaultStoreFactory.build({softwareProduct: {softwareProductAttachments, softwareProductEditor: {data: {...versionControllerData}}}});
		var result = mapStateToProps(data);
		expect(result).toBeTruthy();
		expect(result.isValidationAvailable).toBe(false);
	});

	it('view test', () => {

		let attachmentsTree = VSPAttachmentTreeNodeWithChildrenFactory.build();
		let errorList = VSPAttachmentDetailedError.buildList(3);
		let versionControllerData = VersionControllerUtilsFactory.build();

		let softwareProductAttachments = {
			attachmentsDetails: {},
			heatSetup: {},
			heatValidation: {
				attachmentsTree,
				errorList
			},
			shouldOpenValidationTab: false
		};
		let data = defaultStoreFactory.build({softwareProduct: {softwareProductAttachments, softwareProductEditor: {data: {...versionControllerData}}}});
		var params = mapStateToProps(data);

		const renderer = new ShallowRenderer();
		renderer.render(<SoftwareProductAttachmentsView {...params}/>);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

	const renderCandidateControls = isReadOnlyMode => {
		const renderer = new ShallowRenderer();
		renderer.render(<SoftwareProductAttachmentsView
			activeTab={tabsMapping.SETUP}
			candidateInProcess={true}
			isValidationAvailable={true}
			isReadOnlyMode={isReadOnlyMode}
			setActiveTab={() => {}}/>);
		const controllers = renderer.getRenderOutput().props.children[0];
		return React.Children.toArray(controllers.props.children)
			.filter(child => child && child.props)
			.map(child => child.props['data-test-id']);
	};

	it('shows proceed-to-validation and abort for an editable candidate', () => {
		const testIds = renderCandidateControls(false);
		expect(testIds).toContain('proceed-to-validation-btn');
		expect(testIds).toContain('abort-btn');
	});

	it('hides proceed-to-validation and abort in read-only mode', () => {
		const testIds = renderCandidateControls(true);
		expect(testIds).not.toContain('proceed-to-validation-btn');
		expect(testIds).not.toContain('abort-btn');
	});

});
