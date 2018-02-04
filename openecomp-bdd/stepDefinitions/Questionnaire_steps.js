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
 * @module Questionnaire
 * @description Gets the questionnaire for the current item and saves it on the context
 * @exampleFile Example_VSP.feature
 * @step I want to get the questionnaire for this item
 **/
Then('I want to get the questionnaire for this item', function () {
	let path = "/vendor-software-products/" + this.context.item.id + "/versions/" + this.context.item.versionId + "/questionnaire";
	return util.request(this.context, 'GET', path).then(result => {
		this.context.qdata = JSON.parse(result.data.data);
		this.context.qschema = result.data.schema;
		this.context.qurl = path;
	});
});

/**
 * @module Questionnaire
 * @description Gets the questionnaire for the current item and component and saves it on the context
 * @exampleFile Example_VSP.feature
 * @step I want to get the questionnaire for this component
 **/
Then('I want to get the questionnaire for this component', function () {
	let path = "/vendor-software-products/" + this.context.item.id + "/versions/" + this.context.item.versionId + "/components/" + this.context.componentId  + "/questionnaire";
	return util.request(this.context, 'GET', path).then(result => {
		this.context.qdata = JSON.parse(result.data.data);
		this.context.qschema = result.data.schema;
		this.context.qurl = path;
	});
});


/**
 * @module Questionnaire
 * @description Updates the property for the saved questionnaire
 * @exampleFile Example_VSP.feature
 * @step I want to update this questionnaire with value {string} for path {string}
 **/
Then('I want to update this questionnaire with value {string} for property {string}', function (string, string2) {
	_.set(this.context.qdata, string, string2);
});

/**
 * @module Questionnaire
 * @description Checks the questionnaire data on the context for the given value and property
 * @exampleFile Example_VSP.feature
 * @step I want to check this questionnaire has value {string} for property {string}
 **/
Then('I want to check this questionnaire has value {string} for property {string}', function (string, string2) {
	assert.equal(_.get(this.context.qdata, string), string2);
});

/**
 * @module Questionnaire
 * @description Updates the the questionnaire data from the context to the same url that loaded it
 * @exampleFile Example_VSP.feature
 * @step I want to update this questionnaire
 **/
Then('I want to update this questionnaire', function () {
	return util.request(this.context, 'PUT', this.context.qurl, this.context.qdata);
});