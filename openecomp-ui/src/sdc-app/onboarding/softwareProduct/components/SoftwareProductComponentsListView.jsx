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

const ComponentPropType = PropTypes.shape({
	id: PropTypes.string,
	name: PropTypes.string,
	displayName: PropTypes.string,
	description: PropTypes.string
});

class SoftwareProductComponentsListView extends React.Component {

	state = {
		localFilter: ''
	};

	static propTypes = {
		isReadOnlyMode: PropTypes.bool,
		componentsList: PropTypes.arrayOf(ComponentPropType),
		onComponentSelect: PropTypes.func
	};

	render() {
		let {componentsList = [], isManual} =  this.props;
		return (
			<div className=''>
				{
					(componentsList.length > 0 || isManual) && this.renderComponents()
				}
			</div>
		);
	}

	renderComponents() {
		const {localFilter} = this.state;
		const {isManual, onAddComponent, isReadOnlyMode, version, currentSoftwareProduct: {id: softwareProductId}, componentsList } = this.props;
		return (
			<ListEditorView
				title={i18n('Virtual Function Components')}
				filterValue={localFilter}
				placeholder={i18n('Filter Components')}
				onFilter={value => this.setState({localFilter: value})}
				isReadOnlyMode={isReadOnlyMode || !!this.filterList().length}
				plusButtonTitle={i18n('Add Component')}
				onAdd={isManual && componentsList.length === 0 ? () => onAddComponent(softwareProductId, version) : false}
				twoColumns>
				{this.filterList().map(component => this.renderComponentsListItem(component))}
			</ListEditorView>
		);
	}

	renderComponentsListItem(component) {
		let {id: componentId, name, displayName, description = ''} = component;
		let {currentSoftwareProduct: {id}, onComponentSelect, version} = this.props;
		return (
			<ListEditorItemView
				key={name + Math.floor(Math.random() * (100 - 1) + 1).toString()}
				className='list-editor-item-view'				
				onSelect={() => onComponentSelect({id, componentId, version})}>
				<ListEditorItemViewField>
					<div className='name'>{displayName}</div>
				</ListEditorItemViewField>
				<ListEditorItemViewField>
					<div className='description'>{description}</div>
				</ListEditorItemViewField>
			</ListEditorItemView>
		);
	}

	filterList() {
		let {componentsList = []} = this.props;

		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return componentsList.filter(({displayName = '', description = ''}) => {
				return escape(displayName).match(filter) || escape(description).match(filter);
			});
		}
		else {
			return componentsList;
		}
	}
}

export default SoftwareProductComponentsListView;
