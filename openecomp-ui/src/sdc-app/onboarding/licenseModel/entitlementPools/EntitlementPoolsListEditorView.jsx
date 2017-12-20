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

import EntitlementPoolsEditor from './EntitlementPoolsEditor.js';
import {extractUnits} from './EntitlementPoolsConstants';

class EntitlementPoolsListEditorView extends React.Component {
	static propTypes = {
		vendorName: PropTypes.string,
		licenseModelId: PropTypes.string.isRequired,
		entitlementPoolsList: PropTypes.array,
		isReadOnlyMode: PropTypes.bool.isRequired,
		isDisplayModal: PropTypes.bool,
		isModalInEditMode: PropTypes.bool,
		onAddEntitlementPoolClick: PropTypes.func,
		onEditEntitlementPoolClick: PropTypes.func,
		onDeleteEntitlementPool: PropTypes.func,
	};

	static defaultProps = {
		entitlementPoolsList: []
	};

	state = {
		localFilter: ''
	};

	render() {
		let {licenseModelId, isReadOnlyMode, isDisplayModal, isModalInEditMode, version} = this.props;
		let {onAddEntitlementPoolClick} = this.props;
		const {localFilter} = this.state;

		return (
			<div className='license-model-list-editor entitlement-pools-list-editor'>
				<ListEditorView
					title={i18n('Entitlement Pools')}
					plusButtonTitle={i18n('Add Entitlement Pool')}
					onAdd={onAddEntitlementPoolClick}
					filterValue={localFilter}
					onFilter={value => this.setState({localFilter: value})}
					isReadOnlyMode={isReadOnlyMode}>
					{this.filterList().map(entitlementPool => this.renderEntitlementPoolListItem(entitlementPool, isReadOnlyMode))}
				</ListEditorView>
				<Modal show={isDisplayModal} bsSize='large' animation={true} className='onborading-modal license-model-modal entitlement-pools-modal'>
					<Modal.Header>
						<Modal.Title>{`${isModalInEditMode ? i18n('Edit Entitlement Pool') : i18n('Create New Entitlement Pool')}`}</Modal.Title>
					</Modal.Header>
					<Modal.Body>
						{
							isDisplayModal && (
								<EntitlementPoolsEditor version={version} licenseModelId={licenseModelId} isReadOnlyMode={isReadOnlyMode}/>
							)
						}
					</Modal.Body>
				</Modal>
			</div>
		);
	}

	filterList() {
		let {entitlementPoolsList} = this.props;
		let {localFilter} = this.state;
		if(localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return entitlementPoolsList.filter(({name = '', description = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter);
			});
		}
		else {
			return entitlementPoolsList;
		}
	}

	renderEntitlementPoolListItem(entitlementPool, isReadOnlyMode) {
		let {id, name, description, thresholdValue, thresholdUnits} = entitlementPool;
		let {onEditEntitlementPoolClick, onDeleteEntitlementPool} = this.props;
		return (
			<ListEditorItemView
				key={id}
				onSelect={() => onEditEntitlementPoolClick(entitlementPool)}
				onDelete={() => onDeleteEntitlementPool(entitlementPool)}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}>
				<div className='list-editor-item-view-field'>

					<div className='title'>{i18n('Name')}</div>
					<div ><div className='textEllipses text name'>{name}</div></div>
				</div>

				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Entitlement')}</div>
					<div className='entitlement-pools-count'>{thresholdValue && `${thresholdValue} ${extractUnits(thresholdUnits)}`}</div>
				</div>

				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Description')}</div>
					<div className='text description'>{description}</div>
				</div>
			</ListEditorItemView>
		);
	}

}

export default EntitlementPoolsListEditorView;

export function generateConfirmationMsg(entitlementPoolToDelete) {
	let poolName = entitlementPoolToDelete ? entitlementPoolToDelete.name : '';
	let msg = i18n('Are you sure you want to delete "{poolName}"?', {poolName: poolName});
	let subMsg = entitlementPoolToDelete
	&& entitlementPoolToDelete.referencingFeatureGroups
	&& entitlementPoolToDelete.referencingFeatureGroups.length > 0 ?
		i18n('This entitlement pool is associated with one or more feature groups') :
		'';
	return (
		<div>
			<p>{msg}</p>
			<p>{subMsg}</p>
		</div>
	);
}
