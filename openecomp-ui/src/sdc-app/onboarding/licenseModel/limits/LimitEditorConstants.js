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
		{enum: 'Software_Instances_Count', title: 'Software Instances'},
		{enum: 'Core', title: 'Core'},
		{enum: 'CPU', title: 'CPU'},
		{enum: 'Trunks', title: 'Trunks'},
		{enum: 'User', title: 'User'},
		{enum: 'Subscribers', title: 'Subscribers'},
		{enum: 'Tenants', title: 'Tenants'},
		{enum: 'Tokens', title: 'Tokens'},
		{enum: 'Seats', title: 'Seats'},
		{enum: 'Units_TB', title: 'Units-TB'},
		{enum: 'Units_GB', title: 'Units-GB'},
		{enum: 'Units_MB', title: 'Units-MB'}
	],
	AGGREGATION_FUNCTION: [
		{enum: '', title: i18n('please select…')},
		{enum: 'Peak', title: 'Peak'},
		{enum: 'Average', title: 'Average'}
	],
	TIME: [
		{enum: '', title: i18n('please select…')},
		{enum: 'Hour', title: 'Hour'},
		{enum: 'Day', title: 'Day'},
		{enum: 'Month', title: 'Month'}
	]
	
};

export const limitType = {
	SERVICE_PROVIDER: 'ServiceProvider',
	VENDOR: 'Vendor'
};

export const defaultState = {
	LIMITS_EDITOR_DATA: {}
};

export const NEW_LIMIT_TEMP_ID = 'NEW_LIMIT_TEMP_ID';