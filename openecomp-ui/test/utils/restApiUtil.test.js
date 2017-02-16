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
import $ from 'jquery';
import RestAPIUtil, {makeQueryParams} from 'src/nfvo-utils/RestAPIUtil';

const URL = 'http://bla.ble.blu/';

describe('RestAPIUtil Util class', () => {

	beforeEach(()=> {
		$.ajax = (options) => options;
	});

	it('RestAPIUtil does exist', () => {
		expect(RestAPIUtil).toExist();
	});

	it('RestAPIUtil makeQueryParams does exist', () => {
		expect(makeQueryParams).toExist();
	});

	it('RestAPIUtil makeQueryParams params', () => {
		const pageStart = 1, pageSize = 25;
		const response = makeQueryParams({pagination: {pageStart, pageSize}});
		expect(response.pageStart).toBe(pageStart);
		expect(response.pageSize).toBe(pageSize);
	});

	it('normal basic fetch', () => {
		const response = RestAPIUtil.fetch(URL);
		expect(response).toExist();
	});

	it('no url', function () {
		expect(function () {
			RestAPIUtil.fetch();
		}).toThrow(/url/);
	});

	it('fetch with pagination', () => {
		const pageStart = 1, pageSize = 25;
		const response = RestAPIUtil.fetch(URL, {pagination: {pageStart, pageSize}});
		expect(response.pagination).toExist();
		expect(response.url).toInclude(`?pageStart=${pageStart}&pageSize=${pageSize}`);
	});

	it('fetch with sorting', () => {
		const sortField = 'name', sortDir = 'ASCENDING';
		const response = RestAPIUtil.fetch(URL, {sorting: {sortField, sortDir}});
		expect(response.sorting).toExist();
		expect(response.url).toInclude(`?sortField=${sortField}&sortDir=${sortDir}`);
	});

	it('fetch with filtering', () => {
		const baseFilter = [
			{
				criterionValue: 'service',
				fieldName: 'Brand',
				operator: 'EQUALS',
				type: 'STRING'
			},
			{
				criterionValue: 'resource',
				fieldName: 'Brand',
				operator: 'EQUALS',
				type: 'STRING'
			}
		];
		const response = RestAPIUtil.fetch(URL, {filtering: {filterCriteria: baseFilter, logicalRelation: 'OR'}});
		expect(response.filtering).toExist();
		expect(response.url).toInclude('?filter=');
	});

	it('fetch with qParams', () => {
		const response = RestAPIUtil.fetch(URL, {qParams: {pageStart: 1, pageSize: 10}});
		expect(response.qParams).toExist();
	});

	it('fetch with url on options', () => {
		const response = RestAPIUtil.fetch(URL, {url:'12345', qParams: {pageStart: 1, pageSize: 10}});
		expect(response.qParams).toExist();
	});

	it('fetch with url path param', () => {
		let someData = 'data';
		const response = RestAPIUtil.fetch(`${URL}{someData}/`, {params: {someData}});
		expect(response.url).toInclude(`/${someData}/`);
	});

	it('fetch with url undefined path param', () => {
		const response = RestAPIUtil.fetch(`${URL}{someData}/`, {params: {someData: undefined}});
		expect(response.url).toInclude('/undefined/');
	});

	it('normal basic create', () => {
		const response = RestAPIUtil.create(URL);
		expect(response).toExist();
	});

	it('create with FormData', () => {
		let formData = new FormData();
		formData.append('username', 'Chris');
		const response = RestAPIUtil.create(URL, formData);
		expect(response).toExist();
	});

	it('create with FormData with md5', () => {
		let formData = new FormData();
		formData.append('username', 'Chris');
		const response = RestAPIUtil.create(URL, formData, {md5: true});
		expect(response).toExist();
	});

	it('create with file', () => {
		let progressCallback = () => {};
		const response = RestAPIUtil.create(URL, {}, {progressCallback, fileSize: 123});
		expect(response).toExist();
	});

	it('normal basic save', () => {
		const response = RestAPIUtil.save(URL);
		expect(response).toExist();
	});

	it('normal basic delete', () => {
		const response = RestAPIUtil.destroy(URL);
		expect(response).toExist();
	});

});
