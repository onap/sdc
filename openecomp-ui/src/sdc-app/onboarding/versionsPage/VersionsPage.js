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
import VersionsPageActionHelper from './VersionsPageActionHelper.js';
import VersionsPageCreationActionHelper from './creation/VersionsPageCreationActionHelper.js';
import PermissionsActionHelper from '../permissions/PermissionsActionHelper.js';
import {onboardingMethod as onboardingMethodType} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';
import VersionsPageView from './VersionsPage.jsx';

export const mapStateToProps = ({
	users: {userInfo},
	versionsPage: {permissions, versionsList},
	currentScreen: {itemPermission: {isCollaborator}, props: {itemId}},
	softwareProductList = []
}) => {

	let {versions, selectedVersion} = versionsList;
	let {owner, contributors, viewers} = permissions;

	// sorting the version list
	versions.sort((a,b) => {
		let statusCompare = b.status.localeCompare(a.status);
		if (statusCompare === 0) {
			return b.modificationTime - a.modificationTime;
		} else {
			return statusCompare;
		}

	});

	const curentSoftwareProduct = softwareProductList.find(item => item.id === itemId);
	return {
		versions,
		contributors,
		viewers,
		owner,
		currentUser: userInfo,
		selectedVersion,
		isCollaborator,
		isManual: curentSoftwareProduct && curentSoftwareProduct.onboardingMethod === onboardingMethodType.MANUAL
	};

};

export const mapActionsToProps = (dispatch, {itemType, itemId, additionalProps}) => {
	return {
		onNavigateToVersion({version}) {
			VersionsPageActionHelper.onNavigateToVersion(dispatch, {version, itemId, itemType, additionalProps});
		},

		onSelectVersion({version}) {
			VersionsPageActionHelper.selectVersion(dispatch, {version});
		},

		onCreateVersion({version}) {
			VersionsPageCreationActionHelper.open(dispatch, {baseVersion: version, itemId, itemType, additionalProps});
		},

		onManagePermissions() {
			PermissionsActionHelper.openPermissonsManager(dispatch, {itemId, askForRights: false});
		},

		onTreeFullScreen(treeProps) {
			VersionsPageActionHelper.openTree(dispatch, treeProps);
		},

		onModalNodeClick({version}) {
			VersionsPageActionHelper.selectVersionFromModal(dispatch, {version});
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(VersionsPageView);
