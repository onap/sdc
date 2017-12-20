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
import {mapStateToProps} from 'sdc-app/onboarding/softwareProduct/components/monitoring/SoftwareProductComponentsMonitoring.js';
import SoftwareProductComponentsMonitoringView from 'sdc-app/onboarding/softwareProduct/components/monitoring/SoftwareProductComponentsMonitoringView.jsx';

import {VSPComponentsMonitoringViewFactory, trap, poll, ves} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsMonitoringFactories.js';
import VersionControllerUtilsFactory from 'test-utils/factories/softwareProduct/VersionControllerUtilsFactory.js';

const version = VersionControllerUtilsFactory.build();


describe('SoftwareProductComponentsMonitoring Module Tests', function () {

	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('should return empty file names', () => {
		let softwareProduct = {softwareProductEditor: {data: {...version}}, softwareProductComponents: {monitoring: {}}};
		var results = mapStateToProps({softwareProduct});
		expect(results.filenames[trap]).toEqual(undefined);
		expect(results.filenames[poll]).toEqual(undefined);
		expect(results.filenames[ves]).toEqual(undefined);
	});

	it('should return trap file name', () => {
		const monitoring = VSPComponentsMonitoringViewFactory.build({}, {createTrap: true});
		let softwareProduct = {softwareProductEditor: {data: {...version}}, softwareProductComponents: {monitoring}};
		var results = mapStateToProps({softwareProduct});
		expect(results.filenames[trap]).toEqual(monitoring[trap]);
		expect(results.filenames[poll]).toEqual(undefined);
		expect(results.filenames[ves]).toEqual(undefined);
	});

	it('should return ves events file name', () => {
		const monitoring = VSPComponentsMonitoringViewFactory.build({}, {createVes: true});
		let softwareProduct = {softwareProductEditor: {data: {...version}}, softwareProductComponents: {monitoring}};
		var results = mapStateToProps({softwareProduct});
		expect(results.filenames[ves]).toEqual(monitoring[ves]);
		expect(results.filenames[poll]).toEqual(undefined);
		expect(results.filenames[trap]).toEqual(undefined);
	});

	it('should return poll file names', () => {
		const monitoring = VSPComponentsMonitoringViewFactory.build({}, {createPoll: true});
		let softwareProduct = {softwareProductEditor: {data: {...version}}, softwareProductComponents: {monitoring}};
		var results = mapStateToProps({softwareProduct});
		expect(results.filenames[poll]).toEqual(monitoring[poll]);
		expect(results.filenames[trap]).toEqual(undefined);
		expect(results.filenames[ves]).toEqual(undefined);

		let renderer = TestUtils.createRenderer();
		renderer.render(<SoftwareProductComponentsMonitoringView {...results} />);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

	it('should return all file names', () => {
		const monitoring =  VSPComponentsMonitoringViewFactory.build({}, {createTrap: true, createVes: true, createPoll: true});
		let softwareProduct = {softwareProductEditor: {data: {...version}}, softwareProductComponents: {monitoring}};
		var results = mapStateToProps({softwareProduct});
		expect(results.filenames[poll]).toEqual(monitoring[poll]);
		expect(results.filenames[trap]).toEqual(monitoring[trap]);
		expect(results.filenames[ves]).toEqual(monitoring[ves]);

		let renderer = TestUtils.createRenderer();
		renderer.render(<SoftwareProductComponentsMonitoringView {...results} />);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

	it('should change state to dragging', () => {
		var view = TestUtils.renderIntoDocument(<SoftwareProductComponentsMonitoringView />);
		expect(view.state.dragging).toBe(false);
		view.handleOnDragEnter(false);
		expect(view.state.dragging).toBe(true);
	});

	it('should not change state to dragging', () => {
		var view = TestUtils.renderIntoDocument(<SoftwareProductComponentsMonitoringView />);
		expect(view.state.dragging).toBe(false);
		view.handleOnDragEnter(true);
		expect(view.state.dragging).toBe(false);
	});

});
