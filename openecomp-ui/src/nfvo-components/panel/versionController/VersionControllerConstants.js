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
import keyMirror from 'nfvo-utils/KeyMirror.js';

export const actionsEnum = keyMirror({
	CHECK_IN: 'Checkin',
	CHECK_OUT: 'Checkout',
	UNDO_CHECK_OUT: 'Undo_Checkout',
	SUBMIT: 'Submit',
	CREATE_PACKAGE: 'Create_Package'
});

export const statusEnum = keyMirror({
	CHECK_OUT_STATUS: 'Locked',
	CHECK_IN_STATUS: 'Available',
	SUBMIT_STATUS: 'Final',
	LOCK_STATUS: 'LockedByUser',
	PREVIOUS_VERSION: 'READ ONLY'
});

export const statusBarTextMap = keyMirror({
	'Locked': 'Checked Out',
	'LockedByUser': '',
	'Available': 'Checked In',
	'Final': 'Submitted',
	'READ ONLY': 'Locked'
});

