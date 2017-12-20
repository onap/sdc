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

import GlobalModal, {GlobalModalView, mapStateToProps} from 'src/nfvo-components/modal/GlobalModal.js';
import React from 'react';
import TestUtils from 'react-addons-test-utils';
import store from 'sdc-app/AppStore.js';
import {actionTypes, typeEnum} from 'src/nfvo-components/modal/GlobalModalConstants.js';

const title = 'TITLE';
const msg = 'message';

describe('Global Modal tests: ', function () {
	it (' mapStateToProps exists', function () {
		expect(mapStateToProps).toBeTruthy();
	});

	it ('mapStateToProps should return show as true', () => {
		let state = {
			modal: {
				type: ''				
			}
		};
		let props = mapStateToProps(state);
		expect(props.show).toEqual(true);
	});

	it('modal should show with default values', () => {
		store.dispatch({
			type: actionTypes.GLOBAL_MODAL_SHOW,
			data: {
				title,
				msg
			}
		});
		const modal = store.getState().modal;
		expect(modal).toBeTruthy();
		expect(modal.title).toBe(title);
		expect(modal.msg).toBe(msg);	
	});

	it('global modal should show with type success with connected component', () => {
		store.dispatch({type: actionTypes.GLOBAL_MODAL_SHOW, data: {title, msg}});

		expect(store.getState().modal).toBeTruthy();
			
		let renderer = TestUtils.createRenderer();
		renderer.render(<GlobalModal store={store}/>);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

	});


	it('global modal should show with type success with connected component and closed after', () => {
		store.dispatch({type: actionTypes.GLOBAL_MODAL_SHOW, data: {title, msg}});
		
		expect(store.getState().modal).toBeTruthy();
			
		let renderer = TestUtils.createRenderer();
		renderer.render(<GlobalModal store={store}/>);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

		store.dispatch({type: actionTypes.GLOBAL_MODAL_CLOSE});
		expect(store.getState().modal).toBe(null);
	});


	it('checking component default render', ()=> {
		expect(window.document).toBeTruthy();
		let renderer = TestUtils.createRenderer();
		renderer.render(<GlobalModalView show={true} type={typeEnum.WARNING} title={title} msg={msg} onDeclined={()=>{}} />);
		let globalModalView = renderer.getRenderOutput();
		expect(globalModalView).toBeTruthy();
	});

});

