/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */
'use strict'

const fs = require('fs');

var pathToRoot = process.env.TESTS_BASE;
if (!pathToRoot.endsWith("/")) {
	pathToRoot += "/";
}
var envConfig = require(pathToRoot + 'config.json');
var protocol = (process.env.PROTOCOL !== undefined) ? process.env.PROTOCOL : 'https';

try {
	envConfig = require(pathToRoot + 'environments/dockerConfig.json');
} catch (e) {
}

function run() {
	var inputArgs = process.argv.slice(2);
	let changeConfig = false;
	if (process.env.K8S_CONF_PATH !== undefined) {
		console.log('updating with kubernetes services');
		let k8sConfig = require(pathToRoot + process.env.K8S_CONF_PATH);
		mapK8sPod2Docker(k8sConfig, inputArgs[0], inputArgs[1]);
		changeConfig = true;
	} else {
		console.log('not updating at all');
	}
	if (changeConfig) {
		let data = JSON.stringify(envConfig, null, 2);
		console.log('writing config file: ' + pathToRoot+'environments/dockerConfig.json');
		console.log(data);
		fs.writeFileSync(pathToRoot+'environments/dockerConfig.json', data);
	}
}

function mapK8sPod2Docker(k8sConfig, id, k8sid) {
	let item = k8sConfig.items.find(item => {
		if (item.spec !== undefined && item.spec.ports !== undefined) {
			let spec = item.spec.ports.find(port => {
				if (port.name === k8sid) {
					return true;
				}
			});
			return (spec !== undefined);
		} else {
			return false;
		}
	});

	item.spec.ports.forEach(port => {
		if (port.name === k8sid) {
			envConfig[id].port = port.nodePort;
			let rancherData = JSON.parse(item.metadata.annotations["field.cattle.io/publicEndpoints"]);
			let address = rancherData.find(address => {
				return address.port === port.nodePort;
			});
			envConfig[id].port = address.port;
			envConfig[id].server = address.addresses[0];
			envConfig[id].protocol = protocol;
			envConfig[id].user = process.env.SDC_USER_ID;
		}
	});

}

run();
