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
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import {permissionTypes} from 'sdc-app/onboarding/permissions/PermissionsConstants.js';
import {actionsEnum as VersionControllerActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import {actionTypes as onboardingActionTypes} from 'sdc-app/onboarding/OnboardingConstants.js';

function baseUrl() {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/items`;
}

const ItemsHelper = {
	performVCAction({itemId, version, action, comment}) {
		const {id: versionId} = version;
		const data = {
			action,
			...action === VersionControllerActionsEnum.COMMIT && {commitRequest: {message: comment}}
		};
		return RestAPIUtil.put(`${baseUrl()}/${itemId}/versions/${versionId}/actions`, data);
	},

	fetchVersions({itemId}) {
		return RestAPIUtil.fetch(`${baseUrl()}/${itemId}/versions`);
	},

	fetchVersion({itemId, versionId}) {
		return RestAPIUtil.fetch(`${baseUrl()}/${itemId}/versions/${versionId}`);
	},

	fetchActivityLog({itemId, versionId}) {
		return RestAPIUtil.fetch(`${baseUrl()}/${itemId}/versions/${versionId}/activity-logs`);
	},

	fetchUsers({itemId}) {
		return RestAPIUtil.fetch(`${baseUrl()}/${itemId}/permissions`);
	},

	updateContributors({itemId, removedUsersIds, addedUsersIds}) {
		return RestAPIUtil.put(`${baseUrl()}/${itemId}/permissions/${permissionTypes.CONTRIBUTOR}`, {removedUsersIds, addedUsersIds});
	},

	changeOwner({itemId, ownerId}) {
		return RestAPIUtil.put(`${baseUrl()}/${itemId}/permissions/${permissionTypes.OWNER}`, {removedUsersIds: [], addedUsersIds: [ownerId]});
	},

	checkItemStatus(dispatch, {itemId, versionId}) {
		return ItemsHelper.fetchVersion({itemId, versionId}).then(response => {
			let state = response && response.state || {};
			const {baseId, description, id, name, status} = response;

			dispatch({
				type: onboardingActionTypes.UPDATE_ITEM_STATUS,
				itemState: state,
				itemStatus: response.status,
				updatedVersion: {baseId, description, id, name, status}
			});
			return Promise.resolve(response);
		});

	},
};

export default ItemsHelper;
