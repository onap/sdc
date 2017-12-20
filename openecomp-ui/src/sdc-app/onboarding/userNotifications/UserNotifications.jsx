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
import ReactDOM from 'react-dom';
import classnames from 'classnames';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {notificationType} from './UserNotificationsConstants.js';
import ShowMore from 'react-show-more';

const Notification = ({notification, users, onActionClicked, getNotificationTypeDesc}) => {
	const {eventType, read, eventAttributes, dateTime} = notification;
	const {itemName, userId, description, versionName, permission, granted} = eventAttributes;
	const {fullName: userName} = users.find(user => user.userId === userId);
	return (
		<div className={classnames('notification', {'unread': !read})}>
			<div className='notification-data'>
				<div className='item-name'>
					{itemName}
					{versionName && <span>&nbsp;&nbsp;&nbsp;v{versionName}</span>}
					{!read && <div className='unread-circle-icon'></div> }
				</div>
				<div className='flex-items'>
					<div className='type'>{getNotificationTypeDesc(eventType, permission, granted)}</div>
					<div className='separator'/>
					<div className='user-name'>{`${i18n('By')} ${userName}`}</div>
				</div>
				{(description || versionName) && <div className='description'>
					{description && <ShowMore anchorClass='more-less' lines={2} more={i18n('More')} less={i18n('Less')}>
						{description}
					</ShowMore>}
					{eventType === notificationType.ITEM_CHANGED.SUBMIT &&
						<div>
							<div>{i18n('Version {versionName} was submitted.', {versionName: versionName})}</div>
						</div>
					}
				</div>
				}
				<div className='date'>{dateTime}</div>
			</div>
			<div className='notification-action'>
				<div className={classnames('action-button', {'hidden': read})} onClick={() => onActionClicked(notification)}>
					{eventType === notificationType.PERMISSION_CHANGED ? i18n('Accept') : i18n('Sync')}
				</div>
			</div>
		</div>
	);
};

function getNotificationTypeDesc(eventType, permission, granted) {
	switch (eventType) {
		case notificationType.PERMISSION_CHANGED:
			return i18n('Permission {granted}: {permission}', {granted: granted ? 'Granted' : 'Taken', permission: permission});
		case notificationType.ITEM_CHANGED.COMMIT:
			return i18n('Your Copy Is Out Of Sync');
		case notificationType.ITEM_CHANGED.SUBMIT:
			return i18n('Version Submitted');
	}
}

class UserNotifications extends React.Component {

	static propTypes = {
		currentScreen: PropTypes.object,
		notificationsList: PropTypes.array,
		usersList: PropTypes.array,
		lastScanned: PropTypes.string,
		endOfPage:PropTypes.string,
		onLoadPrevNotifications: PropTypes.func,
		onSync: PropTypes.func,
		updateNotification: PropTypes.func,
		onLoadItemsLists: PropTypes.func
	};

	render() {
		const {notificationsList = [], usersList, lastScanned, endOfPage} = this.props;

		return (
			<div className='user-notifications'>
				<div className='notifications-title'>{i18n('Notifications')}</div>
				<div className='notifications-list' ref='notificationList' onScroll={() => this.loadPrevNotifications(lastScanned, endOfPage)}>
				{
					notificationsList.map(notification => (
						<Notification key={notification.eventId} notification={notification} users={usersList}
							onActionClicked={notification => this.onActionClicked(notification)}
							getNotificationTypeDesc={getNotificationTypeDesc}/>))
				}
				</div>
			</div>
		);
	}

	onActionClicked(notification) {
		const {onSync, updateNotification, currentScreen, onLoadItemsLists} = this.props;
		const {eventType, eventAttributes: {itemId, itemName, versionId, versionName}} = notification;
		if(eventType !== notificationType.PERMISSION_CHANGED) {
			onSync({itemId, itemName, versionId, versionName, currentScreen});
		}
		else {
			onLoadItemsLists();
		}
		updateNotification(notification);
	}

	loadPrevNotifications(lastScanned, endOfPage) {
		if(endOfPage && lastScanned) {
			let element = ReactDOM.findDOMNode(this.refs['notificationList']);
			const {onLoadPrevNotifications} = this.props;

			if (element && element.clientHeight + element.scrollTop === element.scrollHeight) {
				onLoadPrevNotifications(lastScanned, endOfPage);
			}
		}
	}
}

export default UserNotifications;
