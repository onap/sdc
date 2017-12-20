/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import React from 'react';
import TestUtils from 'react-addons-test-utils';

import {UserFactory} from 'test-utils/factories/users/UsersFactories.js';
import {mapStateToProps} from 'sdc-app/onboarding/permissions/PermissionsManager.js';
import PermissionsManager from 'sdc-app/onboarding/permissions/PermissionsManager.jsx';



describe('Manage Permissions: ', function () {
	let globalItemId = '343434', usersList = [], userInfo = {}, versionsPage = {}, contributor = {}, contributorToAdd = {}, owner = {};
	beforeAll(function() {
		usersList = UserFactory.buildList(3);
		userInfo = usersList[0];
		owner = usersList[0];
		contributor = usersList[1];
		contributorToAdd = usersList[2];
		versionsPage = {
			permissions: {
				owner: owner,
				contributors: [contributor],
				viewers: []
			}
		};
	});

	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('should mapper return basic permissions page data', () => {
		const obj = {versionsPage, users: {usersList, userInfo}};
		const result = mapStateToProps(obj);
		result.itemId = globalItemId;
		expect(result.owner).toBeTruthy();
		expect(result.itemUsers).toBeTruthy();
		expect(result.userInfo).toBeTruthy();
		expect(result.itemId).toBeTruthy();
		expect(result.users).toBeTruthy();
	});

	it('permission manager basic view', () => {

		const obj = {versionsPage, users: {usersList, userInfo}};
		const params = mapStateToProps(obj);
		let permissionsView = TestUtils.renderIntoDocument(<PermissionsManager	{...params}/>);
		expect(permissionsView).toBeTruthy();
	});

	it('permisssion manager onChange contributors test', () => {

		const obj = {versionsPage, users: {usersList, userInfo}};
		const params = mapStateToProps(obj);
		params.itemId = globalItemId;
		let permissionsView = TestUtils.renderIntoDocument(<PermissionsManager	{...params}/>);
		expect(permissionsView).toBeTruthy();
		const userToAdd = {
			value: contributorToAdd.userId,
			label: contributorToAdd.fullName
		};
		let itemUsers = [{...userToAdd}];

		permissionsView.onChangeItemUsers({itemUsers});
		expect(permissionsView.state.itemUsers[0].userId).toEqual(userToAdd.value);
	});

	it('permisssion manager onSave contributors test', () => {

		const obj = {versionsPage, users: {usersList, userInfo}};
		let  params = mapStateToProps(obj);
		params.itemId = globalItemId;
		const userToAdd = {
			value: contributorToAdd.userId,
			label: contributorToAdd.fullName
		};
		let itemUsers = [{...userToAdd}];


		params.onSubmit = ({itemId, addedUsersIds, removedUsersIds, allUsers, owner}) => {
			expect(itemId).toEqual(globalItemId);
			expect(addedUsersIds[0]).toEqual(contributorToAdd.userId);
			expect(removedUsersIds[0]).toEqual(contributor.userId);
			expect(allUsers).toEqual(usersList);
			expect(owner).toEqual(owner);
		};
		let permissionsView = TestUtils.renderIntoDocument(<PermissionsManager	{...params}/>);
		expect(permissionsView).toBeTruthy();
		permissionsView.onChangeItemUsers({itemUsers});
		permissionsView.onsaveItemUsers();
	});



});