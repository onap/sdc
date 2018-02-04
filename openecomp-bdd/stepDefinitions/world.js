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
const config = require('../config.json');
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
		this.context = {}
		if (options.parameters && options.parameters.server) {
			this.context.server =  options.parameters.server;
		} else if (process.env.SERVER) {
			this.context.server = process.env.SERVER;
		} else {
			this.context.server = config.server;
		}
		this.context.onboarding_server = (config.protocol + '://' + this.context.server + ':' + config.port + '/' + config.prefix);
		this.context.vf_server = (config.protocol + '://' + this.context.server + ':' + config.port + '/' + config.vf_prefix);


		this.context.headers = {};
		this.context.headers['USER_ID']  = 'cs0008';

		this.context.vlm = {id: null, versionId: null};
		this.context.vsp = {id: null, versionId: null};
		this.context.item = {id: null, versionId: null, componentId: null};

		this.context.shouldFail = false;
		this.context.errorCode = null;
		this.context.inputData = null;
		this.context.responseData = null;

		this.setServer = function(server) {
			this.context.onboarding_server = (config.protocol + '://' +server + ':' + config.port + '/' + config.prefix);
			this.context.vf_server = (config.protocol + '://' +server + ':' + config.port + '/' + config.vf_prefix);
		}

		setDefaultTimeout(60 * 1000);
	}
}


setWorldConstructor(CustomWorld)
