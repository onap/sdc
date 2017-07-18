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
import React from 'react';
import TestUtils from 'react-addons-test-utils';
import {storeCreator} from 'sdc-app/AppStore.js';
import ComputeFlavors from 'sdc-app/onboarding/softwareProduct/components/compute/computeComponents/ComputeFlavors.js';
import {ComputeFlavorBaseData} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsComputeFactory.js';

const softwareProductId = '123';
const componentId = '111';

describe('Software Product Component ComputeFlavors - View Classes.', () => {

	it('ComputeFlavors List View Test', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const ComputeFlavorsList = ComputeFlavorBaseData.buildList(1);

		var renderer = TestUtils.createRenderer();
		renderer.render(
			<ComputeFlavors
				store={store}
				ComputeFlavorsList={ComputeFlavorsList}
				softwareProductId={softwareProductId}
				componentId={componentId}
				isReadOnlyMode={false}/>
		);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});
});

