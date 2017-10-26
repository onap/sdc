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

import FeatureGroupsActionHelper  from './FeatureGroupsActionHelper.js';
import FeatureGroupListEditorView, {generateConfirmationMsg} from './FeatureGroupListEditorView.jsx';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {actionTypes as globalMoadlActions}  from 'nfvo-components/modal/GlobalModalConstants.js';

export const mapStateToProps = ({licenseModel: {featureGroup, licenseModelEditor}}) => {
	const {featureGroupEditor: {data}, featureGroupsList} = featureGroup;
	let {vendorName, version} = licenseModelEditor.data;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(licenseModelEditor.data);
	return {
		vendorName,
		version,
		featureGroupsModal: {
			show: Boolean(data),
			editMode: Boolean(data && data.id)
		},
		featureGroupsList,
		isReadOnlyMode
	};
};


const mapActionsToProps = (dispatch, {licenseModelId}) => {
	return {
		onDeleteFeatureGroupClick: (featureGroup, version) => dispatch({
			type: globalMoadlActions.GLOBAL_MODAL_WARNING,
			data:{
				msg: generateConfirmationMsg(featureGroup),
				confirmationButtonText: i18n('Delete'),
				title: i18n('Delete'),
				onConfirmed: ()=>FeatureGroupsActionHelper.deleteFeatureGroup(dispatch, {featureGroupId: featureGroup.id, licenseModelId, version})
			}
		}),
		onAddFeatureGroupClick: (actualVersion) => FeatureGroupsActionHelper.openFeatureGroupsEditor(dispatch, {licenseModelId, version: actualVersion}),
		onEditFeatureGroupClick: (featureGroup, actualVersion) => FeatureGroupsActionHelper.openFeatureGroupsEditor(dispatch, {
			featureGroup,
			licenseModelId,
			version: actualVersion
		})
	};
};

export default connect(mapStateToProps, mapActionsToProps)(FeatureGroupListEditorView);
