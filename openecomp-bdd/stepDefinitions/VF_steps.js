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
 * @module VF
 * @description Creates a VF for this Item (does NOT work on localhost). will get the data for the item and then update the input
 * data for the VF call.
 * @exampleFile Example_VSP.feature
 * @step I want to create a VF for this Item
 **/
Then('I want to create a VF for this Item', function () {
	return util.request(this.context, 'GET', '/vendor-software-products/' + this.context.item.id + '/versions/' + this.context.item.versionId).then(result => {
		this.context.inputData = util.getJSONFromFile('resources/json/createVF.json');
		// start replacing stuff
		this.context.inputData.contactId = this.context.headers["USER_ID"];
		this.context.inputData.categories[0].uniqueId = result.data.category;
		this.context.inputData.categories[0].subcategories[0].uniqueId = result.data.subCategory;
		this.context.inputData.description = result.data.description;
		this.context.inputData.name = result.data.name;
		this.context.inputData.tags[0] = result.data.name;
		this.context.inputData.vendorName = result.data.vendorName;
		this.context.inputData.csarUUID = this.context.item.id;
		return util.request(this.context, 'POST', '/catalog/resources', this.context.inputData, false, true);
	});
});

