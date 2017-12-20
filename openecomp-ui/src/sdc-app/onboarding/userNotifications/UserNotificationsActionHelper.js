import {actionTypes} from './UserNotificationsConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Configuration from 'sdc-app/config/Configuration.js';
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import WebSocketUtil, {websocketUrl} from 'nfvo-utils/WebSocketUtil.js';
import {actionsEnum as VersionControllerActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import ItemsHelper from 'sdc-app/common/helpers/ItemsHelper.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import MergeEditorActionHelper from 'sdc-app/common/merge/MergeEditorActionHelper.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {SyncStates} from 'sdc-app/common/merge/MergeEditorConstants.js';

function baseUrl() {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/notifications`;
}

function fetch() {
	return RestAPIUtil.fetch(baseUrl());
}

function updateNotification(notificationId) {
	return RestAPIUtil.put(`${baseUrl()}/${notificationId}`);
}

function updateLastSeenNotification(notificationId) {
	return RestAPIUtil.put(`${baseUrl()}/last-seen/${notificationId}`);
}

function loadPrevNotifications(lastScanned, endOfPage) {
	return RestAPIUtil.fetch(`${baseUrl()}?LAST_DELIVERED_EVENT_ID=${lastScanned}&END_OF_PAGE_EVENT_ID=${endOfPage}`);
}

const INITIAL_LAST_SCANNED = '00000000-0000-1000-8080-808080808080';

const UserNotificationsActionHelper = {
	notificationsFirstHandling(dispatch) {
		console.log('Websocket Url: ', websocketUrl);
		UserNotificationsActionHelper.fetchUserNotificationsList(dispatch).then(({lastScanned}) => {
			WebSocketUtil.open(websocketUrl, {lastScanned: lastScanned || INITIAL_LAST_SCANNED});
		});
	},

	fetchUserNotificationsList(dispatch) {
		return fetch().then(result => {
			dispatch({
				type: actionTypes.LOAD_NOTIFICATIONS,
				result
			});
			return Promise.resolve({lastScanned: result.lastScanned});
		});
	},

	loadPreviousNotifications(dispatch, {lastScanned, endOfPage}) {
		loadPrevNotifications(lastScanned, endOfPage).then(result => dispatch({
			type: actionTypes.LOAD_PREV_NOTIFICATIONS,
			result
		}));
	},

	notifyAboutConflicts(dispatch, {itemId, itemName, version, currentScreen}) {
		let {props} = currentScreen;
		let currentItemId = props.softwareProductId || props.licenseModelId;
		let currentVersion = props.version;
		if(currentItemId === itemId && currentVersion.id === version.id) {
			MergeEditorActionHelper.analyzeSyncResult(dispatch, {itemId, version}).then(() => ScreensHelper.loadScreen(dispatch, currentScreen));
		}
		else {
			dispatch({
				type: modalActionTypes.GLOBAL_MODAL_WARNING,
				data: {
					title: i18n('Conflicts'),
					msg: i18n('There are conflicts in {itemName} version {versionName} that you have to resolve', {itemName: itemName.toUpperCase(), versionName: version.versionName}),
					cancelButtonText: i18n('OK')
				}
			});
		}
	},

	syncItem(dispatch, {itemId, itemName, versionId, versionName, currentScreen}) {
		let version = {id: versionId, versionName};
		ItemsHelper.fetchVersion({itemId, versionId}).then(response => {
			let inMerge = response && response.state && response.state.synchronizationState === SyncStates.MERGE;
			if (!inMerge) {
				ItemsHelper.performVCAction({itemId, version, action: VersionControllerActionsEnum.SYNC}).then(() => {
					return ItemsHelper.fetchVersion({itemId, versionId}).then(response => {
						let inMerge = response && response.state && response.state.synchronizationState === SyncStates.MERGE;
						if (!inMerge) {
							return ScreensHelper.loadScreen(dispatch, currentScreen);
						}
						else {
							return this.notifyAboutConflicts(dispatch, {itemId, itemName, version, currentScreen});
						}
					});
				});
			}
			else {
				this.notifyAboutConflicts(dispatch, {itemId, itemName, version, currentScreen});
			}
		});
	},

	updateNotification(dispatch, {notificationForUpdate}) {
		updateNotification(notificationForUpdate.eventId).then(response => {
			if(response.status === 'Success' && Object.keys(response.errors).length === 0) {
				dispatch({
					type: actionTypes.UPDATE_READ_NOTIFICATION,
					notificationForUpdate
				});
			}
		});
	},

	updateLastSeenNotification(dispatch, {notificationId}) {
		updateLastSeenNotification(notificationId).then(response => {
			if (response.status === 'Success' && Object.keys(response.errors).length === 0) {
				dispatch({type: actionTypes.RESET_NEW_NOTIFICATIONS});
			}
		});
	}
};

export default UserNotificationsActionHelper;
