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
import {scryRenderedDOMComponentsWithTestId} from 'test-utils/Util.js';
import Input from 'nfvo-components/input/validation/Input.jsx';
import Overlay from 'react-bootstrap/lib/Overlay.js';

describe('Input', function () {
	it('should render with type text', () => {
		let renderedOutput = TestUtils.renderIntoDocument(<Input type='text' data-test-id='mytest' />);
		const elem = scryRenderedDOMComponentsWithTestId(renderedOutput,'mytest');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
		expect(elem[0].type).toBe('text');
	});

	it('should render with type textarea', () => {
		let renderedOutput = TestUtils.renderIntoDocument(<Input type='textarea' data-test-id='mytest' />);
		const elem = scryRenderedDOMComponentsWithTestId(renderedOutput,'mytest');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
		expect(elem[0].tagName.toLowerCase()).toBe('textarea');
	});

	it('should render with type radio', () => {
		let renderedOutput = TestUtils.renderIntoDocument(<Input type='radio' data-test-id='mytest' />);
		const elem = scryRenderedDOMComponentsWithTestId(renderedOutput,'mytest');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
		expect(elem[0].type).toBe('radio');
	});

	it('should render with type select', () => {
		let renderedOutput = TestUtils.renderIntoDocument(<Input type='select' data-test-id='mytest' />);
		const elem = scryRenderedDOMComponentsWithTestId(renderedOutput,'mytest');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
		expect(elem[0].tagName.toLowerCase()).toBe('select');
	});

	it('should render with type number', () => {
		let renderedOutput = TestUtils.renderIntoDocument(<Input type='number' data-test-id='mytest' />);
		const elem = scryRenderedDOMComponentsWithTestId(renderedOutput,'mytest');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
		expect(elem[0].tagName.toLowerCase()).toBe('input');
		expect(elem[0].type).toBe('number');
	});

	it('should render with type checkbox', () => {
		let renderedOutput = TestUtils.renderIntoDocument(<Input type='checkbox' data-test-id='mytest' />);
		const elem = scryRenderedDOMComponentsWithTestId(renderedOutput,'mytest');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
		expect(elem[0].tagName.toLowerCase()).toBe('input');
		expect(elem[0].type).toBe('checkbox');
	});

	it('should render error overlay when invalid', () => {
		let renderedOutput = TestUtils.renderIntoDocument(<Input type='text' data-test-id='mytest' isValid={false} errorText='this is an error'/>);
		const elem = TestUtils.findRenderedComponentWithType(renderedOutput,Overlay);
		expect(elem).toBeTruthy();
		expect(elem.props.show).toBe(true);
	});

	it('should not render error overlay when valid', () => {
		let renderedOutput = TestUtils.renderIntoDocument(<Input type='text' data-test-id='mytest' isValid={true} errorText='this is an error'/>);
		const elem = TestUtils.findRenderedComponentWithType(renderedOutput,Overlay);
		expect(elem).toBeTruthy();
		expect(elem.props.show).toBe(false);
	});

	/*it('should return the value of a select', () => {

	});

	it('should return the value of a checkbox', () => {

	});

	it('should return the value of a radio', () => {

	});

	it('should return the value of a text', () => {

	});

	it('should return the value of a textarea', () => {

	});*/

	/*it('should render and work as a group', () => {
	 let MockComp = React.createClass({
	 render: function() {
	 return (<div>
	 <Input type='radio' data-test-id='mytest' name='g1' value='0'/><Input type='radio' data-test-id='mytest1' name='g1' value='1' />
	 </div>);
	 }
	 });
	 let renderedOutput = TestUtils.renderIntoDocument(<MockComp />);
	 const radio1 = scryRenderedDOMComponentsWithTestId(renderedOutput,'mytest');
	 expect(radio1).toBeTruthy();
	 expect(radio1.length).toBe(1);
	 expect(radio1[0].type).toBe('radio');
	 expect(radio1[0].value).toBe('0');
	 const radio2 = scryRenderedDOMComponentsWithTestId(renderedOutput,'mytest1');
	 expect(radio2).toBeTruthy();
	 expect(radio2.length).toBe(1);
	 expect(radio2[0].type).toBe('radio');
	 expect(radio2[0].value).toBe('1');
	 TestUtils.Simulate.click(
	 radio2[0]
	 );
	 TestUtils.Simulate.click(
	 radio1[0]
	 );
	 console.log('radio1: ' + radio1[0].checked);
	 console.log('radio2: ' + radio2[0].checked);
	 expect(radio2[0].checked).toBe(false);
	 expect(radio1[0].checked).toBe(true);


	 });*/

});
