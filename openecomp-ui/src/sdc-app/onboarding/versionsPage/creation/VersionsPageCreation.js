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
import VersionsPageCreationActionHelper from './VersionsPageCreationActionHelper.js';
import VersionsPageActionHelper from '../VersionsPageActionHelper.js';
import VersionsPageCreationView from './VersionsPageCreationView.jsx';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {VERSION_CREATION_FORM_NAME} from './VersionsPageCreationConstants.js';

export const mapStateToProps = ({versionsPage: {versionCreation}}) => {
	let {genericFieldInfo} = versionCreation;
	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

	return {...versionCreation, isFormValid};
};

export const mapActionsToProps = (dispatch, {itemId, itemType, additionalProps}) => {
	return {
		onDataChanged: (deltaData, customValidations) => ValidationHelper.dataChanged(dispatch, {deltaData, formName: VERSION_CREATION_FORM_NAME, customValidations}),
		onCancel: () => VersionsPageCreationActionHelper.close(dispatch),
		onSubmit: ({baseVersion, payload}) => {
			VersionsPageCreationActionHelper.close(dispatch);
			VersionsPageCreationActionHelper.createVersion(dispatch, {baseVersion, itemId, payload}).then(response => {
				VersionsPageActionHelper.onNavigateToVersion(dispatch, {version: response, itemId, itemType, additionalProps});
			});
		},
		onValidateForm: () => ValidationHelper.validateForm(dispatch, VERSION_CREATION_FORM_NAME)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(VersionsPageCreationView);
