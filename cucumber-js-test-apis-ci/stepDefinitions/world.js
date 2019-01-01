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

let config = require('../config.json');
let localConfig = {};
try {
	localConfig = require('../devConfig.json');
} catch (e) {
	try {
		localConfig = require('../environments/dockerConfig.json');
	} catch (e) {
		console.error("no env configuration was found!");
	}
}

config = _.merge(config, localConfig);
var {setDefaultTimeout} = require('cucumber');


/**
 * @module Context
 * @description Context that is used per feature file and can be accessed as 'this.context' in all steps.<Br>
 *<Br>
 * Contains the following items:<br>
 * <li>this.context.server <ul>REST server and onboarding prefix including version. set either in configuration file or from the command line or SERVER environment variable</ul>
 * <li>this.context.vlm <ul>When a VLM has been created, this has the an id and versionId set to the correct IDs.</ul>
 * <li>this.context.vsp <ul>When a VSP has been created, this has the an id and versionId and componentId set to the correct IDs.</ul>
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
		for (typeName in config) {
			this.context.headers[typeName] = {};
			if (config[typeName].user) {
				this.context.headers[typeName]['USER_ID'] = config[typeName].user;
			}
		}

		this.context.vlm = {id: null, versionId: null};
		this.context.vsp = {id: null, versionId: null};
		this.context.item = {id: null, versionId: null, componentId: null};

		this.context.shouldFail = false;
		this.context.errorCode = null;
		this.context.inputData = null;
		this.context.responseData = null;

		this.context.defaultServerType = 'onboarding';

		this.config = config;

		let context = this.context;
		this.context.getUrlForType = (function(type) {
			var _server = context.server;
			var _config = config;
			return function(type) {
				let typeData = _config[type];
				let _url = _config.protocol + '://' +
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
