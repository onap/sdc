import React from 'react';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';

import FeatureGroupEditor from './FeatureGroupEditor.js';
import FeatureGroupsConfirmationModal from './FeatureGroupsConfirmationModal.jsx';

class FeatureGroupListEditorView extends React.Component {
	static propTypes = {
		vendorName: React.PropTypes.string,
		licenseModelId: React.PropTypes.string.isRequired,
		featureGroupsModal: React.PropTypes.shape({
			show: React.PropTypes.bool,
			editMode: React.PropTypes.bool
		}),
		isReadOnlyMode: React.PropTypes.bool.isRequired,
		onAddFeatureGroupClick: React.PropTypes.func,
		onEditFeatureGroupClick: React.PropTypes.func,
		onDeleteFeatureGroupClick: React.PropTypes.func,
		onCancelFeatureGroupsEditor: React.PropTypes.func,
		featureGroupsList: React.PropTypes.array
	};

	static defaultProps = {
		featureGroupsList: [],
		featureGroupsModal: {
			show: false,
			editMode: false
		}
	};

	state = {
		localFilter: ''
	};

	render() {
		let {vendorName, licenseModelId, featureGroupsModal, isReadOnlyMode, onAddFeatureGroupClick} = this.props;
		const {localFilter} = this.state;

		return (
			<div className='feature-groups-list-editor'>
				<ListEditorView
					title={i18n('Feature Groups for {vendorName} License Model', {vendorName})}
					plusButtonTitle={i18n('Add Feature Group')}
					filterValue={localFilter}
					onFilter={filter => this.setState({localFilter: filter})}
					onAdd={() => onAddFeatureGroupClick()}
					isReadOnlyMode={isReadOnlyMode}>
					{this.filterList().map(listItem => this.renderFeatureGroupListItem(listItem, isReadOnlyMode))}
				</ListEditorView>
				<Modal show={featureGroupsModal.show} bsSize='large' animation={true} className='feature-group-modal'>
					<Modal.Header>
						<Modal.Title>{`${featureGroupsModal.editMode ? i18n('Edit Feature Group') : i18n('Create New Feature Group')}`}</Modal.Title>
					</Modal.Header>
					<Modal.Body>
						<FeatureGroupEditor
							onCancel={() => this.closeFeatureGroupsEditor()}
							licenseModelId={licenseModelId}
							isReadOnlyMode={isReadOnlyMode}/>
					</Modal.Body>
				</Modal>

				<FeatureGroupsConfirmationModal licenseModelId={licenseModelId}/>

			</div>
		);
	}


	renderFeatureGroupListItem(listItem, isReadOnlyMode) {
		let {name, description, entitlementPoolsIds = [], licenseKeyGroupsIds = []} = listItem;
		return (
			<ListEditorItemView
				key={listItem.id}
				onDelete={() => this.deleteFeatureGroupItem(listItem)}
				onSelect={() => this.editFeatureGroupItem(listItem)}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Name')}</div>
					<div className='text name'>{name}</div>
				</div>

				<div className='list-editor-item-view-field'>
					<div className='feature-groups-count-field'>
						<div className='title'>{i18n('Entitlement')}</div>
						<div className='title'>{i18n('Pools')}</div>
						<div className='feature-groups-count-ep'>{entitlementPoolsIds.length || 0}</div>
					</div>
					<div className='feature-groups-count-field'>
						<div className='title'>{i18n('License key')}</div>
						<div className='title'>{i18n('Groups')}</div>
						<div className='feature-groups-count-lk'>{licenseKeyGroupsIds.length || 0}</div>
					</div>
				</div>

				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Description')}</div>
					<div className='text description'>{description}</div>
				</div>

			</ListEditorItemView>
		);
	}

	filterList() {
		let {featureGroupsList} = this.props;
		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return featureGroupsList.filter(({name = '', description = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter);
			});
		}
		else {
			return featureGroupsList;
		}
	}

	closeFeatureGroupsEditor() {
		this.props.onCancelFeatureGroupsEditor();
	}

	editFeatureGroupItem(featureGroup) {
		this.props.onEditFeatureGroupClick(featureGroup);
	}

	deleteFeatureGroupItem(featureGroup) {
		this.props.onDeleteFeatureGroupClick(featureGroup);
	}
}

export default FeatureGroupListEditorView;
