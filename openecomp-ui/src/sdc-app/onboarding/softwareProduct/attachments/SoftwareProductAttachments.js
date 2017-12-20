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
import {connect} from 'react-redux';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import HeatSetupActionHelper from './setup/HeatSetupActionHelper.js';
import SoftwareProductAttachmentsView from './SoftwareProductAttachmentsView.jsx';
import {errorLevels} from 'sdc-app/onboarding/softwareProduct/attachments/validation/HeatValidationConstants.js';
import HeatSetup from './setup/HeatSetup.js';
import {doesHeatDataExist} from './SoftwareProductAttachmentsUtils.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import {enums, screenTypes} from 'sdc-app/onboarding/OnboardingConstants.js';
import SoftwareProductAttachmentsActionHelper from './SoftwareProductAttachmentsActionHelper.js';

export const mapStateToProps = (state) => {
	let {
		softwareProduct: {
			softwareProductEditor:{data: currentSoftwareProduct = {}},
			softwareProductAttachments: {attachmentsDetails: {activeTab}, heatSetup, heatSetupCache, heatValidation : {errorList}}
		}
	} = state;

	let {unassigned = [], modules = []} = heatSetup;
	let goToOverview = true;
	if (errorList) {
		for (let i = 0 ; i < errorList.length ; i++) {
			if (errorList[i].level === errorLevels.ERROR) {
				goToOverview = false;
			}
		}
	}
	let heatDataExist = doesHeatDataExist(heatSetup);

	let {version, onboardingOrigin} = currentSoftwareProduct;
	return {
		isValidationAvailable: unassigned.length === 0 && modules.length > 0,
		heatSetup,
		heatSetupCache,
		heatDataExist,
		goToOverview,
		HeatSetupComponent: HeatSetup,
		version,
		onboardingOrigin,
		activeTab
	};
};

export const mapActionsToProps = (dispatch, {softwareProductId, version}) => {
	return {
		onDownload: ({heatCandidate, isReadOnlyMode}) => SoftwareProductActionHelper.downloadHeatFile(dispatch, {softwareProductId, heatCandidate, isReadOnlyMode, version}),
		onUpload: (formData) => dispatch({
			type: modalActionTypes.GLOBAL_MODAL_WARNING,
			data:{
				msg: i18n('Upload will erase existing data. Do you want to continue?'),
				confirmationButtonText: i18n('Continue'),
				onConfirmed: ()=>SoftwareProductActionHelper.uploadFile(dispatch, {
					softwareProductId,
					formData,
					failedNotificationTitle: i18n('Upload validation failed'),
					version
				})
			}
		}),
		onInvalidFileUpload: () => dispatch({
			type: modalActionTypes.GLOBAL_MODAL_ERROR,
			data: {
				title: i18n('Upload Failed'),
				confirmationButtonText: i18n('Continue'),
				msg: i18n('no zip or csar file was uploaded or expected file doesn\'t exist')
			}
		}),
		onSave: (heatCandidate) => SoftwareProductActionHelper.updateSoftwareProductHeatCandidate(dispatch, {softwareProductId, heatCandidate, version}),
		onGoToOverview: () => ScreensHelper.loadScreen(dispatch, {
			screen: enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE, screenType: screenTypes.SOFTWARE_PRODUCT,
			props: {softwareProductId, version}
		}),
		onProcessAndValidate: ({heatData, heatDataCache, isReadOnlyMode}) => {
			return HeatSetupActionHelper.processAndValidateHeat(dispatch,
				{softwareProductId, heatData, heatDataCache, isReadOnlyMode, version});
		},
		setActiveTab: ({activeTab}) => SoftwareProductAttachmentsActionHelper.setActiveTab(dispatch, {activeTab})

	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductAttachmentsView);
