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
import {mount} from 'enzyme';
import {cloneAndSet} from 'test-utils/Util.js';
import ActivityLogView, {ActivityListItem} from 'sdc-app/common/activity-log/ActivityLogView.jsx';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ActivityLogActionHelper from 'sdc-app/common/activity-log/ActivityLogActionHelper.js';
import {mapStateToProps} from 'sdc-app/common/activity-log/ActivityLog.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import mockRest from 'test-utils/MockRest.js';
import {ActivityLogStoreFactory} from 'test-utils/factories/activity-log/ActivityLogFactories.js';
import VersionControllerUtilsFactory from 'test-utils/factories/softwareProduct/VersionControllerUtilsFactory.js';

describe('Activity Log Module Tests', function () {
	const LICENSE_MODEL_ID = '555';
	const version = VersionControllerUtilsFactory.build().version;

	it('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('Loads Activity Log and renders into jsx', () => {
		const store = storeCreator();
		const dispatch = store.dispatch;
		let ActivityLogList = ActivityLogStoreFactory.buildList(1);
		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.activityLog', ActivityLogList);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/activity-logs/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: ActivityLogList};
		});

		return ActivityLogActionHelper.fetchActivityLog(dispatch, {itemId: LICENSE_MODEL_ID, versionId: version.id}).then(() => {
			const state = store.getState();
			expect(state).toEqual(expectedStore);
			const props = mapStateToProps(state);
			expect(props.activities).toEqual(ActivityLogList);
			const wrapper = mount(<ActivityLogView {...props}/>);
			expect(wrapper).toBeTruthy();
		});
	});

	it('Tests Activity Log filter and sorting abilities', () => {
		const firstDate = new Date();
		const secondDate = new Date();
		secondDate.setDate(firstDate.getDate() - 1);

		const firstTimestamp = firstDate.getTime();
		const secondTimestamp = secondDate.getTime();

		let firstActivity = ActivityLogStoreFactory.build({user: 'first', timestamp: firstTimestamp});
		let secondActivity = ActivityLogStoreFactory.build({user: 'second', timestamp: secondTimestamp, status: {success: false, message: 'error'}});
		let props = mapStateToProps({licenseModel: {activityLog: [firstActivity, secondActivity]}});
		const wrapper = mount(<ActivityLogView {...props}/>);
		expect(wrapper.find(ActivityListItem).length).toEqual(3); // Includes Header component

		const firstInstance = wrapper.find(ActivityListItem).at(1);
		const firstInstanceProps = firstInstance.props();
		expect(firstInstanceProps.activity.timestamp).toEqual(secondTimestamp); // Default sorting is descending

		const header = wrapper.find(ActivityListItem).at(0);
		header.props().onSort();
		const newFirstInstance = wrapper.find(ActivityListItem).at(1);
		const newFirstInstanceProps = newFirstInstance.props();
		expect(newFirstInstanceProps.activity.timestamp).toEqual(firstTimestamp);

		const listEditor = wrapper.find(ListEditorView);
		listEditor.props().onFilter('second');
		expect(wrapper.find(ActivityListItem).length).toEqual(2);
		expect(wrapper.find(ActivityListItem).at(1).props().activity.user).toEqual('second');
	});
});
