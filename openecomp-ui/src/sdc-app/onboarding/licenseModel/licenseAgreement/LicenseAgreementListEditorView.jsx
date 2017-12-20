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
import LicenseAgreementEditor from './LicenseAgreementEditor.js';
import {extractValue} from './LicenseAgreementConstants';

class LicenseAgreementListEditorView extends React.Component {
	static propTypes = {
		vendorName: PropTypes.string,
		licenseModelId: PropTypes.string.isRequired,
		licenseAgreementList: PropTypes.array,
		isReadOnlyMode: PropTypes.bool.isRequired,
		isDisplayModal: PropTypes.bool,
		isModalInEditMode: PropTypes.bool,
		onAddLicenseAgreementClick: PropTypes.func,
		onEditLicenseAgreementClick: PropTypes.func,
		onDeleteLicenseAgreement: PropTypes.func,
	};

	static defaultProps = {
		licenseAgreementList: []
	};

	state = {
		localFilter: ''
	};

	render() {
		const {licenseModelId, isReadOnlyMode, isDisplayModal, isModalInEditMode, version} = this.props;
		const {onAddLicenseAgreementClick} = this.props;
		const {localFilter} = this.state;

		return (
			<div className='license-model-list-editor license-agreement-list-editor'>
				<ListEditorView
					title={i18n('License Agreements')}
					plusButtonTitle={i18n('Add License Agreement')}
					onAdd={() => onAddLicenseAgreementClick(version)}
					filterValue={localFilter}
					onFilter={value => this.setState({localFilter: value})}
					isReadOnlyMode={isReadOnlyMode}>
					{this.filterList().map(licenseAgreement => this.renderLicenseAgreementListItem(licenseAgreement, isReadOnlyMode, version))}
				</ListEditorView>
				<Modal show={isDisplayModal} bsSize='large' animation={true} className='onborading-modal license-model-modal license-agreement-modal'>
					<Modal.Header>
						<Modal.Title>{`${isModalInEditMode ? i18n('Edit License Agreement') : i18n('Create New License Agreement')}`}</Modal.Title>
					</Modal.Header>
					<Modal.Body>
						{
							isDisplayModal && (
								<LicenseAgreementEditor version={version} licenseModelId={licenseModelId} isReadOnlyMode={isReadOnlyMode} />
							)
						}
					</Modal.Body>
				</Modal>
			</div>
		);
	}

	filterList() {
		let {licenseAgreementList} = this.props;
		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return licenseAgreementList.filter(({name = '', description = '', licenseTerm = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter) || escape(extractValue(licenseTerm)).match(filter);
			});
		}
		else {
			return licenseAgreementList;
		}
	}

	renderLicenseAgreementListItem(licenseAgreement, isReadOnlyMode, version) {
		let {id, name, description, licenseTerm, featureGroupsIds = []} = licenseAgreement;
		let {onEditLicenseAgreementClick, onDeleteLicenseAgreement} = this.props;
		return (
			<ListEditorItemView
				key={id}
				onSelect={() => onEditLicenseAgreementClick(licenseAgreement, version)}
				onDelete={() => onDeleteLicenseAgreement(licenseAgreement, version)}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Name')}</div>
					<div className='text name'>{name}</div>
				</div>
				<div className='list-editor-item-view-field'>
					<div className='list-editor-item-view-field-tight'>
						<div className='title'>{i18n('Type')}</div>
						<div className='text type'>{extractValue(licenseTerm)}</div>
					</div>
					<div className='list-editor-item-view-field-tight'>
						<div className='title'>{i18n('Feature')}</div>
						<div className='title'>{i18n('Groups')}</div>
						<div className='feature-groups-count'>{featureGroupsIds.length}</div>
					</div>
				</div>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Description')}</div>
					<div className='text description'>{description}</div>
				</div>
			</ListEditorItemView>
		);
	}
}

export default LicenseAgreementListEditorView;
