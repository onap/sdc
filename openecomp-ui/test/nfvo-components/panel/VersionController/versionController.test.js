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
import React from 'react';
import TestUtils from 'react-addons-test-utils';
import VersionController from 'nfvo-components/panel/versionController/VersionController.jsx';
import {actionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';

describe('versionController UI Component', () => {

	it('function does exist', () => {
		var renderer = TestUtils.createRenderer();
		renderer.render(<VersionController isCheckedOut={false} status={'OUT'} />);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();
	});

	it('validating checkin function', () => {

		let versionController = TestUtils.renderIntoDocument(<VersionController isCheckedOut={false} status={'OUT'} onSave={()=>{return Promise.resolve();}}/>);
		let cb = action => expect(action).toBe(actionsEnum.CHECK_IN);
		versionController.checkin(cb);

	});

});
