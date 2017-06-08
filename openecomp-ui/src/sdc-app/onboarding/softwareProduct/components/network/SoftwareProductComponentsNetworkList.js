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
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';

import SoftwareProductComponentsActionHelper from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsActionHelper.js';
import SoftwareProductComponentsNetworkListView from './SoftwareProductComponentsNetworkListView.jsx';
import SoftwareProductComponentsNetworkActionHelper from './SoftwareProductComponentsNetworkActionHelper.js';
import {COMPONENTS_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';


export const mapStateToProps = ({softwareProduct}) => {

	let {softwareProductEditor: {data: currentSoftwareProduct = {}, isValidityData = true}, softwareProductComponents} = softwareProduct;
	let {network: {nicEditor = {}, nicList = []}, componentEditor: {data: componentData, qdata, dataMap, qgenericFieldInfo}} = softwareProductComponents;
	let {data} = nicEditor;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(currentSoftwareProduct);
	let {version} = currentSoftwareProduct;
	let isModalInEditMode = true;

	return {
		version,
		componentData,
		qdata,
		dataMap,
		qgenericFieldInfo,
		isValidityData,
		nicList,
		isDisplayModal: Boolean(data),
		isModalInEditMode,
		isReadOnlyMode
	};

};

const mapActionsToProps = (dispatch, {softwareProductId, componentId}) => {
	return {
		onQDataChanged: (deltaData) => ValidationHelper.qDataChanged(dispatch, {deltaData,
			qName: COMPONENTS_QUESTIONNAIRE}),
		onEditNicClick: (nic, version) => {
			Promise.all([
				SoftwareProductComponentsNetworkActionHelper.loadNICData({
					softwareProductId,
					version,
					componentId,
					nicId: nic.id
				}),
				SoftwareProductComponentsNetworkActionHelper.loadNICQuestionnaire(dispatch, {
					softwareProductId,
					version,
					componentId,
					nicId: nic.id
				})
			]).then(
				([{data}]) => SoftwareProductComponentsNetworkActionHelper.openNICEditor(dispatch, {nic, data})
			);
		},
		onSubmit: ({qdata, version}) => { return SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(dispatch,
			{softwareProductId, version,
			vspComponentId: componentId,
			qdata});
		}


	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductComponentsNetworkListView);
