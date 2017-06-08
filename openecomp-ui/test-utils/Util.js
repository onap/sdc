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
import times from 'lodash/times';
import pick from 'lodash/pick';
import intersection from 'lodash/intersection';
import ReactTestUtils from 'react-addons-test-utils';

export const buildListFromFactory = (factory, quantity = 3, overrides) => {
	let list = [];
	times(quantity, () =>{
		list.push(factory.build(overrides));
	});
	return list;
};

export const buildFromExistingObject = (factory, obj, overrides = {}, options = {}) => {
	const mock = factory.build();
	const sharedProperties = intersection(Object.keys(mock), Object.keys(obj));
	return factory.build({...pick(obj, sharedProperties), ...overrides}, options);
};

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

/**
 * Finds all instance of components in the rendered tree that are DOM
 * components with the data-test-id
 * @return {array} an array of all the matches.
 */
export const scryRenderedDOMComponentsWithTestId = (root, testId) => {
	return ReactTestUtils.findAllInRenderedTree(root, function (inst) {
		if (ReactTestUtils.isDOMComponent(inst)) {
			var compTestId = inst.getAttribute('data-test-id');
			return compTestId === testId;
		}
		return false;
	});
};
