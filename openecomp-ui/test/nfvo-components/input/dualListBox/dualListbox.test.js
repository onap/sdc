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

import React from 'react';
import TestUtils from 'react-addons-test-utils';
import DualListboxView from 'nfvo-components/input/dualListbox/DualListboxView.jsx';

const ITEMS = [{id: '1', name: 'aaa'}, {id: '2', name: 'bbb'}, {id: '3', name: 'ccc'}];

describe('dualListBox Module Tests', function () {


	it('should render basically', () => {
		var renderer = TestUtils.createRenderer();
		renderer.render(<DualListboxView onChange={()=>{}}/>);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

	it('should render with available list and 4 control buttons', () => {
		var view = TestUtils.renderIntoDocument(<DualListboxView availableList={ITEMS} onChange={()=>{}}/>);
		expect(view).toBeTruthy();
		var results = TestUtils.scryRenderedDOMComponentsWithClass(view, 'dual-list-option');
		expect(results.length).toBe(4);
	});

	it('should add item to selected list', done => {
		const onChange = (values)=> {
			expect(values).toEqual([ITEMS[2].id, ITEMS[0].id]);
			done();
		};
		const document = TestUtils.renderIntoDocument(
			<DualListboxView
				availableList={ITEMS}
				onChange={onChange}
				selectedValuesList={[ITEMS[2].id]}/>);

		const result = TestUtils.scryRenderedDOMComponentsWithTag(document, 'select');
		const options = TestUtils.scryRenderedDOMComponentsWithTag(document, 'option');
		const listBox = TestUtils.findRenderedComponentWithType(document, DualListboxView);
		expect(result).toBeTruthy();
		expect(options).toBeTruthy();
		expect(listBox).toBeTruthy();

		TestUtils.Simulate.change(result[0], {target: {selectedOptions: [options[0]]}});
		expect(listBox.state.selectedValues).toEqual([ITEMS[0].id]);

		listBox.addToSelectedList();
	});

	it('should remove item from selected list', done => {
		const onChange = (values)=> {
			expect(values).toEqual([ITEMS[0].id]);
			done();
		};
		const document = TestUtils.renderIntoDocument(
			<DualListboxView
				availableList={ITEMS}
				onChange={onChange}
				selectedValuesList={[ITEMS[0].id, ITEMS[1].id]}/>);

		const result = TestUtils.scryRenderedDOMComponentsWithTag(document, 'select');
		const options = TestUtils.scryRenderedDOMComponentsWithTag(document, 'option');
		const listBox = TestUtils.findRenderedComponentWithType(document, DualListboxView);
		expect(result).toBeTruthy();
		expect(options).toBeTruthy();
		expect(listBox).toBeTruthy();

		TestUtils.Simulate.change(result[1], {target: {selectedOptions: [options[2]]}});
		expect(listBox.state.selectedValues).toEqual([ITEMS[1].id]);

		listBox.removeFromSelectedList();
	});

	it('should add all items to selected list', done => {
		let onChange = (value)=> {
			expect(value).toEqual(ITEMS.map(item => item.id));
			done();
		};
		var view = new DualListboxView({availableList:ITEMS, onChange, selectedValuesList:[]});
		expect(view).toBeTruthy();
		view.addAllToSelectedList();
	});

	it('should remove all items from selected list', done => {
		let onChange = (value)=> {
			expect(value.length).toBe(0);
			done();
		};
		var view = new DualListboxView({availableList:ITEMS, onChange, selectedValuesList:[]});
		expect(view).toBeTruthy();
		view.removeAllFromSelectedList();
	});


});
