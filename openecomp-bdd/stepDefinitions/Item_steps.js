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
/**
 * @module Item
 * @description uses item id and version id from context
 * @exampleFile Example_VSP.feature, Example_VLM.feature
 * @step I want to make sure this Item has status {string}
 **/
Then('I want to make sure this Item has status {string}', function (string) {
	let path = '/items/' + this.context.item.id + '/versions';
	return util.request(this.context, 'GET', path).then(result => {
		assert.equal(result.data.results[0].id, this.context.item.versionId);
		assert.equal(result.data.results[0].status, string);
	});
});
/**
 * @module Item
 * @description uses item id and version id from context
 * @exampleFile Example_VSP.feature, Example_VLM.feature
 * @step I want to commit this Item
 **/
Then('I want to commit this Item', function () {
	let path = '/items/' + this.context.item.id + '/versions/' + this.context.item.versionId + '/actions';
	let inputData = {action: 'Commit', commitRequest: {message: '00Behave'}};
	return util.request(this.context, 'PUT', path, inputData);
});
/**
 * @module Item
 * @description creates a new major version. item id and version id from context
 * @exampleFile Example_VLM.feature
 * @step I want to create a new version for this Item
 **/
Then('I want to create a new version for this Item', function () {
	let path = '/items/' + this.context.item.id + '/versions/' + this.context.item.versionId;
	let inputData = {description: 'Behave Version', creationMethod: 'major'};
	return util.request(this.context, 'POST', path, inputData).then(result => {
		assert.equal(result.data.status, 'Draft');
	});
});
/**
 * @module Item
 * @description reverts to a revision with a given saved property. Should be set from the revision list first
 * @exampleFile Example_VLM.feature
 * @step I want to commit this Item
 **/
Then('I want to revert this Item to the revision with the value from saved property {string}', function (string) {
	let path = '/items/' + this.context.item.id + '/versions/' + this.context.item.versionId + '/actions';
	let inputData = {action: 'Revert', revisionRequest: {revisionId: this.context[string]}};
	return util.request(this.context, 'PUT', path, inputData);
});

