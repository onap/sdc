/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

import expect from 'expect';
import deepFreeze from 'deep-freeze';
import Configuration from 'sdc-app/config/Configuration.js';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import {statusEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';

const status = 'testStatus';

describe('versionController UI Component', () => {

	it('function does exist', () => {
		expect(VersionControllerUtils).toExist();
	});

	it('validating getCheckOutStatusKindByUserID - without "UserID"', () => {
		var result = VersionControllerUtils.getCheckOutStatusKindByUserID(status);
		expect(result.status).toBe(status);
		expect(result.isCheckedOut).toBe(true);
	});

	it('validating getCheckOutStatusKindByUserID - without "UserID" with locking user', () => {
		var result = VersionControllerUtils.getCheckOutStatusKindByUserID(status, 'locking user');
		expect(result.status).toBe(statusEnum.LOCK_STATUS);
		expect(result.isCheckedOut).toBe(false);
	});

	it('validating getCheckOutStatusKindByUserID - with "UserID" with configuration set', () => {
		const userId = 'att';

		Configuration.set('ATTUserID', userId);
		var result = VersionControllerUtils.getCheckOutStatusKindByUserID(status, userId);
		Configuration.set('ATTUserID', undefined);

		expect(result.status).toBe(status);
		expect(result.isCheckedOut).toBe(true);
	});



	it('validating isCheckedOutByCurrentUser - when resource is not checked out', () => {
		const currentUser = 'current';
		const resource = deepFreeze({
			version: '0.6',
			viewableVersions: ['0.1', '0.2', '0.3', '0.4', '0.5', '0.6'],
			status: 'Final'
		});

		Configuration.set('ATTUserID', currentUser);
		const result = VersionControllerUtils.isCheckedOutByCurrentUser(resource);
		Configuration.set('ATTUserID', undefined);

		expect(result).toBe(false);
	});

	it('validating isCheckedOutByCurrentUser - when resource is checked out', () => {
		const currentUser = 'current';
		const resource = deepFreeze({
			version: '0.6',
			viewableVersions: ['0.1', '0.2', '0.3', '0.4', '0.5', '0.6'],
			status: 'Locked',
			lockingUser: 'current'
		});

		Configuration.set('ATTUserID', currentUser);
		const result = VersionControllerUtils.isCheckedOutByCurrentUser(resource);
		Configuration.set('ATTUserID', undefined);

		expect(result).toBe(true);
	});

	it('validating isCheckedOutByCurrentUser - when resource is checked out by another user', () => {
		const currentUser = 'current';
		const resource = deepFreeze({
			version: '0.6',
			viewableVersions: ['0.1', '0.2', '0.3', '0.4', '0.5', '0.6'],
			status: 'Locked',
			lockingUser: 'another'
		});

		Configuration.set('ATTUserID', currentUser);
		const result = VersionControllerUtils.isCheckedOutByCurrentUser(resource);
		Configuration.set('ATTUserID', undefined);

		expect(result).toBe(false);
	});



	it('validating isReadOnly - when resource is not checked out', () => {
		const currentUser = 'current';
		const resource = deepFreeze({
			version: '0.6',
			viewableVersions: ['0.1', '0.2', '0.3', '0.4', '0.5', '0.6'],
			status: 'Final'
		});

		Configuration.set('ATTUserID', currentUser);
		const result = VersionControllerUtils.isReadOnly(resource);
		Configuration.set('ATTUserID', undefined);

		expect(result).toBe(true);
	});

	it('validating isReadOnly - when resource is checked out', () => {
		const currentUser = 'current';
		const resource = deepFreeze({
			version: '0.6',
			viewableVersions: ['0.1', '0.2', '0.3', '0.4', '0.5', '0.6'],
			status: 'Locked',
			lockingUser: 'current'
		});

		Configuration.set('ATTUserID', currentUser);
		const result = VersionControllerUtils.isReadOnly(resource);
		Configuration.set('ATTUserID', undefined);

		expect(result).toBe(false);
	});

	it('validating isReadOnly - when version of resource is not latest', () => {
		const currentUser = 'current';
		const resource = deepFreeze({
			version: '0.2',
			viewableVersions: ['0.1', '0.2', '0.3', '0.4', '0.5', '0.6'],
			status: 'Locked',
			lockingUser: 'current'
		});

		Configuration.set('ATTUserID', currentUser);
		const result = VersionControllerUtils.isReadOnly(resource);
		Configuration.set('ATTUserID', undefined);

		expect(result).toBe(true);
	});

	it('validating isReadOnly - when resource is checked out by another user', () => {
		const currentUser = 'current';
		const resource = deepFreeze({
			version: '0.6',
			viewableVersions: ['0.1', '0.2', '0.3', '0.4', '0.5', '0.6'],
			status: 'Locked',
			lockingUser: 'another'
		});

		Configuration.set('ATTUserID', currentUser);
		const result = VersionControllerUtils.isReadOnly(resource);
		Configuration.set('ATTUserID', undefined);

		expect(result).toBe(true);
	});
});

