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


import Configuration from 'sdc-app/config/Configuration.js';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import {statusEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import VersionControllerUtilsFactory from 'test-utils/factories/softwareProduct/VersionControllerUtilsFactory.js';

const status = 'testStatus';
const {lockingUser: currentUser, viewableVersions: defaultVersions} = VersionControllerUtilsFactory.build();

describe('versionController UI Component', () => {

	it('function does exist', () => {
		expect(VersionControllerUtils).toBeTruthy();
	});

	it('validating getCheckOutStatusKindByUserID - without "UserID"', () => {
		var result = VersionControllerUtils.getCheckOutStatusKindByUserID(status);
		expect(result.status).toBe(status);
		expect(result.isCheckedOut).toBe(false);
	});

	it('validating getCheckOutStatusKindByUserID - without "UserID" with locking user', () => {
		var result = VersionControllerUtils.getCheckOutStatusKindByUserID(status, 'locking user');
		expect(result.status).toBe(statusEnum.LOCK_STATUS);
		expect(result.isCheckedOut).toBe(false);
	});

	it('validating getCheckOutStatusKindByUserID - with "UserID" with configuration set', () => {
		const Uid = 'ecomp';

		Configuration.set('UserID', Uid);
		var result = VersionControllerUtils.getCheckOutStatusKindByUserID(status, Uid);
		Configuration.set('UserID', undefined);
		expect(result.status).toBe(status);
		expect(result.isCheckedOut).toBe(true);
	});



	it('validating isCheckedOutByCurrentUser - when resource is not checked out', () => {
		const resource = VersionControllerUtilsFactory.build({status: statusEnum.SUBMIT_STATUS});

		Configuration.set('UserID', currentUser);
		const result = VersionControllerUtils.isCheckedOutByCurrentUser(resource);
		Configuration.set('UserID', undefined);

		expect(result).toBe(false);
	});

	it('validating isCheckedOutByCurrentUser - when resource is checked out', () => {
		const resource = VersionControllerUtilsFactory.build();

		Configuration.set('UserID', currentUser);
		const result = VersionControllerUtils.isCheckedOutByCurrentUser(resource);
		Configuration.set('UserID', undefined);

		expect(result).toBe(true);
	});

	it('validating isCheckedOutByCurrentUser - when resource is checked out by another user', () => {
		const resource = VersionControllerUtilsFactory.build({lockingUser: 'another'});

		Configuration.set('UserID', currentUser);
		const result = VersionControllerUtils.isCheckedOutByCurrentUser(resource);
		Configuration.set('UserID', undefined);

		expect(result).toBe(false);
	});



	it('validating isReadOnly - when resource is not checked out', () => {
		const resource = VersionControllerUtilsFactory.build({status: statusEnum.SUBMIT_STATUS});

		Configuration.set('UserID', currentUser);
		const result = VersionControllerUtils.isReadOnly(resource);
		Configuration.set('UserID', undefined);

		expect(result).toBe(true);
	});

	it('validating isReadOnly - when resource is checked out', () => {
		const resource = VersionControllerUtilsFactory.build();

		Configuration.set('UserID', currentUser);
		const result = VersionControllerUtils.isReadOnly(resource);
		Configuration.set('UserID', undefined);

		expect(result).toBe(false);
	});

	it('validating isReadOnly - when version of resource is not latest', () => {

		const resource = VersionControllerUtilsFactory.build({version: defaultVersions[defaultVersions.length - 2]});

		Configuration.set('UserID', currentUser);
		const result = VersionControllerUtils.isReadOnly(resource);
		Configuration.set('UserID', undefined);

		expect(result).toBe(true);
	});

	it('validating isReadOnly - when resource is checked out by another user', () => {
		const resource = VersionControllerUtilsFactory.build({lockingUser: 'another'});

		Configuration.set('UserID', currentUser);
		const result = VersionControllerUtils.isReadOnly(resource);
		Configuration.set('UserID', undefined);

		expect(result).toBe(true);
	});
});
