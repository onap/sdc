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

var exec = require('child_process');
var prompt = require('prompt');
var fs = require('fs');


function runNpm(target, dir) {
	console.log('\n---> npm ' + target);
	let options = {stdio:[0,1,2]};
	if (dir) options.cwd = dir;
	exec.execSync("npm " + target,options);
}

function npmInstallAll() {
	setNpmconfig();
	if (!fs.existsSync('../dox-sequence-diagram-ui/node_modules')) {
		console.log('--> first time installing dox-sequence-diagram-ui');
		runNpm('install', '../dox-sequence-diagram-ui');
	};
	runNpm('install');
	// just to make sure restful js is installed properly
	runNpm('install jquery', 'node_modules/restful-js');
}

function getDevConfig() {
	var content=fs.readFileSync('./devConfig.json');
	var data=JSON.parse(content);
	console.log('Current ATT server is set to: ' + data.proxyATTTarget);
	if (!data.proxyTarget) {
		console.log('Current onboarding server defaults to the ATT server');
	} else {
		console.log('Current onboarding server set to: ' + data.proxyTarget);
	}
	return data;
}

function setNpmconfig() {
	exec.execSync("npm config set proxy http://genproxy.amdocs.com:8080");
	exec.execSync("npm config set https_proxy http://genproxy.amdocs.com:8080");
}

// getting the run details before starting to work
prompt.start();
prompt.get([{
		name:'runType',
		type:'number',
		default:1,
		description: 'Choose run: 1-test and build, 2- run frontend server '
	}], function (err, result) {
	if (result.runType === 2) {
		console.log('--> Reading the configuration for the local server');
		if (!fs.existsSync('./devConfig.json')) {
			console.log('First time - setting up the devConfig.json file');
			fs.writeFileSync('./devConfig.json', fs.readFileSync('./devConfig.defaults.json'));
		}
		let data = getDevConfig();
		let attProxyField = {
			name:'attProxyTarget',
			description:'ATT server'
		};
		let proxyField = {
			name:'proxyTarget',
			description:'onboarding server, \'null\' to reset'
		};
		if (data.proxyATTTarget) attProxyField.default = data.proxyATTTarget;
		if (data.proxyTarget) proxyField.default = data.proxyTarget;
		prompt.get([ attProxyField, proxyField], function (err,result) {
				data.proxyATTTarget = result.attProxyTarget;
				if(result.proxyTarget) {
					if (result.proxyTarget === 'null') {
						if (data.proxyTarget) delete data.proxyTarget;
					} else {
						data.proxyTarget = result.proxyTarget;
					}
				}
				fs.writeFileSync('./devConfig.json', JSON.stringify(data, null, 2));
				getDevConfig();
				console.log('FE server will be answering on: http://localhost:9000/sdc1/proxy-designer1#/onboardVendor');
				npmInstallAll();
				runNpm("start");
			}
		);
	} else {
		npmInstallAll();
		runNpm("run build");
		runNpm("run test");
	}
});

