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
const {When} = require('cucumber');
const _ = require('lodash');
const util = require('./Utils.js');
_.templateSettings.interpolate = /{([\s\S]+?)}/g;

function getPath(path, context) {
	let compiled = _.template(path);
	return compiled(context);
}
/**
 * @module Rest_Calls
 * @description makes a GET request to the given path (path is appended after the "onboarding-api/v1.0" prefix)<br>
 * @exampleFile Example_Rest_Calls.feature
 * @step I want to get path {string}
 **/
When('I want to get path {string}', function(string)  {
	let path = getPath(string, this.context);
	return util.request(this.context, 'GET', path);
});

/**
 * @module Rest_Calls
 * @description makes a DELETE request to the given path and appends the saved property (path is appended after the "onboarding-api/v1.0" prefix)<br>
 * @exampleFile Example_Rest_Calls.feature
 * @step I want to delete for path {string} with the value from saved property {string}
 **/
When('I want to delete for path {string} with the value from saved property {string}', function(string, string2)  {
	let path = getPath(string, this.context);
	path += '/' + this.context[string2];
	return util.request(this.context, 'DELETE', path);
});


/**
 * @module Rest_Calls
 * @description makes a PUT request to the given path and sends the input data from the context (path is appended after the "onboarding-api/v1.0" prefix)<br>
 *     @exampleFile Example_Rest_Calls.feature
 * @step I want to update for path {string} with the input data from the context
 **/
When('I want to update for path {string} with the input data from the context', function(string)  {
	let path = getPath(string, this.context);
	return util.request(this.context, 'PUT', path, this.context.inputData);
});

/**
 * @module Rest_Calls
 * @description makes a POST request to the given path and sends the input data from the context (path is appended after the "onboarding-api/v1.0" prefix)<br>
 *     @exampleFile Example_Rest_Calls.feature
 * @step I want to create for path {string} with the input data from the context
 **/
When('I want to create for path {string} with the input data from the context', function(string)  {
	let path = getPath(string, this.context);
	return util.request(this.context, 'POST', path, this.context.inputData);
});
