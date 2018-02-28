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
const fs = require('fs');
const util = require('./Utils.js');

/**
 * @module InputData
 * @description creates an ampty input data object
 * @exampleFile Example_Rest_Calls.feature, Example_VLM.feature
 * @step I want to create input data
 **/
When('I want to create input data', function () {
	this.context.inputData = {};
});

/**
 * @module InputData
 * @exampleFile Example_Heat.feature
 * @description I want to set the input data to:<br>
 *     """<br>
 *         {jsonObject}<br>
 *             """<br>
 * @step creates an input data element with the given json object
 **/
When('I want to set the input data to:', function (docString) {
	this.context.inputData = JSON.parse(docString);
});

/**
 * @module InputData
 * @description creates an input data object from the json in the given file
 * @exampleFile Example_Rest_Calls.feature
 * @step I want to set the input data to file {string}
 **/
When('I want to set the input data to file {string}', function (string) {
	this.context.inputData = util.getJSONFromFile(string);
});

/**
 * @module InputData
 * @description sets the property on the input data to the given value
 * @exampleFile Example_Rest_Calls.feature, Example_VLM.feature
 * @step I want to update the input property {string} with value {string}
 **/
Then('I want to update the input property {string} with value {string}', function(string, string2)  {
	_.set(this.context.inputData, string, string2);
});

/**
 * @module InputData
 * @description removes a property from the input data object
 * @exampleFile Example_Rest_Calls.feature
 * @step I want to remove {string} from the input data
 **/
Then('I want to remove {string} from the input data', function(string)  {
	delete this.context.inputData[string];
});

/**
 * @module InputData
 * @description sets the input data property to a random value
 * @exampleFile Example_Rest_Calls.feature
 * @step I want to update the input property {string} with a random value
 **/
Then('I want to update the input property {string} with a random value', function(string)  {
	_.set(this.context.inputData, string, util.random());
});
