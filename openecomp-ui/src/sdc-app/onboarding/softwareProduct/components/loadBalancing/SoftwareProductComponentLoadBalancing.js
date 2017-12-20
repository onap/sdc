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
import SoftwareProductComponentLoadBalancingView from './SoftwareProductComponentLoadBalancingRefView.jsx';
import SoftwareProductComponentsActionHelper from '../SoftwareProductComponentsActionHelper.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {COMPONENTS_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';

export const mapStateToProps = ({softwareProduct: {softwareProductComponents}}) => {

	let {componentEditor: {qdata, qgenericFieldInfo: genericFieldInfo, dataMap}} = softwareProductComponents;

	return {
		qdata,
		genericFieldInfo,
		dataMap
	};

};

const mapActionsToProps = (dispatch, {softwareProductId, version, componentId}) => {
	return {
		onQDataChanged: (deltaData) => ValidationHelper.qDataChanged(dispatch, {deltaData, qName: COMPONENTS_QUESTIONNAIRE}),
		onSubmit: ({qdata}) =>{ return SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(dispatch, {softwareProductId, version, vspComponentId: componentId, qdata});}
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductComponentLoadBalancingView);
