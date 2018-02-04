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
const _ = require('lodash');
const normalizeNewline = require('normalize-newline');
require('node-zip');
YAML = require('yamljs');
const fs = require('fs');
const util = require('./Utils.js');

/**
 * @module ContextData
 * @description  Use with "Given". Use ONLY for local testing when you know the value of the Item you want to use
 * instead of creating a new one.
 * @step Item {string} and version Id {string}
 **/
Given('Item {string} and version Id {string}', function (string, string2) {
	this.context.item.id = string;
	this.context.item.versionId = string2;
});
/**
 * @module ContextData
 * @exampleFile Example_ResponseData_CheckAndManipulation.feature
 * @description Response Data::<br>
 *     """<br>
 *         {jsonObject}<br>
 *             """<br>
 * @step  Use with "Given". Use ONLY for local testing, creates a response data object
 **/
Given('Response Data:', function (docString) {
	this.context.responseData = JSON.parse(docString);
});

/**
 * @module ContextData
 * @description Sets the server for the test. overrides configuration.
 * @step Server with IP
 **/
Given('Server host {string}', function (string) {
	this.setServer(string);
});

/**
 * @module ContextData
 * @description Copy a property from the response data to context Item/VLM/VSP data, example: vsp.componentId
 * @step I want to save on the context for {string} property {string} with value {string}
 **/
Then('I want to save on the context for {string} property {string} with value {string}', function(string, string1, string2)  {
	assert.equal(_.includes(['VLM', 'VSP', 'Item'], string), true);
	let val = _.get(this.context.responseData, string2);
	_.set(this.context, string1, val);
});
/**
 * @module ContextData
 * @description Copy a property from the response data to saved data on the context. Example: save newly generated IDs. Response data value can be from a path, xample: results[0].id
 * @exampleFile Example_Rest_Calls.feature
 * @step I want to save to property {string} from response data path {string}
 **/
Then('I want to copy to property {string} from response data path {string}', function(string, string2)  {
	let val = _.get(this.context.responseData, string2);
	_.set(this.context, string, val);
});
/**
 * @module ContextData
 * @description This will set the value of a saved property on the context
 * @exampleFile Example_Rest_Calls.feature
 * @step I want to set property {string} to value {string}
 **/
Then('I want to set property {string} to value {string}', function(string, string2)  {
	_.set(this.context, string, string2);
});

/**
 * @module ResponseData
 * @description Will check the output data for a property and a value. property can be a path (example: results[0].id)
 * @exampleFile Example_ResponseData_CheckAndManipulation.feature
 * @step I want to check property {string} for value {string}
 **/
Then('I want to check property {string} for value {string}', function(string, string2)  {
	assert.equal(_.get(this.context.responseData, string), string2);
});
/**
 * @module ResponseData
 * @description Will check the output data for a property and a integer. property can be a path (example: results[0].id)
 * @exampleFile Example_ResponseData_CheckAndManipulation.feature
 * @step I want to check property {string} for value {int}
 **/
Then('I want to check property {string} for value {int}', function(string, int)  {
	assert.equal(_.get(this.context.responseData, string), int);
});
/**
 * @module ResponseData
 * @description Will check the output data for a property and a boolean. property can be a path (example: results[0].id)
 * @exampleFile Example_ResponseData_CheckAndManipulation.feature
 * @step I want to check property {string} to be "True/False"
 **/
Then('I want to check property {string} to be {string}', function(string, string2)  {
	assert.equal(_.get(this.context.responseData, string), string2.toLowerCase());
});
/**
 * @module ResponseData
 * @description Will check the output data for a property and a boolean. property can be a path (example: results[0].id)
 * @exampleFile Example_ResponseData_CheckAndManipulation.feature
 * @step I want to check property {string} to have length {int}
 **/
Then('I want to check property {string} to have length {int}', function(string, intLength)  {
	let arrayProp = _.get(this.context.responseData, string);
	assert.equal(arrayProp.length, intLength);
});
/**
 * @module ResponseData
 * @description Will check the output data for a property and make sure it exists
 * @exampleFile Example_ResponseData_CheckAndManipulation.feature
 * @step I want to check property {string} exists
 **/
Then('I want to check property {string} exists', function(string)  {
	assert.equal(_.has(this.context.responseData, string), true);
});
/**
 * @module ResponseData
 * @description Will check the output data for a property and make sure it does not exist
 * @exampleFile Example_ResponseData_CheckAndManipulation.feature
 * @step I want to check property {string} does not exist
 **/
Then('I want to check property {string} does not exist', function(string)  {
	assert.equal(_.has(this.context.responseData, string), false);
});

/**
* @module ContextData
* @description Use during development to see what is on the context
 * @exampleFile Example_ResponseData_CheckAndManipulation.feature
* @step I want to print context data
**/
Then('I want to print the context data', function()  {
	console.log('------------ context ---------------');
	console.log(JSON.stringify(this.context, null, 2));
	console.log('--------------------------------------');
});
/**
 * @module ContextData
 * @description Set this in order to check that the following Rest call will not have response code 200
 * @exampleFile Example_Rest_Calls.feature
 * @step I want the following to fail
 **/
Then('I want the following to fail', function()  {
	this.context.shouldFail = true;
});

/**
 * @module ContextData
 * @description Set this in order to check that the following Rest call will have the error code on the return data
 * @exampleFile Example_VSP.feature
 * @step I want the following to fail with error code {string}
 **/
Then('I want the following to fail with error code {string}', function(string)  {
	this.context.shouldFail = true;
	this.context.errorCode = string;
});

/**
 * @module ZipData
 * @description Use this in order to extract a file from a zip file and to compare it to a local file (string comparison).
 * @exampleFile Example_VSP.feature
 * @step I want to compare the content of the entry {string} in the zip {string} with file {string}
 **/
Then ('I want to compare the content of the entry {string} in the zip {string} with file {string}', function (string, string2, string3) {
	let zipFile = fs.readFileSync(string2, 'binary');
	let zip = new JSZip(zipFile, {base64: false, checkCRC32: true});
	let fileData = zip.files[string]._data;
	let compareFileData = fs.readFileSync(string3, {encoding: 'ascii'});
	assert.equal(normalizeNewline(compareFileData), normalizeNewline(fileData));
});

/**
 * @module ZipData
 * @description Loads the yaml from zip file onto the context responseData as JSON for running checks on the output
 * @exampleFile Example_VSP.feature
 * @step I want to load the yaml content of the entry {string} in the zip {string} to context
 **/
Then ('I want to load the yaml content of the entry {string} in the zip {string} to context', function (string, string2, callback) {
	let zipFile = fs.readFileSync(string2, 'binary');
	let zip = new JSZip(zipFile, {base64: false, checkCRC32: true});
	let fileData = zip.files[string]._data;
	let nativeObject = YAML.parse(fileData);
	this.context.responseData = nativeObject;
	callback();
});


/**
 * @module ZipData
 * @description Loads the json from zip file onto the context responseData for running check son the output
 * @exampleFile Example_VSP.feature
 * @step I want to load the json content of the entry {string} in the zip {string} to context
 **/
When('I want to load the json content of the entry {string} in the zip {string} to context', function (string, string2, callback) {
	let zipFile = fs.readFileSync(string2, 'binary');
	let zip = new JSZip(zipFile, {base64: false, checkCRC32: true});
	let str = zip.files[string]._data;
	this.context.responseData = JSON.parse(str);
	callback();
});