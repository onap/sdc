/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

import {connect} from 'react-redux';

import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';
import SoftwareProductCreationActionHelper from './SoftwareProductCreationActionHelper.js';
import SoftwareProductCreationView from './SoftwareProductCreationView.jsx';

const mapStateToProps = ({finalizedLicenseModelList, softwareProduct: {softwareProductCreation, softwareProductCategories} }) => {
	return {
		data: softwareProductCreation.data,
		softwareProductCategories,
		finalizedLicenseModelList
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onDataChanged: deltaData => SoftwareProductCreationActionHelper.changeData(dispatch, {deltaData}),
		onCancel: () => SoftwareProductCreationActionHelper.resetData(dispatch),
		onSubmit: (softwareProduct) => {
			SoftwareProductCreationActionHelper.resetData(dispatch);
			SoftwareProductCreationActionHelper.createSoftwareProduct(dispatch, {softwareProduct}).then(softwareProductId => {
				let {vendorId: licenseModelId, licensingVersion} = softwareProduct;
				OnboardingActionHelper.navigateToSoftwareProductLandingPage(dispatch, {softwareProductId, licenseModelId, licensingVersion});
			});
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductCreationView);
