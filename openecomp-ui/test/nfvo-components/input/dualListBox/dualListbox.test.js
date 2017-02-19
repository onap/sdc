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
import DualListboxView from 'nfvo-components/input/dualListbox/DualListboxView.jsx';

const ITEMS = [{id: '1', name: 'aaa'}, {id: '2', name: 'bbb'}, {id: '3', name: 'ccc'}];

describe('dualListBox Module Tests', function () {


	it('should render basically', () => {
		var renderer = TestUtils.createRenderer();
		renderer.render(<DualListboxView onChange={()=>{}}/>);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();
	});

	it('should render with available list and 4 control buttons', () => {
		var view = TestUtils.renderIntoDocument(<DualListboxView availableList={ITEMS} onChange={()=>{}}/>);
		expect(view).toExist();
		var results = TestUtils.scryRenderedDOMComponentsWithClass(view, 'dual-list-option');
		expect(results.length).toBe(4);
	});

	it('should add item to selected list', done => {
		const newItemValue = 'new item';
		let onChange = (value)=> {
			expect(value).toEqual(newItemValue);
			done();
		};
		var view = new DualListboxView({availableList:ITEMS, onChange, selectedValuesList:[]});
		expect(view).toExist();
		view.refs = {
			availableValues: {getValue(){return newItemValue;}}
		};
		view.addToSelectedList();
	});

	it('should remove item from selected list', done => {
		const selectedValuesList = ['a','b'];
		let onChange = (value)=> {
			expect(value).toEqual(selectedValuesList[1]);
			done();
		};
		var view = new DualListboxView({availableList:ITEMS, onChange, selectedValuesList});
		expect(view).toExist();
		view.refs = {
			selectedValues: {getValue(){return ['a'];}}
		};
		view.removeFromSelectedList();
	});

	it('should add all items to selected list', done => {
		let onChange = (value)=> {
			expect(value).toEqual(ITEMS.map(item => item.id));
			done();
		};
		var view = new DualListboxView({availableList:ITEMS, onChange, selectedValuesList:[]});
		expect(view).toExist();
		view.addAllToSelectedList();
	});

	it('should remove all items from selected list', done => {
		let onChange = (value)=> {
			expect(value.length).toBe(0);
			done();
		};
		var view = new DualListboxView({availableList:ITEMS, onChange, selectedValuesList:[]});
		expect(view).toExist();
		view.removeAllFromSelectedList();
	});


});
