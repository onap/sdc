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
const {Then, When, Given} = require('cucumber');
const assert = require('assert');
const util = require('./Utils.js');

/**
 * @module VLM
 * @description Creates a new VLM with a random name and saves the id and versionId on the context item object and the context vlm object<br>
 *     Input data  will be taken from the 'resources/json/createVLM.json' file.
 *@exampleFile Example_VLM.feature
 * @step I want to create a VLM
 **/
When('I want to create a VLM', function()  {
	let inputData = util.getJSONFromFile('resources/json/createVLM.json');
	inputData.vendorName = util.random();
	let path = '/vendor-license-models';
	return util.request(this.context, 'POST', path, inputData).then(result => {
		this.context.item ={id : result.data.itemId, versionId: result.data.version.id};
		this.context.vlm = {id : result.data.itemId, name : inputData.vendorName};
	});
});

/**
 * @module VLM
 * @exampleFile Example_VLM.feature
 * @step I want to submit this VLM
 **/
Then('I want to submit this VLM', function()  {
	let inputData = {action: 'Submit'};
	let path = '/vendor-license-models/' + this.context.item.id + '/versions/' + this.context.item.versionId + '/actions';
	return util.request(this.context, 'PUT', path, inputData);
});

