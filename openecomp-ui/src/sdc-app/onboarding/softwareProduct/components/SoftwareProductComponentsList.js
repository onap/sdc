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
import SoftwareProductComponentsListView from './SoftwareProductComponentsListView.jsx';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';


const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductEditor: {data: currentSoftwareProduct}, softwareProductComponents} = softwareProduct;
	let {componentsList} = softwareProductComponents;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(currentSoftwareProduct);

	return {
		currentSoftwareProduct,
		isReadOnlyMode,
		componentsList
	};
};


const mapActionToProps = (dispatch) => {
	return {
		onComponentSelect: ({id: softwareProductId, componentId, version}) => {
			OnboardingActionHelper.navigateToSoftwareProductComponentGeneralAndUpdateLeftPanel(dispatch, {softwareProductId, componentId, version });
		}
	};
};

export default connect(mapStateToProps, mapActionToProps, null, {withRef: true})(SoftwareProductComponentsListView);
