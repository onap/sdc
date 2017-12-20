import React from 'react';
import {storiesOf} from '@kadira/storybook';
import {withKnobs} from '@kadira/storybook-addon-knobs';
import Tree from './Tree.jsx';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';

const stories = storiesOf('Version Tree', module);
stories.addDecorator(withKnobs);

const response = {
	listCount: 6,
	results: [
		{
			'id': '123',
			'name': '1.0',
			'description': 'string',
			'baseId': '',
			'status': 'Draft',
			'creationTime': '2017-06-08T08:55:37.831Z',
			'modificationTime': '2017-06-08T08:55:37.831Z'
		},
		{
			'id': '1234',
			'name': '1.1',
			'description': 'string',
			'baseId': '123',
			'status': 'Draft',
			'creationTime': '2017-06-08T08:55:37.831Z',
			'modificationTime': '2017-06-08T08:55:37.831Z'
		},
		{
			'id': '12345',
			'name': '2.0',
			'description': 'string',
			'baseId': '123',
			'status': 'Draft',
			'creationTime': '2017-06-08T08:55:37.831Z',
			'modificationTime': '2017-06-08T08:55:37.831Z'
		},
		{
			'id': '123456',
			'name': '3.0',
			'description': 'string',
			'baseId': '12345',
			'status': 'Draft',
			'creationTime': '2017-06-08T08:55:37.831Z',
			'modificationTime': '2017-06-08T08:55:37.831Z'
		},
		{
			'id': '1234567',
			'name': '1.2',
			'description': 'string',
			'baseId': '1234',
			'status': 'Draft',
			'creationTime': '2017-06-08T08:55:37.831Z',
			'modificationTime': '2017-06-08T08:55:37.831Z'
		},
		{
			'id': '12345678',
			'name': '2.1',
			'description': 'string',
			'baseId': '12345',
			'status': 'Draft',
			'creationTime': '2017-06-08T08:55:37.831Z',
			'modificationTime': '2017-06-08T08:55:37.831Z'
		},
		{
			'id': '123456789',
			'name': '4.0',
			'description': 'string',
			'baseId': '123456',
			'status': 'Draft',
			'creationTime': '2017-06-08T08:55:37.831Z',
			'modificationTime': '2017-06-08T08:55:37.831Z'
		},
		{
			'id': '12345678910',
			'name': '3.1',
			'description': 'string',
			'baseId': '123456',
			'status': 'Draft',
			'creationTime': '2017-06-08T08:55:37.831Z',
			'modificationTime': '2017-06-08T08:55:37.831Z'
		}
	]
};
const divStyle = { width: '200px', borderStyle: 'solid', borderColor: 'black', border: '1px solid black'};
const tree = response.results.map(item => ({id: item.id, name: item.name, parent: item.baseId}));
const nodeClickHandler = function (node) {
	window.alert(node.name);
};
stories.add('Classic Version Tree', () => (
	<div>
		<Tree nodes={tree} onNodeClick={nodeClickHandler} selectedNodeId={'1234'}/>
	</div>
)).add('Single Version Tree', () => (
	<div>
		<Tree nodes={[tree[0]]} onNodeClick={nodeClickHandler}/>
	</div>
)).add('Single Path Version Tree', () => (
	<div>
		<Tree nodes={[tree[0], tree[1]]} onNodeClick={nodeClickHandler}/>
	</div>
)).add('Empty Tree', () => (
	<div>
		<Tree nodes={[]}/>
	</div>
)).add('Add Tree in Version Page Frame', () => (
	<div style={divStyle}>
		Tree wider than frame<br/><br/><br/>
		<Tree
			name={'versions-tree'}
			width={200}
			nodes={tree}
			onRenderedBeyondWidth={() => {console.log('rendered beyond width')}}
			allowScaleWidth={false}
			onNodeClick={nodeClickHandler}/>
	</div>
));
