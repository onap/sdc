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

import deepFreeze from 'deep-freeze';
import expect from 'expect';
import {cloneAndSet} from '../../test-utils/Util.js';
import store from 'sdc-app/AppStore.js';
import errorResponseHandler from 'nfvo-utils/ErrorResponseHandler.js';

describe('Error Response Handler Util', () => {

	beforeEach(function () {
		deepFreeze(store.getState());
	});

	it('validating error in policyException', done => {
		let textStatus = '', errorThrown = '';
		let xhr = {
			responseJSON: {
				requestError: {
					policyException: {
						messageId: 'SVC4122',
						text: 'Error: Invalid data.'
					}
				}
			}
		};
		deepFreeze(xhr);

		const errorNotification = {
			type: 'error', title: 'Error: SVC4122', msg: 'Error: Invalid data.', timeout: undefined,
			validationResponse: undefined
		};
		const expectedStore = cloneAndSet(store.getState(), 'notification', errorNotification);

		errorResponseHandler(xhr, textStatus, errorThrown);

		setTimeout(function () {
			expect(store.getState()).toEqual(expectedStore);
			done();
		}, 100);
	});

	it('validating error in serviceException with variables', done => {
		let textStatus = '', errorThrown = '';
		let xhr = {
			responseJSON: {
				requestError: {
					serviceException: {
						messageId: 'SVC4122',
						text: "Error: Invalid artifact type '%1'.",
						variables: ['newType']
					}
				}
			}
		};
		deepFreeze(xhr);

		const errorNotification = {
			type: 'error', title: 'Error: SVC4122', msg: 'Error: Invalid artifact type newType.', timeout: undefined,
			validationResponse: undefined
		};
		const expectedStore = cloneAndSet(store.getState(), 'notification', errorNotification);

		errorResponseHandler(xhr, textStatus, errorThrown);

		setTimeout(function () {
			expect(store.getState()).toEqual(expectedStore);
			done();
		}, 100);
	});

	it('validating error in response', done => {
		let textStatus = '', errorThrown = '';
		let xhr = {
			responseJSON: {
				status: 'AA',
				message: 'Error: Invalid data.'
			}
		};
		deepFreeze(xhr);

		const errorNotification = {
			type: 'error', title: 'AA', msg: 'Error: Invalid data.', timeout: undefined,
			validationResponse: undefined
		};
		const expectedStore = cloneAndSet(store.getState(), 'notification', errorNotification);

		errorResponseHandler(xhr, textStatus, errorThrown);

		setTimeout(function () {
			expect(store.getState()).toEqual(expectedStore);
			done();
		}, 100);
	});

	it('validating error in request', done => {
		let textStatus = '', errorThrown = '';
		let xhr = {
			statusText: '500',
			responseText: 'Internal server error.'
		};
		deepFreeze(xhr);

		const errorNotification = {
			type: 'error', title: '500', msg: 'Internal server error.', timeout: undefined,
			validationResponse: undefined
		};
		const expectedStore = cloneAndSet(store.getState(), 'notification', errorNotification);

		errorResponseHandler(xhr, textStatus, errorThrown);

		setTimeout(function () {
			expect(store.getState()).toEqual(expectedStore);
			done();
		}, 100);
	});
});
