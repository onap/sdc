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
import TabulatedEditor from 'nfvo-components/editor/TabulatedEditor.jsx';
import { Provider } from 'react-redux';
import {storeCreator} from 'sdc-app/AppStore.js';

describe('Tabulated Editor test: ', function () {
	const store = storeCreator();
	it('basic view test', () => {
		let renderer = TestUtils.createRenderer();
		renderer.render(
			<Provider store={store}><TabulatedEditor><button>test</button></TabulatedEditor></Provider>
		);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

	});

	it('handle func test', () => {
		let props = {
			navigationBarProps: {
				groups: [],
				onNavigationItemClick: ()=>{}
			},
			versionControllerProps: {
				isCheckedOut: false,
				version: {id: '0.1', label: '0.1'},
				viewableVersions: [{id: '0.1', label: '0.1'}],
				itemPermission: {isCertified: false, isCollaborator: true, isDirty: false},
				onSubmit: ()=>{},
				onRevert: ()=>{}
			}
		};
		const view = TestUtils.renderIntoDocument(<Provider store={store}><TabulatedEditor {...props}><button>test</button></TabulatedEditor></Provider>);
		expect(view).toBeTruthy();
	});

});
