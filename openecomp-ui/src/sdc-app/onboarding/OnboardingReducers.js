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
import {actionTypes, enums} from './OnboardingConstants.js';
import {actionTypes as permissionActionTypes} from './permissions/PermissionsConstants.js';
import {actionTypes as licenseModelCreateActionTypes} from './licenseModel/creation/LicenseModelCreationConstants.js';
import {actionTypes as softwareProductCreateActionTypes} from './softwareProduct/creation/SoftwareProductCreationConstants.js';
import {actionTypes as versionCreateActionTypes} from './versionsPage/creation/VersionsPageCreationConstants.js';
import {SyncStates} from 'sdc-app/common/merge/MergeEditorConstants.js';

import {catalogItemStatuses} from './onboard/onboardingCatalog/OnboardingCatalogConstants.js';
import Configuration from 'sdc-app/config/Configuration.js';

const checkReadOnly = ({isCollaborator = true, inMerge = false, isCertified = false}) => !isCollaborator || inMerge || isCertified;

const currentScreen = (state = {
	forceBreadCrumbsUpdate: false,
	screen: enums.SCREEN.ONBOARDING_CATALOG,
	itemPermission: {},
	props: {}
}, action) => {

	switch (action.type) {

		case actionTypes.SET_CURRENT_SCREEN: {
			let itemPermission = {...state.itemPermission};
			let {currentScreen} = action;

			if (currentScreen.props.version) {
				let {status} = currentScreen.props.version;
				itemPermission.isCertified = itemPermission.isCertified && status === catalogItemStatuses.CERTIFIED;
			}

			let isReadOnlyMode = checkReadOnly(itemPermission);
			let props = {...currentScreen.props, isReadOnlyMode};

			return {
				...state,
				...currentScreen,
				itemPermission,
				props
			};
		}

		case actionTypes.UPDATE_CURRENT_SCREEN_PROPS:
			return {
				...state,
				props: {
					...state.props,
					...action.props,
					isReadOnlyMode: checkReadOnly(state.itemPermission)
				}
			};

		case actionTypes.SET_CURRENT_SCREEN_VERSION:
			return {
				...state,
				props: {
					...state.props,
					version: action.version,
					isReadOnlyMode: checkReadOnly(state.itemPermission)
				}
			};

		case licenseModelCreateActionTypes.LICENSE_MODEL_CREATED:
		case softwareProductCreateActionTypes.SOFTWARE_PRODUCT_CREATED:
		case versionCreateActionTypes.VERSION_CREATED:
			return {
				...state,
				itemPermission: {
					isCollaborator: true,
					inMerge: false,
					isCertified: false
				},
				props: {
					...state.props,
					isReadOnlyMode: false
				}
			};

		case permissionActionTypes.ITEM_USERS_LOADED: {
			let userId = Configuration.get('UserID');
			let isCollaborator = false;

			if (userId === action.owner.userId) {
				isCollaborator = true;
			} else {
				isCollaborator = action.contributors.reduce(
					(foundUser, contributor) => foundUser || contributor.userId === userId, false
				);
			}

			let itemPermission = {...state.itemPermission, isCollaborator};
			let isReadOnlyMode = checkReadOnly(itemPermission);
			let props = {...state.props, isReadOnlyMode};

			return {
				...state,
				itemPermission,
				props
			};
		}

		case actionTypes.UPDATE_ITEM_STATUS: {
			const {itemState: {synchronizationState, dirty}, itemStatus, updatedVersion} = action;
			const inMerge = synchronizationState === SyncStates.MERGE;
			const isOutOfSync = synchronizationState === SyncStates.OUT_OF_SYNC;
			const isUpToDate = synchronizationState === SyncStates.UP_TO_DATE;
			const isCertified = itemStatus === catalogItemStatuses.CERTIFIED;
			const itemPermission = {...state.itemPermission, inMerge, isDirty: dirty, isOutOfSync, isUpToDate, isCertified};
			const isReadOnlyMode = checkReadOnly(itemPermission);
			const props = {...state.props, isReadOnlyMode, version: {...state.props.version, ...updatedVersion}};

			return {
				...state,
				itemPermission,
				props
			};
		}

		default:
			return state;

	}

};

export default currentScreen;
