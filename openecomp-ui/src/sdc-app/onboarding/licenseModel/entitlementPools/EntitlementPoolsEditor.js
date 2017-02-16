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
import EntitlementPoolsActionHelper from './EntitlementPoolsActionHelper.js';
import EntitlementPoolsEditorView from './EntitlementPoolsEditorView.jsx';

const mapStateToProps = ({licenseModel: {entitlementPool}}) => {


	let {data} = entitlementPool.entitlementPoolEditor;
	
	let previousData;
	const entitlementPoolId = data ? data.id : null;
	if(entitlementPoolId) {
		previousData = entitlementPool.entitlementPoolsList.find(entitlementPool => entitlementPool.id === entitlementPoolId);
	}

	return {
		data,
		previousData
	};
};

const mapActionsToProps = (dispatch, {licenseModelId}) => {
	return {
		onDataChanged: deltaData => EntitlementPoolsActionHelper.entitlementPoolsEditorDataChanged(dispatch, {deltaData}),
		onCancel: () => EntitlementPoolsActionHelper.closeEntitlementPoolsEditor(dispatch),
		onSubmit: ({previousEntitlementPool, entitlementPool}) => {
			EntitlementPoolsActionHelper.closeEntitlementPoolsEditor(dispatch);
			EntitlementPoolsActionHelper.saveEntitlementPool(dispatch, {licenseModelId, previousEntitlementPool, entitlementPool});
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(EntitlementPoolsEditorView);
