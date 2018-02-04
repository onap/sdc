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


/**
 * @module VSP
 * @description Creates a new VSP with a random name and saves the id and versionId on the context item object and the context vsp object<br>
 *     Input data will be taken from the 'resources/json/createVSP.json' file.
 *     Vendor id and name are taken from the vlm on the context (requires a VLM to be created first).
 *     @exampleFile Example_VSP.feature
 * @step I want to create a VSP with onboarding type {string}
 **/
When('I want to create a VSP with onboarding type {string}', function(string)  {
	let inputData = util.getJSONFromFile('resources/json/createVSP.json');
	inputData.onboardingMethod = string;
	inputData.vendorName = this.context.vlm.name;
	inputData.vendorId = this.context.vlm.id;
	inputData.name = util.random();
	let path = '/vendor-software-products';
	return util.request(this.context, 'POST', path, inputData).then(result => {
		this.context.item = {id : result.data.itemId, versionId: result.data.version.id};
		this.context.vsp = {id : result.data.itemId, versionId: result.data.version.id};
	});
});

/**
 * @module VSP
 * @description Creates a new VSP with the 'NetowrkPackage' onboarding type and with a random name and saves the id and versionId on the context item object and the context vsp object<br>
 *     Input data will be taken from the 'resources/json/createVSP.json' file.
 *     Vendor id and name are taken from the vlm on the context (requires a VLM to be created first).
 *     @exampleFile Example_VSP.feature
 * @step I want to create a VSP with onboarding type {string}
 **/
When('I want to create a VSP', function()  {
	let inputData = util.getJSONFromFile('resources/json/createVSP.json');
	inputData.vendorName = this.context.vlm.name;
	inputData.vendorId = this.context.vlm.id;
	inputData.name = util.random();
	let path = '/vendor-software-products';
	return util.request(this.context, 'POST', path, inputData).then(result => {
		this.context.item = {id : result.data.itemId, versionId: result.data.version.id};
		this.context.vsp = {id : result.data.itemId, versionId: result.data.version.id};
	});
});


/**
 * @module VSP
 * @exampleFile Example_VSP.feature
 * @step I want to submit this VSP
 **/
Then('I want to submit this VSP', function () {
	let path = '/vendor-software-products/' + this.context.item.id + '/versions/' + this.context.item.versionId + '/actions';
	let inputData = {action: 'Submit'};
	return util.request(this.context, 'PUT', path, inputData);
});

/**
 * @module VSP
 * @exampleFile Example_VSP.feature
 * @step I want to package this VSP
 **/
Then('I want to package this VSP', function () {
	let path = '/vendor-software-products/' + this.context.item.id + '/versions/' + this.context.item.versionId + '/actions';
	let inputData = {action: 'Create_Package'};
	return util.request(this.context, 'PUT', path, inputData);
});

/**
 * @module VSP
 * @description Adds a component to the current item
 * @exampleFile Example_VSP.feature
 * @step I want to add a component
 **/
Then('I want to add a component', function () {
	let path = '/vendor-software-products/' + this.context.item.id + '/versions/' + this.context.item.versionId + '/components';
	let inputData = {name: 'Cucumber Name', displayName: 'Cucumber', description: 'Cucumber Description'};
	return util.request(this.context, 'POST', path, inputData).then(result => {
		this.context.componentId = result.data.vfcId;
	});
});


/**
 * @module VSP
 * @description Downloads the packaged file for this component to the given path
 * @exampleFile Example_VSP.feature
 * @step I want to get the package for this Item to path {string}
 **/
When('I want to get the package for this Item to path {string}', function (string, callback) {
	let path =  '/vendor-software-products/packages/' + this.context.item.id;
	return [util.download(this.context, path, string, callback)];
});