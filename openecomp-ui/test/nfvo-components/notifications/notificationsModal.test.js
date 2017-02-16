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
import store from 'sdc-app/AppStore.js';
import ConnectedNotificationModal, {NotificationModal} from 'nfvo-components/notifications/NotificationModal.jsx';
import NotificationConstants from 'nfvo-components/notifications/NotificationConstants.js';

const title = 'test title';
const msg = 'test msg';

describe('Notification Modal Mapper and View Class: ', function () {

	it('notification should show with type error', done => {
		store.dispatch({type: NotificationConstants.NOTIFY_ERROR, data: {title, msg}});
		setTimeout(()=> {
			expect(store.getState().notification).toExist();
			expect(store.getState().notification.type).toBe('error');
			done();
		}, 0);
	});

	it('notification should show with type default', done => {
		store.dispatch({type: NotificationConstants.NOTIFY_INFO, data: {title, msg}});
		setTimeout(()=> {
			expect(store.getState().notification).toExist();
			expect(store.getState().notification.type).toBe('default');
			done();
		}, 0);
	});

	it('notification should show with type warning', done => {
		store.dispatch({type: NotificationConstants.NOTIFY_WARNING, data: {title, msg}});
		setTimeout(()=> {
			expect(store.getState().notification).toExist();
			expect(store.getState().notification.type).toBe('warning');
			done();
		}, 0);
	});

	it('notification should show with type success', done => {
		store.dispatch({type: NotificationConstants.NOTIFY_SUCCESS, data: {title, msg}});
		setTimeout(()=> {
			expect(store.getState().notification).toExist();
			expect(store.getState().notification.type).toBe('success');
			done();
		}, 0);
	});

	it('notification should show with type success with connected component', done => {
		store.dispatch({type: NotificationConstants.NOTIFY_SUCCESS, data: {title, msg}});
		setTimeout(()=> {
			expect(store.getState().notification).toExist();
			expect(store.getState().notification.type).toBe('success');
			let renderer = TestUtils.createRenderer();
			renderer.render(<ConnectedNotificationModal store={store}/>);
			let renderedOutput = renderer.getRenderOutput();
			expect(renderedOutput).toExist();
			done();
		}, 0);
	});

	it('notification should hide with connected component', done => {
		setTimeout(()=> {
			expect(store.getState().notification).toNotExist();
			let renderer = TestUtils.createRenderer();
			renderer.render(<ConnectedNotificationModal store={store}/>);
			let renderedOutput = renderer.getRenderOutput();
			expect(renderedOutput).toExist();
			done();
		}, 0);
		store.dispatch({type: NotificationConstants.NOTIFY_CLOSE});
	});

	it('notification should hide', done => {
		store.dispatch({type: NotificationConstants.NOTIFY_CLOSE});
		setTimeout(()=> {
			expect(store.getState().notification).toNotExist();
			done();
		}, 0);
	});

	it('NotificationModal should not render', ()=> {
		let renderer = TestUtils.createRenderer();
		renderer.render(<NotificationModal show={false} title={title} msg={msg} type='error'/>);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();
	});

	it('NotificationModal basic default render', ()=> {
		expect(window.document).toExist();
		let document = TestUtils.renderIntoDocument(
			<NotificationModal show={true} title={title} msg={msg} type='default' onCloseClick={()=>{}}/>
		);
		var result = TestUtils.findAllInRenderedTree(document, element => element.props.className === 'notification-modal primary');
		expect(result.length).toBeGreaterThan(0);
	});

	it('NotificationModal basic error render', ()=> {
		expect(window.document).toExist();
		let document = TestUtils.renderIntoDocument(
			<NotificationModal show={true} title={title} msg={msg} type='error' onCloseClick={()=>{}}/>
		);
		var result = TestUtils.findAllInRenderedTree(document, element => element.props.className === 'notification-modal danger');
		expect(result.length).toBeGreaterThan(0);
	});

	it('NotificationModal basic warning render', ()=> {
		expect(window.document).toExist();
		let document = TestUtils.renderIntoDocument(
			<NotificationModal show={true} title={title} msg={msg} type='warning' onCloseClick={()=>{}}/>
		);
		var result = TestUtils.findAllInRenderedTree(document, element => element.props.className === 'notification-modal warning');
		expect(result.length).toBeGreaterThan(0);
	});

	it('NotificationModal basic success render', ()=> {
		expect(window.document).toExist();
		let document = TestUtils.renderIntoDocument(
			<NotificationModal show={true} title={title} msg={msg} type='success' onCloseClick={()=>{}}/>
		);
		var result = TestUtils.findAllInRenderedTree(document, element => element.props.className === 'notification-modal success');
		expect(result.length).toBeGreaterThan(0);
	});
});
