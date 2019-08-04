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
const { setWorldConstructor } = require('cucumber');
const _ = require('lodash');
const findUp = require('find-up');



let configPath = findUp.sync('config.json');
configPath = configPath.replace(/\\/g, '/');

let config = require(configPath);
let localConfig = {};
try {
	let devConfigPath = findUp.sync('devConfig.json');
	devConfigPath = devConfigPath.replace(/\\/g, '/');
	localConfig = require(devConfigPath);
} catch (e) {
	try {
		let envdir = findUp.sync('environments', {type: 'directory'});
		envdir = envdir.replace(/\\/g, '/');
		localConfig = require(envdir + '/dockerConfig.json');
	} catch (e) {
		console.error("no env configuration was found!");
	}
}

config = _.merge(config, localConfig);
var {setDefaultTimeout} = require('cucumber');


/**
 * @module Context
 * @description Context that is used per feature file and can be accessed as 'this.context' in all steps.<Br>
 *     This class can be extended in order to add additional configurations.
 *<Br>
 * Contains the following items:<br>
 * <li>this.context.server <ul>REST server and onboarding prefix including version. set either in configuration file or from the command line or SERVER environment variable</ul>
 * <li>this.context.item <ul>When a VLM or VSP has been created, this has the an id and versionId set to the correct IDs.</ul>
 * <li>this.context <ul>Object with properties that were saved in the steps.</ul>
 * <li>this.context.inputdata <ul><b>Automatically updated with the last responseData from the Rest call</b><br>Object with properties that were prepares in the steps.</ul>
 * <li>this.context.responseData <ul>Response from the last REST call.</ul>
 **/
class CustomWorld {
	constructor(options) {
		this.context = {};
		this.context.headers = {};
		let typeName;

		this.context.defaultServerType = 'main';
		for (typeName in config) {
			this.context.headers[typeName] = {};
			if (config[typeName].user) {
				this.context.headers[typeName]['USER_ID'] = config[typeName].user;
			}
			// adding additional headers
			if (config[typeName].additionalHeaders) {
				_.assign(this.context.headers[typeName] , config[typeName].additionalHeaders);
			}
			if (config[typeName].isDefault !== undefined && config[typeName].isDefault) {
				this.context.defaultServerType = typeName;
			}
		}
		this.context.item = {id: null, versionId: null, componentId: null};
		// adding the default items that should also be initialized
		if (config.initData) {
			_.assign(this.context, config.initData);
		}

		this.context.shouldFail = false;
		this.context.errorCode = null;
		this.context.inputData = null;
		this.context.responseData = null;


		this.config = config;

		let context = this.context;
		this.context.getUrlForType = (function(type) {
			var _server = context.server;
			var _config = config;
			return function(type) {
				let typeData = _config[type];
				let _url = typeData.protocol + '://' +
					typeData.server + ':' +
					typeData.port + '/' +
					typeData.prefix;
				return _url;
			}
		})();
		setDefaultTimeout(60 * 1000);
	}
}
setWorldConstructor(CustomWorld);
