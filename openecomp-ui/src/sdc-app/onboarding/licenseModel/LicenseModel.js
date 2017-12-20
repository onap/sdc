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
import TabulatedEditor from 'src/nfvo-components/editor/TabulatedEditor.jsx';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import {enums, screenTypes} from 'sdc-app/onboarding/OnboardingConstants.js';

import PermissionsActionHelper from './../permissions/PermissionsActionHelper.js';
import RevisionsActionHelper from './../revisions/RevisionsActionHelper.js';

import LicenseModelActionHelper from './LicenseModelActionHelper.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {CommitModalType} from 'nfvo-components/panel/versionController/components/CommitCommentModal.jsx';


const buildNavigationBarProps = (licenseModel, screen) => {
	const {id, vendorName, version} = licenseModel;
	const meta = {version};

	const groups = [{
		id,
		name: vendorName,
		items: [
			{
				id: enums.SCREEN.LICENSE_MODEL_OVERVIEW,
				name: i18n('Overview'),
				meta
			},
			{
				id: enums.SCREEN.LICENSE_AGREEMENTS,
				name: i18n('License Agreements'),
				meta
			},
			{
				id: enums.SCREEN.FEATURE_GROUPS,
				name: i18n('Feature Groups'),
				meta
			},
			{
				id: enums.SCREEN.ENTITLEMENT_POOLS,
				name: i18n('Entitlement Pools'),
				meta
			},
			{
				id: enums.SCREEN.LICENSE_KEY_GROUPS,
				name: i18n('License Key Groups'),
				meta
			},
			{
				id: enums.SCREEN.ACTIVITY_LOG,
				name: i18n('Activity Log'),
				meta
			}
		]
	}];

	return {
		activeItemId: screen, groups
	};
};


const buildVersionControllerProps = ({
	licenseModelEditor = {data: {}},
	versions,
	currentVersion,
	userInfo,
	usersList,
	permissions,
	itemPermission,
	isReadOnlyMode
}) => {
	const {isValidityData = true} = licenseModelEditor;
	return {
		version: currentVersion,
		viewableVersions: versions,
		isFormDataValid: isValidityData,
		permissions,
		userInfo,
		usersList,
		itemName: licenseModelEditor.data.vendorName,
		itemPermission,
		isReadOnlyMode
	};
};


const mapStateToProps = ({
	users: {userInfo, usersList},
	licenseModel: {licenseModelEditor},
	versionsPage: {permissions, versionsList: {versions, itemName}}
}, {
	currentScreen: {screen, itemPermission, props: {isReadOnlyMode, version: currentVersion}}
}) => {
	return {
		versionControllerProps: buildVersionControllerProps({
			licenseModelEditor,
			versions,
			currentVersion,
			userInfo,
			permissions,
			usersList,
			itemPermission,
			isReadOnlyMode
		}),
		navigationBarProps: buildNavigationBarProps(licenseModelEditor.data, screen)
	};
};


const mapActionsToProps = (dispatch, {currentScreen: {screen, props: {licenseModelId, version}}}) => {

	return {
		onVersionControllerAction: (action, version, comment) =>
			LicenseModelActionHelper.performVCAction(dispatch, {licenseModelId, action, version, comment}).then(updatedVersion => {
				ScreensHelper.loadScreen(dispatch, {screen, screenType: screenTypes.LICENSE_MODEL, props: {licenseModelId, version: updatedVersion}});
			}),

		onOpenCommentCommitModal: ({onCommit, title}) => dispatch({
			type: modalActionTypes.GLOBAL_MODAL_SHOW,
			data: {
				modalComponentName: modalContentMapper.COMMIT_COMMENT,
				modalComponentProps: {
					onCommit,
					type: CommitModalType.COMMIT
				},
				title
			}
		}),

		onVersionSwitching: version => {
			ScreensHelper.loadScreen(dispatch, {screen, screenType: screenTypes.LICENSE_MODEL, props: {licenseModelId, version}});
		},

		onManagePermissions() {
			PermissionsActionHelper.openPermissonsManager(dispatch, {itemId: licenseModelId, askForRights: false});
		},

		onMoreVersionsClick: ({itemName, users}) => {
			ScreensHelper.loadScreen(dispatch, {screen: enums.SCREEN.VERSIONS_PAGE, screenType: screenTypes.LICENSE_MODEL,
				props: {licenseModelId, licenseModel: {name: itemName}, usersList: users}});
		},

		onOpenPermissions: ({users}) => {
			return PermissionsActionHelper.fetchItemUsers(dispatch, {itemId: licenseModelId, allUsers: users});
		},

		onOpenRevisionsModal: () => {
			return RevisionsActionHelper.openRevisionsView(dispatch, {itemId: licenseModelId, version: version, itemType: screenTypes.LICENSE_MODEL});
		},

		onNavigate: ({id}) => {
			ScreensHelper.loadScreen(dispatch, {screen: id, screenType: screenTypes.LICENSE_MODEL, props: {licenseModelId, version}});
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(TabulatedEditor);
