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
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import Limits from 'sdc-app/onboarding/licenseModel/limits/Limits.jsx';
import {actionTypes as globalModalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import EntitlementPoolsActionHelper from './EntitlementPoolsActionHelper.js';

const mapStateToProps = ({licenseModel: {entitlementPool: {entitlementPoolEditor: {data}}, limitEditor}, currentScreen}) => {	
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
		onSubmit: (limit, entitlementPool, licenseModelId, version) => EntitlementPoolsActionHelper.submitLimit(dispatch,
			{
				limit,
				entitlementPool,
				licenseModelId,
				version}),
		onDelete: ({limit, parent, licenseModelId, version, onCloseLimitEditor, selectedLimit}) => dispatch({
			type: globalModalActionTypes.GLOBAL_MODAL_WARNING,
			data:{
				msg: i18n(`Are you sure you want to delete ${limit.name}?`),
				confirmationButtonText: i18n('Delete'),
				title: i18n('Delete'),
				onConfirmed: ()=> EntitlementPoolsActionHelper.deleteLimit(dispatch, {limit, entitlementPool: parent, licenseModelId, version}).then(() => 
					selectedLimit === limit.id && onCloseLimitEditor()
				)
			}
		})
	};
};

export default connect(mapStateToProps, mapActionsToProps)(Limits);