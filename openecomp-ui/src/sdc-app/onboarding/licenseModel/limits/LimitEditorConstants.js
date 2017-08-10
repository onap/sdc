import keyMirror from 'nfvo-utils/KeyMirror.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
// import InputOptions, {other as optionInputOther} from 'nfvo-components/input/inputOptions/InputOptions.jsx';

export const actionTypes = keyMirror({
	OPEN: null,
	CLOSE: null,
	DATA_CHANGED: null,
});

export const LIMITS_FORM_NAME = 'LIMITSFORM';

export const selectValues = {
	METRIC: [
		{enum: '', title: i18n('please select…')},
		{enum: 'BWTH', title: 'BWTH'},
		{enum: 'Country', title: 'Country'},
		{enum: 'Session', title: 'Session'},
		{enum: 'LoB', title: 'LoB'},
		{enum: 'Site', title: 'Site'},
		{enum: 'Usage', title: 'Usage'}
	],
	UNIT: [
		{enum: '', title: i18n('please select…')},
		{enum: 'trunk', title: 'Trunks'},
		{enum: 'user', title: 'Users'},
		{enum: 'subscriber', title: 'Subscribers'},
		{enum: 'session', title: 'Sessions'},
		{enum: 'tenant', title: 'Tenants'},
		{enum: 'token', title: 'Tokens'},
		{enum: 'seats', title: 'Seats'},
		{enum: 'TB', title: 'TB'},
		{enum: 'GB', title: 'GB'},
		{enum: 'MB', title: 'MB'}
	],
	AGGREGATION_FUNCTION: [
		{enum: '', title: i18n('please select…')},
		{enum: 'Peak', title: 'Peak'},
		{enum: 'Average', title: 'Average'}
	],
	TIME: [
		{enum: '', title: i18n('please select…')},
		{enum: 'Day', title: 'Day'},
		{enum: 'Month', title: 'Month'},
		{enum: 'Hour', title: 'Hour'},
		{enum: 'Minute', title: 'Minute'},
		{enum: 'Second', title: 'Second'},
		{enum: 'Milli-Second', title: 'Milli-Second'}
	]

};

export const limitType = {
	SERVICE_PROVIDER: 'ServiceProvider',
	VENDOR: 'Vendor'
};

export const defaultState = {
	LIMITS_EDITOR_DATA: {
		metric: {choice: '', other: ''},
	}
};

export const NEW_LIMIT_TEMP_ID = 'NEW_LIMIT_TEMP_ID';
