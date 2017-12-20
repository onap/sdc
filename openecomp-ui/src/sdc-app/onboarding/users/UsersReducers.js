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

import {actionTypes} from './UsersConstants.js';
import {combineReducers} from 'redux';

function usersList (state = [], action) {
	switch (action.type) {
		case (actionTypes.USERS_LIST_LOADED):
			return [...action.usersList];
		default:
			return state;
	}
};

function userInfo (state = {}, action) {
	switch (action.type) {
		case (actionTypes.GOT_USER_INFO):
			return action.userInfo;
		default:
			return state;
	}
}


export default combineReducers({
	usersList: usersList,
	userInfo: userInfo
});