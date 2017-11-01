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
import EntitlementPoolsActionHelper from './EntitlementPoolsActionHelper.js';
import EntitlementPoolsListEditorView, {generateConfirmationMsg} from './EntitlementPoolsListEditorView.jsx';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import {actionTypes as globalMoadlActions}  from 'nfvo-components/modal/GlobalModalConstants.js';

const mapStateToProps = ({licenseModel: {entitlementPool, licenseModelEditor}}) => {
	let {entitlementPoolsList} = entitlementPool;
	let {data} = entitlementPool.entitlementPoolEditor;

	let {vendorName} = licenseModelEditor.data;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(licenseModelEditor.data);

	return {
		vendorName,
		entitlementPoolsList,
		isReadOnlyMode,
		isDisplayModal: Boolean(data),
		isModalInEditMode: Boolean(data && data.id),
	};
};

const mapActionsToProps = (dispatch, {licenseModelId, version}) => {
	return {
		onAddEntitlementPoolClick: () => EntitlementPoolsActionHelper.openEntitlementPoolsEditor(dispatch),
		onEditEntitlementPoolClick: entitlementPool => EntitlementPoolsActionHelper.openEntitlementPoolsEditor(dispatch, {entitlementPool, licenseModelId, version}),
		onDeleteEntitlementPool: entitlementPool => dispatch({
			type: globalMoadlActions.GLOBAL_MODAL_WARNING,
			data:{
				msg: generateConfirmationMsg(entitlementPool),
				confirmationButtonText: i18n('Delete'),
				title: i18n('Delete'),
				onConfirmed: ()=>EntitlementPoolsActionHelper.deleteEntitlementPool(dispatch, {
					licenseModelId,
					entitlementPoolId: entitlementPool.id,
					version
				})
			}
		})
	};
};

export default connect(mapStateToProps, mapActionsToProps)(EntitlementPoolsListEditorView);
