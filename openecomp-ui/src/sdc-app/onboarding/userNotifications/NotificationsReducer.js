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

import {actionTypes} from './UserNotificationsConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.NOTIFICATION:
			let list = (state.notificationsList) ? state.notificationsList : [];
			const {notifications, lastScanned} = action.data;
			return {
				...state,
				lastScanned,
				notificationsList: [...notifications, ...list],
				numOfNotSeenNotifications: state.numOfNotSeenNotifications + notifications.length
			};
		case actionTypes.LOAD_NOTIFICATIONS:
			return {
				...state,
				...action.result,
				notificationsList: action.result.notifications,
				notifications: undefined
			};
		case actionTypes.LOAD_PREV_NOTIFICATIONS:
			const {notifications: prevNotifications, endOfPage: newEndOfPage} = action.result;
			return {
				...state,
				notificationsList: [
					...state.notificationsList,
					...prevNotifications
				],
				endOfPage: newEndOfPage
			};
		case actionTypes.UPDATE_READ_NOTIFICATION:
			let {notificationForUpdate} = action;
			notificationForUpdate = {...notificationForUpdate, read: true};
			const indexForEdit = state.notificationsList.findIndex(notification => notification.eventId === notificationForUpdate.eventId);
			return {
				...state,
				notificationsList: [
					...state.notificationsList.slice(0, indexForEdit),
					notificationForUpdate,
					...state.notificationsList.slice(indexForEdit + 1)
				]
			};
		case actionTypes.RESET_NEW_NOTIFICATIONS:
			return {
				...state,
				numOfNotSeenNotifications: 0
			};
		case actionTypes.TOGGLE_OVERLAY:
			return {
				...state,
				showNotificationsOverlay: action.showNotificationsOverlay
			};
		default:
			return state;
	}
};
