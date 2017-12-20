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
import SoftwareProductComponentComputeView from './SoftwareProductComponentComputeView.jsx';
import SoftwareProductComponentsActionHelper from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsActionHelper.js';
import {COMPONENTS_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {onboardingMethod} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';


const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductEditor: {data: currentVSP}, softwareProductComponents} = softwareProduct;
	let {componentEditor: {qdata, dataMap, qgenericFieldInfo}, computeFlavor: {computesList: computeFlavorsList}} = softwareProductComponents;

	return {
		qdata,
		dataMap,
		qgenericFieldInfo,
		computeFlavorsList,
		isManual: currentVSP.onboardingMethod === onboardingMethod.MANUAL
	};
};

const mapActionToProps = (dispatch, {softwareProductId, version, componentId}) => {
	return {
		onQDataChanged: (deltaData, customValidations) => ValidationHelper.qDataChanged(dispatch, {deltaData, customValidations: customValidations,
			qName: COMPONENTS_QUESTIONNAIRE}),
		onSubmit: ({qdata}) =>{ return SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(dispatch, {softwareProductId, version, vspComponentId: componentId, qdata});},
		qValidateData: (data, customValidations) => ValidationHelper.qValidateData(dispatch, {data, customValidations: customValidations,
			qName: COMPONENTS_QUESTIONNAIRE})
	};
};

export default connect(mapStateToProps, mapActionToProps, null, {withRef: true}) (SoftwareProductComponentComputeView);
