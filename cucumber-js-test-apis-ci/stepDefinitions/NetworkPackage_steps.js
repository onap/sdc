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
const {Then, When} = require('cucumber');
const assert = require('assert');
const util = require('./Utils.js');
const _ = require('lodash');
const fs = require('fs');
require('node-zip');

/**
 * @module NetworkPackage
 * @description Uploads the NetworkPackage file to the VSP on the context
 * @exampleFile Example_HEAT.feature
 * @step I want to upload a NetworkPackage for this VSP from path {string}
 **/
Then('I want to upload a NetworkPackage for this VSP from path {string}', function (string) {
	let path =  '/vendor-software-products/' + this.context.item.id + '/versions/' + this.context.item.versionId + '/orchestration-template-candidate';
	return util.request(this.context, 'POST', path, string, true);
});

/**
 * @module NetworkPackage
 * @description Downloads the network package to disk
 * @exampleFile Example_HEAT.feature
 * @step I want to download the NetworkPackage for this VSP to path {string}
 **/
When('I want to download the NetworkPackage for this VSP to path {string}', function (string, callback) {
	let path =  '/vendor-software-products/' + this.context.item.id + '/versions/' + this.context.item.versionId + '/orchestration-template-candidate';
	return [util.download(this.context, path, string, callback)];
});

/**
 * @module NetworkPackage
 * @description Processes the NetworkPackage file on the server
 * @exampleFile Example_HEAT.feature
 * @step I want to process the NetworkPackage file for this VSP
 **/
Then('I want to process the NetworkPackage file for this VSP', function () {
	let path = '/vendor-software-products/' + this.context.item.id + '/versions/' + this.context.item.versionId + '/orchestration-template-candidate/process';
	return util.request(this.context, 'PUT', path, this.context.inputData);
});
