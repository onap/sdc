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
 * @module Collaboration
 * @description Adds the user with the given user ID as a contributor on the item
 * @exampleFile Example_Collaboration.feature
 * @step I want to add user {string} as a contributor to this Item
 **/
When('I want to add user {string} as a contributor to this Item', function(string)  {
	let path = '/items/' + this.context.item.id + '/permissions/Contributor';
	let inputData = {removedUsersIds:[],addedUsersIds:[string]};
	return util.request(this.context, 'PUT', path, inputData);
});

/**
 * @module Collaboration
 * @description Adds the user with the given user ID as a contributor on the item
 * @exampleFile Example_Collaboration.feature
 * @step I want to remove user {string} as a contributor to this Item
 **/
When('I want to remove user {string} as a contributor to this Item', function(string)  {
	let path = '/items/' + this.context.item.id + '/permissions/Contributor';
	let inputData = {removedUsersIds:[string],addedUsersIds:[]};
	return util.request(this.context, 'PUT', path, inputData);
});

/**
 * @module Collaboration
 * @description Changes the owner to the given User ID
 * @exampleFile Example_Collaboration.feature
 * @step I want to change the owner to user {string} on this Item
 **/
When('I want to change the owner to user {string} on this Item', function(string)  {
	let path = '/items/' + this.context.item.id + '/permissions/Owner';
	let inputData = {removedUsersIds:[],addedUsersIds:[string]};
	return util.request(this.context, 'PUT', path, inputData);
});


/**
 * @module Collaboration
 * @description Checks the role for a user on the item by User id and Role can be either: Contributor/Owner
 * @exampleFile Example_Collaboration.feature
 * @step I want check user {string} has role {string} on this Item
 **/
When('I want to check user {string} has role {string} on this Item', function(string, string2)  {
	let path = '/items/' + this.context.item.id + '/permissions';
	return util.request(this.context, 'GET', path).then(results => {
		for (i in results.data.results) {
			if (results.data.results[i].userId === string) {
				assert.equal(string2.toLowerCase(), results.data.results[i].permission.toLowerCase());
				return;
			}
		}
		assert.fail('User not found');
	});
});

/**
 * @module Collaboration
 * @description Checks the user wth this Id has no permissions on this item
 * @exampleFile Example_Collaboration.feature
 * @step I want check user {string} has rno permissions on this Item
 **/
When('I want to check user {string} has no permissions on this Item', function(string)  {
	let path = '/items/' + this.context.item.id + '/permissions';
	return util.request(this.context, 'GET', path).then(results => {
		for (i in results.data.results) {
			if (results.data.results[i].userId === string) {
				assert.fail('Found', null, 'User should not have permissions');
				return;
			}
		}
	});
});

/**
 * @module Collaboration
 * @description Gets the permissions for the Item
 * @exampleFile Example_Collaboration.feature
 * @step I want to get the permissions for this Item
 **/
When('I want to get the permissions for this Item', function()  {
	let path = '/items/' + this.context.item.id + '/permissions';
	return util.request(this.context, 'GET', path);
});

/**
 * @module Collaboration
 * @description Changes the user for the Rest calls
 * @exampleFile Example_Collaboration.feature
 * @step I want to set the user to {string}
 **/
When('I want to set the user to {string}', function(string)  {
	this.context.headers.USER_ID = string;
});
