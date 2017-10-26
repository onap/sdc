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
import {actionTypes as globalModalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import Limits from 'sdc-app/onboarding/licenseModel/limits/Limits.jsx';

import LicenseKeyGroupsActionHelper from './LicenseKeyGroupsActionHelper.js';

const mapStateToProps = ({licenseModel: {licenseKeyGroup: {licenseKeyGroupsEditor: {data}}, limitEditor}, currentScreen}) => {	
	let  {props: {licenseModelId, version}} = currentScreen;
	return {
		parent: data,		
		limitEditor,
		licenseModelId,
		version
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onDataChanged: (deltaData, formName, customValidations) => ValidationHelper.dataChanged(dispatch, {deltaData, formName, customValidations}),
		onSubmit: (limit, licenseKeyGroup, licenseModelId, version) => LicenseKeyGroupsActionHelper.submitLimit(dispatch,
			{
				limit,
				licenseKeyGroup,
				licenseModelId,
				version}),		
		onDelete: ({limit, parent, licenseModelId, version, onCloseLimitEditor, selectedLimit}) => dispatch({
			type: globalModalActionTypes.GLOBAL_MODAL_WARNING,
			data:{
				msg: i18n(`Are you sure you want to delete ${limit.name}?`),
				confirmationButtonText: i18n('Delete'),
				title: i18n('Delete'),
				onConfirmed: ()=> LicenseKeyGroupsActionHelper.deleteLimit(dispatch, {limit, licenseKeyGroup: parent, licenseModelId, version}).then(() => 
					selectedLimit === limit.id && onCloseLimitEditor()
				)
			}
		})
	};
};

export default connect(mapStateToProps, mapActionsToProps)(Limits);