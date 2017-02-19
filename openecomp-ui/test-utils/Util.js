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
import ReactTestUtils from 'react-addons-test-utils';

//returned object should be treated as immutable.
export const cloneAndSet = (obj, path, value) => {
	let retVal = {...obj};
	let inner = retVal;

	if (typeof path === 'string') {
		path = path.split('.');
	}

	for (let i = 0; i < path.length - 1; i++) {
		inner[path[i]] = {
			...inner[path[i]]
		};
		inner = inner[path[i]];
	}
	inner[path[path.length - 1]] = value;
	return deepFreeze(retVal);
};

/**
 * array findAllRenderedDOMComponentsWithTestId(
 ReactComponent tree,
 function test
 )
 * @param tree - ReactComponent
 * @param testId - string
 * @returns {Array.<T>}
 */
export const findAllRenderedComponentsWithTestId = (tree, testId) => {
	return ReactTestUtils.findAllInRenderedTree(tree, component => component.props.testId === testId);
};

