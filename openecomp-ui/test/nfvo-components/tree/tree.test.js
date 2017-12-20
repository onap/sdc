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
import Tree from 'nfvo-components/tree/Tree.jsx';

describe('Tree Module Tests', function () {

	it('Tree view should exist', () => {
		expect(Tree).toBeTruthy();
	});

	it('should render Tree and call onNodeClick', done => {
		const tree = [{id: '123', name: '', parent: ''}];
		let treeView = new Tree({nodes: tree, onNodeClick: () => done()});
		expect(treeView).toBeTruthy();
		treeView.onNodeClick(tree[0]);
	});

});
