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
import ListEditorView from 'src/nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'src/nfvo-components/listEditor/ListEditorItemView.jsx';

describe('listEditor Module Tests', function () {


	it('list editor view should exist', () => {
		expect(ListEditorView).toExist();
	});

	it('list editor item view should exist', () => {
		expect(ListEditorItemView).toExist();
	});

	it('should render list and list item and call onEdit', done => {
		let itemView = TestUtils.renderIntoDocument(
			<ListEditorView title='some title'>
				<ListEditorItemView onEdit={done}>
					<div></div>
				</ListEditorItemView>
			</ListEditorView>
		);
		expect(itemView).toExist();
		let sliderIcon = TestUtils.findRenderedDOMComponentWithClass(itemView, 'fa-sliders');
		TestUtils.Simulate.click(sliderIcon);
	});

	it('should render list and list item and call onFilter', done => {
		let itemView = TestUtils.renderIntoDocument(
			<ListEditorView onFilter={()=>{done();}}>
				<ListEditorItemView>
					<div></div>
				</ListEditorItemView>
			</ListEditorView>
		);
		expect(itemView).toExist();
		let filterInput = TestUtils.findRenderedDOMComponentWithTag(itemView, 'input');
		TestUtils.Simulate.change(filterInput);
	});

	it('should render READONLY list item and not call onEdit', done => {
		let itemView = TestUtils.renderIntoDocument(
			<ListEditorItemView onEdit={done} isReadOnlyMode={true}>
				<div></div>
			</ListEditorItemView>
		);
		expect(itemView).toExist();
		let sliderIcon = TestUtils.findRenderedDOMComponentWithClass(itemView, 'fa-sliders');
		TestUtils.Simulate.click(sliderIcon);
	});

	it('should render list item and call onDelete', done => {
		let itemView = TestUtils.renderIntoDocument(
			<ListEditorItemView onDelete={done} isReadOnlyMode={false}>
				<div></div>
			</ListEditorItemView>
		);
		expect(itemView).toExist();
		let sliderIcon = TestUtils.findRenderedDOMComponentWithClass(itemView, 'fa-trash-o');
		TestUtils.Simulate.click(sliderIcon);
	});

	it('should render READONLY list item and not call onDelete', () => {
		let itemView = TestUtils.renderIntoDocument(
			<ListEditorItemView onDelete={()=>{}} isReadOnlyMode={true}>
				<div></div>
			</ListEditorItemView>
		);
		expect(itemView).toExist();
		let sliderIcon = TestUtils.scryRenderedDOMComponentsWithClass(itemView, 'fa-trash-o');
		expect(sliderIcon).toEqual(0);
	});
});
