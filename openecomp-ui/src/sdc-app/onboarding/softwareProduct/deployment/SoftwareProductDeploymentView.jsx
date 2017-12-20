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
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import ListEditorItemViewField from 'nfvo-components/listEditor/ListEditorItemViewField.jsx';

export default class SoftwareProductDeployment extends React.Component {
	state = {
		localFilter: ''
	};

	static propTypes = {
		onAddDeployment: PropTypes.func.isRequired,
		onDeleteDeployment: PropTypes.func.isRequired,
		onEditDeployment: PropTypes.func.isRequired,
		isReadOnlyMode: PropTypes.bool.isRequired
	};

	render() {
		return (
			<div>
				{this.renderList()}
			</div>
		);
	}

	renderList() {
		let {onAddDeployment, isReadOnlyMode, componentsList} = this.props;
		return (
			<ListEditorView
				plusButtonTitle={i18n('Add Deployment Flavor')}
				filterValue={this.state.localFilter}
				placeholder={i18n('Filter Deployment')}
				onAdd={() => onAddDeployment(componentsList)}
				isReadOnlyMode={isReadOnlyMode}
				title={i18n('Deployment Flavors')}
				onFilter={value => this.setState({localFilter: value})}
				twoColumns>
				{this.filterList().map(deploymentFlavor => this.renderListItem(deploymentFlavor, isReadOnlyMode))}
			</ListEditorView>
		);
	}

	renderListItem(deploymentFlavor, isReadOnlyMode) {
		let {id, model, description} = deploymentFlavor;
		let {onEditDeployment, onDeleteDeployment, componentsList} =  this.props;
		return (
			<ListEditorItemView
				key={id}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}
				onSelect={() => onEditDeployment(deploymentFlavor, componentsList)}
				onDelete={() => onDeleteDeployment(deploymentFlavor)}>
				<ListEditorItemViewField>
					<div className='model'>{model}</div>
				</ListEditorItemViewField>
				<ListEditorItemViewField>
					<div className='description'>{description}</div>
				</ListEditorItemViewField>
			</ListEditorItemView>
		);
	}

	filterList() {
		let {deploymentFlavors} = this.props;
		let {localFilter} = this.state;

		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return deploymentFlavors.filter(({model = '', description = ''}) => {
				return escape(model).match(filter) || escape(description).match(filter);
			});
		}
		else {
			return deploymentFlavors;
		}
	}
}
