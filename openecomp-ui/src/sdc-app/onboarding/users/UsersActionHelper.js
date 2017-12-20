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
import {actionTypes} from './UsersConstants.js';

function getUserId() {
	let attApiHeaders = Configuration.get('ATTApiHeaders');
	let User = attApiHeaders && attApiHeaders.userId;
	let userId = User && User.value ? User.value : '';
	return userId;
}

function baseUrl() {
	const restATTPrefix = Configuration.get('restATTPrefix');
	return `${restATTPrefix}`;
}


function fetchUsersList() {
	const url = '/v1/user/users';
	return  RestAPIUtil.fetch(`${baseUrl()}${url}`);
}



const UsersActionHelper = {
	fetchUsersList(dispatch) {
		fetchUsersList().then(response => {
			dispatch({
				type: actionTypes.USERS_LIST_LOADED,
				usersList: response
			});

			let userId = getUserId();
			let userInfo = response.find(user => user.userId === userId);
			dispatch({
				type: actionTypes.GOT_USER_INFO,
				userInfo
			});

		});

	}
};

export default UsersActionHelper;
