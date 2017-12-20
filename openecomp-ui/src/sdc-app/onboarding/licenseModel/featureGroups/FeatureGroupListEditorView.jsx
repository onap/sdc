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
import Modal from 'nfvo-components/modal/Modal.jsx';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';

import FeatureGroupEditor from './FeatureGroupEditor.js';

class FeatureGroupListEditorView extends React.Component {
	static propTypes = {
		vendorName: PropTypes.string,
		licenseModelId: PropTypes.string.isRequired,
		featureGroupsModal: PropTypes.shape({
			show: PropTypes.bool,
			editMode: PropTypes.bool
		}),
		isReadOnlyMode: PropTypes.bool.isRequired,
		onAddFeatureGroupClick: PropTypes.func,
		onEditFeatureGroupClick: PropTypes.func,
		onDeleteFeatureGroupClick: PropTypes.func,
		onCancelFeatureGroupsEditor: PropTypes.func,
		featureGroupsList: PropTypes.array
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
		let {licenseModelId, featureGroupsModal, isReadOnlyMode, onAddFeatureGroupClick, version} = this.props;
		const {localFilter} = this.state;
		return (
			<div className='license-model-list-editor feature-groups-list-editor'>
				<ListEditorView
					title={i18n('Feature Groups')}
					plusButtonTitle={i18n('Add Feature Group')}
					filterValue={localFilter}
					onFilter={value => this.setState({localFilter: value})}
					onAdd={() => onAddFeatureGroupClick(version)}
					isReadOnlyMode={isReadOnlyMode}>
					{this.filterList().map(listItem => this.renderFeatureGroupListItem(listItem, isReadOnlyMode, version))}
				</ListEditorView>
				{featureGroupsModal.show && <Modal show={featureGroupsModal.show} bsSize='large' animation={true}
					       className='onborading-modal license-model-modal feature-group-modal'>
						<Modal.Header>
							<Modal.Title>{`${featureGroupsModal.editMode ? i18n('Edit Feature Group') : i18n('Create New Feature Group')}`}</Modal.Title>
						</Modal.Header>
						<Modal.Body>
							<FeatureGroupEditor
								version={version}
								licenseModelId={licenseModelId}
								isReadOnlyMode={isReadOnlyMode}/>
						</Modal.Body>
					</Modal>
				}

			</div>
		);
	}


	renderFeatureGroupListItem(listItem, isReadOnlyMode, version) {
		let {name, description, manufacturerReferenceNumber, entitlementPoolsIds = [], licenseKeyGroupsIds = []} = listItem;
		return (
			<ListEditorItemView
				key={listItem.id}
				onDelete={() => this.deleteFeatureGroupItem(listItem, version)}
				onSelect={() => this.editFeatureGroupItem(listItem, version)}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Name')}</div>
					<div className='text name'>{name}</div>
				</div>

				<div className='list-editor-item-view-field smaller-field'>
					<div className='feature-groups-count-field'>
						<div className='title'>{i18n('EP')}</div>
						<div className='feature-groups-count-ep'>{entitlementPoolsIds.length || 0}</div>
					</div>
				</div>
				<div className='list-editor-item-view-field smaller-field'>
					<div className='feature-groups-count-field'>
						<div className='title'>{i18n('LKG')}</div>
						<div className='feature-groups-count-lk'>{licenseKeyGroupsIds.length || 0}</div>
					</div>
				</div>

				<div className='list-editor-item-view-field'>
					<div className='feature-groups-count-field'>
						<div className='title title-no-wrap'>{i18n('Manufacturer Reference Number')}</div>
						<div className='feature-groups-mrn-ep'>{manufacturerReferenceNumber}</div>
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

	editFeatureGroupItem(featureGroup, version) {
		this.props.onEditFeatureGroupClick(featureGroup, version);
	}

	deleteFeatureGroupItem(featureGroup, version) {
		this.props.onDeleteFeatureGroupClick(featureGroup, version);
	}
}

export default FeatureGroupListEditorView;

export function generateConfirmationMsg(featureGroupToDelete) {
	let name = featureGroupToDelete ? featureGroupToDelete.name : '';
	let msg = i18n('Are you sure you want to delete "{name}"?', {name: name});
	let subMsg = featureGroupToDelete.referencingLicenseAgreements
	&& featureGroupToDelete.referencingLicenseAgreements.length > 0 ?
		i18n('This feature group is associated with one ore more license agreements') :
		'';
	return (
		<div>
			<p>{msg}</p>
			<p>{subMsg}</p>
		</div>
	);
}
