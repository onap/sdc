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
import {Factory} from 'rosie';
import {statusEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';

export default new Factory()
	.attrs({
		version: { id: '1.2', label: '1.2'},
		viewableVersions: [{id: '1.0', label: '1.0'}, {id: '1.1', label: '1.1'}, {id: '1.2', label: '1.2'}],
		status: statusEnum.CHECK_OUT_STATUS,
		lockingUser: 'current'
	}).after(function(inst) {
		if (inst.status !== statusEnum.CHECK_OUT_STATUS) {
			delete inst.lockingUser;
		}
	});

