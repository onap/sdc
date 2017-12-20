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
import deepFreeze from 'deep-freeze';

import {cloneAndSet} from '../../test-utils/Util.js';
import store from 'sdc-app/AppStore.js';
import errorResponseHandler from 'nfvo-utils/ErrorResponseHandler.js';
import {typeEnum as modalType} from 'nfvo-components/modal/GlobalModalConstants.js';

describe('Error Response Handler Util', () => {

	beforeEach(function () {
		deepFreeze(store.getState());
	});

	it('validating error in policyException', () => {
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
			title: 'Error: SVC4122',
			msg: 'Error: Invalid data.',			
			modalClassName: 'notification-modal',
			type: modalType.ERROR	
		};



		const expectedStore = cloneAndSet(store.getState(), 'modal', errorNotification);

		errorResponseHandler(xhr, textStatus, errorThrown);

		expect(store.getState()).toEqual(expectedStore);
	});

	it('validating error in serviceException with variables', () => {
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
			title: 'Error: SVC4122',
			msg: 'Error: Invalid artifact type newType.',			
			modalClassName: 'notification-modal',
			type: modalType.ERROR	
		};

		const expectedStore = cloneAndSet(store.getState(), 'modal', errorNotification);

		errorResponseHandler(xhr, textStatus, errorThrown);

		expect(store.getState()).toEqual(expectedStore);
	});

	it('validating error in response', () => {
		let textStatus = '', errorThrown = '';
		let xhr = {
			responseJSON: {
				status: 'AA',
				message: 'Error: Invalid data.'
			}
		};
		deepFreeze(xhr);

		const errorNotification = {			
			title: 'AA',
			msg: 'Error: Invalid data.',			
			modalClassName: 'notification-modal',
			type: modalType.ERROR	
		};

		const expectedStore = cloneAndSet(store.getState(), 'modal', errorNotification);

		errorResponseHandler(xhr, textStatus, errorThrown);

		expect(store.getState()).toEqual(expectedStore);
	});

	it('validating error in request', () => {
		let textStatus = '', errorThrown = '';
		let xhr = {
			statusText: '500',
			responseText: 'Internal server error.'
		};
		deepFreeze(xhr);
	
		const errorNotification = {			
			title: '500',
			msg: 'Internal server error.',			
			modalClassName: 'notification-modal',
			type: modalType.ERROR	
		};

		const expectedStore = cloneAndSet(store.getState(), 'modal', errorNotification);

		errorResponseHandler(xhr, textStatus, errorThrown);

		expect(store.getState()).toEqual(expectedStore);
	});
});
