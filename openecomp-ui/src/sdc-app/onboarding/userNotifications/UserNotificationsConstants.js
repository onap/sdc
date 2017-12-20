
import keyMirror from 'nfvo-utils/KeyMirror.js';

export const actionTypes = keyMirror({
	NOTIFICATION: null,
	LOAD_NOTIFICATIONS: null,
	LOAD_PREV_NOTIFICATIONS: null,
	UPDATE_READ_NOTIFICATION: null,
	RESET_NEW_NOTIFICATIONS: null,
	TOGGLE_OVERLAY: null
});

export const notificationType = keyMirror({
	PERMISSION_CHANGED: 'PermissionChanged',
	ITEM_CHANGED: {
		COMMIT: 'commit',
		SUBMIT: 'submit'
	}
});