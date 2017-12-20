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

import i18n from 'nfvo-utils/i18n/i18n.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {actionTypes} from './PermissionsConstants.js';
import {permissionTypes} from './PermissionsConstants.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {askForRightsMsg} from './PermissionsManager.jsx';
import ItemsHelper from 'sdc-app/common/helpers/ItemsHelper.js';

const PermissionsActionHelper = {
	openPermissonsManager(dispatch, {itemId, askForRights}) {
		if (askForRights) {
			dispatch({
				type: modalActionTypes.GLOBAL_MODAL_SHOW,
				data: {
					title: i18n('Ask For Contributers Rights'),
					msg: askForRightsMsg(),
					confirmationButtonText: i18n('SEND'),
					onConfirmed: () => 	this.askForContributorRights()
				}
			});
		} else {
			dispatch({
				type: modalActionTypes.GLOBAL_MODAL_SHOW,
				data: {
					modalComponentName: modalContentMapper.MANAGE_PERMISSIONS,
					title: i18n('Manage Permissions'),
					modalComponentProps: {
						itemId
					}
				}
			});
		}
	},

	closePermissionManager(dispatch) {
		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_CLOSE
		});
	},

	saveItemUsers(dispatch, {itemId, removedUsersIds, addedUsersIds, allUsers}) {
		return ItemsHelper.updateContributors({itemId, removedUsersIds, addedUsersIds}).then(() =>
			PermissionsActionHelper.fetchItemUsers(dispatch, {itemId, allUsers})
		);
	},

	changeOwner(dispatch, {itemId, newOwnerId, allUsers}) {
		return ItemsHelper.changeOwner({itemId, ownerId: newOwnerId}).then(() =>
			PermissionsActionHelper.fetchItemUsers(dispatch, {itemId, allUsers})
		);
	},

	fetchItemUsers(dispatch, {itemId, allUsers}) {
		return ItemsHelper.fetchUsers({itemId}).then(response => {

			let allContributors = response.results;

			let owner = {};
			let contributors = [];
			allContributors.map(user => {
				let userObject = allUsers.find(userObject => userObject.userId === user.userId);
				if (userObject) {
					user = {...user, fullName: userObject.fullName, role: userObject.role};

					switch(user.permission) {
						case permissionTypes.OWNER:
							owner = user;
							break;
						case permissionTypes.CONTRIBUTOR:
							contributors.push(user);
							break;
					}
				}
			});

			dispatch({
				type: actionTypes.ITEM_USERS_LOADED,
				contributors,
				owner
			});
		});
	},

	askForContributorRights() {
		console.log('asked for contributor rights');
	}



};

export default PermissionsActionHelper;
