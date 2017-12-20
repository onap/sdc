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

import React from 'react';
import PropTypes from 'prop-types';
import enhanceWithClickOutside from 'react-click-outside';
import classnames from 'classnames';
import {connect} from 'react-redux';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import Overlay from 'nfvo-components/overlay/Overlay.jsx';
import UserNotifications from 'sdc-app/onboarding/userNotifications/UserNotifications.jsx';
import UserNotificationsActionHelper from 'sdc-app/onboarding/userNotifications/UserNotificationsActionHelper.js';
import {actionTypes} from './UserNotificationsConstants.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';

const mapStateToProps = ({currentScreen, notifications, users: {usersList}}) => {
	return {currentScreen, notifications, usersList};
};

const mapActionToProps = (dispatch) => {
	return {
		resetNewNotifications: notificationId => UserNotificationsActionHelper.updateLastSeenNotification(dispatch, {notificationId}),
		toggleOverlay: ({showNotificationsOverlay}) => dispatch({type: actionTypes.TOGGLE_OVERLAY, showNotificationsOverlay}),
		onLoadPrevNotifications: (lastScanned, endOfPage) => UserNotificationsActionHelper.loadPreviousNotifications(dispatch, {lastScanned, endOfPage}),
		onSync: ({itemId, itemName, versionId, versionName, currentScreen}) =>  UserNotificationsActionHelper.syncItem(dispatch, {itemId, itemName, versionId, versionName, currentScreen}),
		updateNotification: notificationForUpdate => UserNotificationsActionHelper.updateNotification(dispatch, {notificationForUpdate}),
		onLoadItemsLists: () => OnboardingActionHelper.loadItemsLists(dispatch)
	};
};


class NotificationsView extends React.Component {

	static propTypes = {
		currentScreen: PropTypes.object,
		notifications: PropTypes.object,
		resetNewNotifications: PropTypes.func,
		toggleOverlay: PropTypes.func,
		onLoadPrevNotifications: PropTypes.func,
		onSync: PropTypes.func,
		updateNotification: PropTypes.func,
		onLoadItemsLists: PropTypes.func
	};

	render() {
		const {usersList, notifications, onLoadPrevNotifications, onSync, updateNotification, onLoadItemsLists, currentScreen} = this.props;
		const {notificationsList, numOfNotSeenNotifications, showNotificationsOverlay, lastScanned, endOfPage} = notifications;

		return (
			<div className='onboarding-notifications'>
				<div className='notifications-icon' onClick={() => this.onNotificationIconClick()}>
					<SVGIcon name={numOfNotSeenNotifications > 0 ? 'notificationFullBell' : 'notificationBell'} color={numOfNotSeenNotifications > 0 ? 'primary' : ''}/>
					<div className={classnames('notifications-count', {'hidden-count': numOfNotSeenNotifications === 0})}>
							{numOfNotSeenNotifications}
					</div>
				</div>
				{showNotificationsOverlay &&
					<Overlay>
						<UserNotifications notificationsList={notificationsList} usersList={usersList} lastScanned={lastScanned} endOfPage={endOfPage}
							onLoadPrevNotifications={onLoadPrevNotifications} onSync={onSync} updateNotification={updateNotification} onLoadItemsLists={onLoadItemsLists}
							currentScreen={currentScreen}/>
					</Overlay>
				}
			</div>
		);
	}

	handleClickOutside() {
		const {notifications: {showNotificationsOverlay}} = this.props;
		if(showNotificationsOverlay) {
			this.onCloseOverlay();
		}
	}

	onNotificationIconClick() {
		const {notifications: {showNotificationsOverlay}, toggleOverlay} = this.props;
		if (showNotificationsOverlay) {
			this.onCloseOverlay();
		} else {
			toggleOverlay({showNotificationsOverlay: true});
		}
	}

	onCloseOverlay() {
		const {notifications: {numOfNotSeenNotifications, lastScanned}, resetNewNotifications, toggleOverlay} = this.props;
		if (numOfNotSeenNotifications) {
			resetNewNotifications(lastScanned);
		}
		toggleOverlay({showNotificationsOverlay: false});
	}
}

export default connect(mapStateToProps, mapActionToProps)(enhanceWithClickOutside(NotificationsView));
