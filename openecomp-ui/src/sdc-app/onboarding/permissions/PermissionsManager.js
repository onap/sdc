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
import PermissionsManager from './PermissionsManager.jsx';
import PermissionsActionHelper from './PermissionsActionHelper.js';

export const mapStateToProps = ({versionsPage, users: {usersList, userInfo}}) => {
	let {permissions} = versionsPage;

	return {
		users: usersList,
		userInfo,
		owner: permissions.owner,
		itemUsers: permissions.contributors
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onCancel: () => PermissionsActionHelper.closePermissionManager(dispatch),
		onSubmit: ({itemId, addedUsersIds, removedUsersIds, allUsers, newOwnerId}) => {
			return PermissionsActionHelper.saveItemUsers(dispatch,{itemId, addedUsersIds, removedUsersIds, allUsers}).then(() => {
				return newOwnerId ? PermissionsActionHelper.changeOwner(dispatch, {itemId, newOwnerId, allUsers}) : Promise.resolve();
			}).then(() => PermissionsActionHelper.closePermissionManager(dispatch));
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(PermissionsManager);
