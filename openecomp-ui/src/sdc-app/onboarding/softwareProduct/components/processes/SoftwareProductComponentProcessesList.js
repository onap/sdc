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
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import SoftwareProductComponentProcessesActionHelper from './SoftwareProductComponentProcessesActionHelper.js';

import SoftwareProductComponentsProcessesListView from './SoftwareProductComponentsProcessesListView.jsx';

export const mapStateToProps = ({softwareProduct}) => {

	let {softwareProductEditor: {data:currentSoftwareProduct = {},  isValidityData = true}, softwareProductComponents: {componentProcesses = {}}} = softwareProduct;
	let{processesList = [], processesEditor = {}} = componentProcesses;
	let {data} = processesEditor;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(currentSoftwareProduct);

	return {
		currentSoftwareProduct,
		isValidityData,
		processesList,
		isDisplayModal: Boolean(data),
		isModalInEditMode: Boolean(data && data.id),
		isReadOnlyMode
	};

};

const mapActionsToProps = (dispatch, {componentId, softwareProductId}) => {

	return {
		onAddProcess: () => SoftwareProductComponentProcessesActionHelper.openEditor(dispatch),
		onEditProcessClick: (process) => SoftwareProductComponentProcessesActionHelper.openEditor(dispatch, process),
		onDeleteProcessClick: (process, version) => dispatch({
			type: modalActionTypes.GLOBAL_MODAL_WARNING,
			data:{
				msg: i18n(`Are you sure you want to delete "${process.name}"?`),
				confirmationButtonText: i18n('Delete'),
				title: i18n('Delete'),
				onConfirmed: ()=> SoftwareProductComponentProcessesActionHelper.deleteProcess(dispatch,
					{process, softwareProductId, version, componentId})
			}
		})
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductComponentsProcessesListView);
