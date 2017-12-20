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
import NICCreationActionHelper from './NICCreationActionHelper.js';
import NICCreationView from './NICCreationView.jsx';
import SoftwareProductComponentsNetworkActionHelper from '../SoftwareProductComponentsNetworkActionHelper.js';
import {networkTypes, NIC_CREATION_FORM_NAME} from '../SoftwareProductComponentsNetworkConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';

export const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductEditor: {data:currentSoftwareProduct = {}}, softwareProductComponents} = softwareProduct;
	let {network: {nicCreation = {}}} = softwareProductComponents;
	let {data, genericFieldInfo, formReady} = nicCreation;
	data = {...data, networkType: networkTypes.EXTERNAL};
	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

	return {
		currentSoftwareProduct,
		data,
		genericFieldInfo,
		isFormValid,
		formReady
	};
};

const mapActionsToProps = (dispatch, {softwareProductId, version}) => {
	return {
		onDataChanged: deltaData => ValidationHelper.dataChanged(dispatch, {deltaData, formName: NIC_CREATION_FORM_NAME}),
		onCancel: () => NICCreationActionHelper.close(dispatch),
		onSubmit: ({nic, componentId}) => {
			SoftwareProductComponentsNetworkActionHelper.createNIC(dispatch, {nic, softwareProductId, componentId, version});
			NICCreationActionHelper.close(dispatch);
		},
		onValidateForm: () => ValidationHelper.validateForm(dispatch, NIC_CREATION_FORM_NAME)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(NICCreationView);
