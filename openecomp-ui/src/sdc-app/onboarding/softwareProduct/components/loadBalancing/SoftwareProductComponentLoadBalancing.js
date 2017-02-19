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
import SoftwareProductComponentLoadBalancingView from './SoftwareProductComponentLoadBalancingRefView.jsx';
import SoftwareProductComponentsActionHelper from '../SoftwareProductComponentsActionHelper.js';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';

export const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductEditor: {data: currentVSP}, softwareProductComponents} = softwareProduct;
	let {componentEditor: {qdata, qschema}} = softwareProductComponents;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(currentVSP);

	return {
		qdata,
		qschema,
		isReadOnlyMode
	};
};


const mapActionsToProps = (dispatch, {softwareProductId, componentId}) => {
	return {
		onQDataChanged: ({data}) => SoftwareProductComponentsActionHelper.componentQuestionnaireUpdated(dispatch, {data}),
		onSubmit: ({qdata}) =>{ return SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(dispatch, {softwareProductId, vspComponentId: componentId, qdata});}
	};

};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductComponentLoadBalancingView);
