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
import SoftwareProductComponentsNetworkActionHelper from './SoftwareProductComponentsNetworkActionHelper.js';
import SoftwareProductComponentsNICEditorView from './SoftwareProductComponentsNICEditorView.jsx';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';

export const mapStateToProps = ({softwareProduct}) => {

	let {softwareProductEditor: {data:currentSoftwareProduct = {},  isValidityData = true}, softwareProductComponents} = softwareProduct;

	let {network: {nicEditor = {}}} = softwareProductComponents;
	let {data, qdata, qschema} = nicEditor;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(currentSoftwareProduct);

	return {
		currentSoftwareProduct,
		isValidityData,
		data,
		qdata,
		qschema,
		isReadOnlyMode
	};

};

const mapActionsToProps = (dispatch, {softwareProductId, componentId}) => {
	return {
		onDataChanged: deltaData => SoftwareProductComponentsNetworkActionHelper.updateNICData(dispatch, {deltaData}),
		onSubmit: ({data, qdata}) => SoftwareProductComponentsNetworkActionHelper.saveNICDataAndQuestionnaire(dispatch, {softwareProductId, componentId, data, qdata}),
		onCancel: () => SoftwareProductComponentsNetworkActionHelper.closeNICEditor(dispatch),
		onQDataChanged: ({data}) => SoftwareProductComponentsNetworkActionHelper.updateNICQuestionnaire(dispatch, {data})
	};
};

export default connect(mapStateToProps, mapActionsToProps)(SoftwareProductComponentsNICEditorView);
