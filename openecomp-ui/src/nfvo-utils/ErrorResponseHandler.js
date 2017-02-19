/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import store from 'sdc-app/AppStore.js';
import NotificationConstants from 'nfvo-components/notifications/NotificationConstants.js';

function showVariablesInMessage(variables, msg) {
	let regex;
	variables.forEach((value, index) => {
		value = value.replace(';', ',');
		regex = new RegExp('\'\%' + (index + 1) + '\'');
		msg = msg.replace(regex, value);
	});
	return msg;
}

function parseATTExceptionObject(responseJSON) {
	let title, msg;
	if (responseJSON.requestError && responseJSON.requestError.policyException) {
		title = 'Error: ' + responseJSON.requestError.policyException.messageId;
		msg = responseJSON.requestError.policyException.text;
	}
	else if (responseJSON.requestError && responseJSON.requestError.serviceException) {
		title = 'Error: ' + responseJSON.requestError.serviceException.messageId;
		msg = responseJSON.requestError.serviceException.text;
		let {variables} = responseJSON.requestError.serviceException;
		if (variables) {
			msg = showVariablesInMessage(variables, msg);
		}
	}
	else {
		title = responseJSON.status;
		msg = responseJSON.message;
	}
	return {title, msg};
}

var errorResponseHandler = (xhr/*, textStatus, errorThrown*/) => {
	let errorData;
	if (xhr.responseJSON) {
		errorData = parseATTExceptionObject(xhr.responseJSON);
	}
	else {
		errorData = {
			title: xhr.statusText,
			msg: xhr.responseText
		};
	}
	store.dispatch({
		type: NotificationConstants.NOTIFY_ERROR,
		data: {...errorData}
	});
};

export default errorResponseHandler;
