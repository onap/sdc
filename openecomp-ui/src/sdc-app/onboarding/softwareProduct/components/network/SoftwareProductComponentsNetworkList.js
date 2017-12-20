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

import SoftwareProductComponentsActionHelper from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsActionHelper.js';
import SoftwareProductComponentsNetworkListView from './SoftwareProductComponentsNetworkListView.jsx';
import SoftwareProductComponentsNetworkActionHelper from './SoftwareProductComponentsNetworkActionHelper.js';
import {COMPONENTS_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {actionTypes as GlobalModalActions} from 'nfvo-components/modal/GlobalModalConstants.js';
import NICCreationActionHelper from './NICCreation/NICCreationActionHelper.js';
import {onboardingMethod as onboardingMethodTypes} from '../../SoftwareProductConstants.js';


export const mapStateToProps = ({softwareProduct}) => {

	let {softwareProductEditor: {data: currentSoftwareProduct = {}, isValidityData = true}, softwareProductComponents} = softwareProduct;
	let {network: {nicList = []}, componentEditor: {data: componentData, qdata, dataMap, qgenericFieldInfo}} = softwareProductComponents;
	let {version, onboardingMethod} = currentSoftwareProduct;
	let isManual = onboardingMethod === onboardingMethodTypes.MANUAL;

	return {
		version,
		componentData,
		qdata,
		dataMap,
		qgenericFieldInfo,
		isValidityData,
		nicList,
		isManual
	};

};

const mapActionsToProps = (dispatch, {softwareProductId, componentId, version}) => {
	return {
		onQDataChanged: (deltaData) => ValidationHelper.qDataChanged(dispatch, {deltaData,
			qName: COMPONENTS_QUESTIONNAIRE}),
		onAddNic: () => NICCreationActionHelper.open(dispatch, {softwareProductId, componentId, modalClassName: 'network-nic-modal-create', version}),
		onDeleteNic: (nic) => dispatch({
			type: GlobalModalActions.GLOBAL_MODAL_WARNING,
			data:{
				msg: i18n('Are you sure you want to delete "{name}"?', {name: nic.name}),
				onConfirmed: () => SoftwareProductComponentsNetworkActionHelper.deleteNIC(dispatch, {softwareProductId,
					componentId, nicId: nic.id, version})
			}
		}),
		onEditNicClick: (nic, isReadOnlyMode) => {
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
				([{data}]) => SoftwareProductComponentsNetworkActionHelper.openNICEditor(dispatch, {nic, data,
					isReadOnlyMode, softwareProductId, componentId, modalClassName: 'network-nic-modal-edit', version})
			);
		},
		onSubmit: ({qdata}) => { return SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(dispatch,
			{softwareProductId, version,
			vspComponentId: componentId,
			qdata});
		}


	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductComponentsNetworkListView);
