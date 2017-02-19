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
import {mapStateToProps} from 'sdc-app/onboarding/softwareProduct/components/monitoring/SoftwareProductComponentsMonitoring.js';
import SoftwareProductComponentsMonitoringView from 'sdc-app/onboarding/softwareProduct/components/monitoring/SoftwareProductComponentsMonitoringView.jsx';

describe('SoftwareProductComponentsMonitoring Module Tests', function () {

	it('should mapper exist', () => {
		expect(mapStateToProps).toExist();
	});

	it('should return empty file names', () => {
		let softwareProduct = {softwareProductEditor: {data: {}}, softwareProductComponents: {monitoring: {}}};
		var results = mapStateToProps({softwareProduct});
		expect(results.trapFilename).toEqual(undefined);
		expect(results.pollFilename).toEqual(undefined);
	});

	it('should return trap file name', () => {
		const monitoring = {
			trapFilename: '123'
		};
		let softwareProduct = {softwareProductEditor: {data: {}}, softwareProductComponents: {monitoring}};
		var results = mapStateToProps({softwareProduct});
		expect(results.trapFilename).toEqual(monitoring.trapFilename);
		expect(results.pollFilename).toEqual(undefined);
	});

	it('should return poll file names', () => {
		const monitoring = {
			pollFilename: '123'
		};
		let softwareProduct = {softwareProductEditor: {data: {}}, softwareProductComponents: {monitoring}};
		var results = mapStateToProps({softwareProduct});
		expect(results.trapFilename).toEqual(undefined);
		expect(results.pollFilename).toEqual(monitoring.pollFilename);

		let renderer = TestUtils.createRenderer();
		renderer.render(<SoftwareProductComponentsMonitoringView {...results} />);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();
	});

	it('should return both file names', () => {
		const monitoring = {
			trapFilename: '1234',
			trapFilename: '123'
		};
		let softwareProduct = {softwareProductEditor: {data: {}}, softwareProductComponents: {monitoring}};
		var results = mapStateToProps({softwareProduct});
		expect(results.trapFilename).toEqual(monitoring.trapFilename);
		expect(results.pollFilename).toEqual(monitoring.pollFilename);

		let renderer = TestUtils.createRenderer();
		renderer.render(<SoftwareProductComponentsMonitoringView {...results} />);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();
	});

	it('should change state to dragging', done => {
		var view = TestUtils.renderIntoDocument(<SoftwareProductComponentsMonitoringView />);
		expect(view.state.dragging).toBe(false);
		view.handleOnDragEnter(false);
		setTimeout(()=> {
			expect(view.state.dragging).toBe(true);
			done();
		}, 100);
	});

	it('should not change state to dragging', done => {
		var view = TestUtils.renderIntoDocument(<SoftwareProductComponentsMonitoringView />);
		expect(view.state.dragging).toBe(false);
		view.handleOnDragEnter(true);
		setTimeout(()=> {
			expect(view.state.dragging).toBe(false);
			done();
		}, 0);
	});

});
