import React from 'react';

import i18n from 'nfvo-utils/i18n/i18n.js';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';

const ComponentPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	displayName: React.PropTypes.string,
	description: React.PropTypes.string
});

class SoftwareProductComponentsListView extends React.Component {

	state = {
		localFilter: ''
	};

	static propTypes = {
		isReadOnlyMode: React.PropTypes.bool,
		componentsList: React.PropTypes.arrayOf(ComponentPropType),
		onComponentSelect: React.PropTypes.func
	};

	render() {
		let {componentsList = []} =  this.props;
		return (
			<div className=''>
				{
					componentsList.length > 0 && this.renderComponents()
				}
			</div>
		);
	}

	renderComponents() {
		const {localFilter} = this.state;
		let {isReadOnlyMode} =  this.props;

		return (
			<ListEditorView
				title={i18n('Virtual Function Components')}
				filterValue={localFilter}
				placeholder={i18n('Filter Components')}
				onFilter={filter => this.setState({localFilter: filter})}
				isReadOnlyMode={isReadOnlyMode}>
				{this.filterList().map(component => this.renderComponentsListItem(component))}
			</ListEditorView>
		);
	}

	renderComponentsListItem(component) {
		let {id: componentId, name, displayName, description = ''} = component;
		let {currentSoftwareProduct: {id}, onComponentSelect} = this.props;
		return (
			<ListEditorItemView
				key={name + Math.floor(Math.random() * (100 - 1) + 1).toString()}
				className='list-editor-item-view'
				onSelect={() => onComponentSelect({id, componentId})}>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Component')}</div>
					<div className='name'>{displayName}</div>
				</div>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Description')}</div>
					<div className='description'>{description}</div>
				</div>
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
