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
import configData from './config.json';

class Configuration {

	get(key) {
		return configData[key];
	}

	set(key, value) {
		var prev = configData[key];
		configData[key] = value;
		return prev;
	}

	setATTApiRoot(ATTApiRoot) {
		let restATTPrefix = ATTApiRoot,
			restPrefix = ATTApiRoot.replace(/\/feProxy\b[^:]*$/, '/feProxy/onboarding-api');

		this.set('restPrefix', restPrefix);
		this.set('restATTPrefix', restATTPrefix);
	}

	setATTApiHeaders(ATTApiHeaders) {
		this.set('ATTApiHeaders', ATTApiHeaders);

		let {userId: {value: UserID} = {}} = ATTApiHeaders;
		this.set('UserID', UserID);
	}
}

const configuration = new Configuration();

(function setDefaultRestPrefixes(configuration) {
	configuration.set('restPrefix', configuration.get('defaultRestPrefix'));
	configuration.set('restATTPrefix', configuration.get('defaultRestATTPrefix'));
	configuration.set('appContextPath', configuration.get('appContextPath'));
})(configuration);
(function setDefaultWebsocketConfig(configuration) {
	let websocketPort = configuration.get('defaultWebsocketPort');
	if (DEBUG) {
		websocketPort = configuration.get('defaultDebugWebsocketPort');
	}
	configuration.set('websocketPort', websocketPort);
	configuration.set('websocketPath', configuration.get('defaultWebsocketPath'));
})(configuration);

export default configuration;
