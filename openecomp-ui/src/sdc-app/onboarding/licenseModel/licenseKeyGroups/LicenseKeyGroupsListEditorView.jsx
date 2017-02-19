import React from 'react';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';

import LicenseKeyGroupsEditor from './LicenseKeyGroupsEditor.js';
import InputOptions, {other as optionInputOther} from 'nfvo-components/input/inputOptions/InputOptions.jsx';
import {optionsInputValues} from './LicenseKeyGroupsConstants';
import LicenseKeyGroupsConfirmationModal from './LicenseKeyGroupsConfirmationModal.jsx';


class LicenseKeyGroupsListEditorView extends React.Component {
	static propTypes = {
		vendorName: React.PropTypes.string,
		licenseModelId: React.PropTypes.string.isRequired,
		licenseKeyGroupsList: React.PropTypes.array,
		isReadOnlyMode: React.PropTypes.bool.isRequired,
		isDisplayModal: React.PropTypes.bool,
		isModalInEditMode: React.PropTypes.bool,
		onAddLicenseKeyGroupClick: React.PropTypes.func,
		onEditLicenseKeyGroupClick: React.PropTypes.func,
		onDeleteLicenseKeyGroupClick: React.PropTypes.func
	};

	static defaultProps = {
		licenseKeyGroupsList: []
	};

	state = {
		localFilter: ''
	};

	render() {
		let {licenseModelId, vendorName, isReadOnlyMode, isDisplayModal, isModalInEditMode} = this.props;
		let {onAddLicenseKeyGroupClick} = this.props;
		const {localFilter} = this.state;

		return (
			<div className='license-key-groups-list-editor'>
				<ListEditorView
					title={i18n('License Key Groups for {vendorName} License Model', {vendorName})}
					plusButtonTitle={i18n('Add License Key Group')}
					onAdd={onAddLicenseKeyGroupClick}
					filterValue={localFilter}
					onFilter={filter => this.setState({localFilter: filter})}
					isReadOnlyMode={isReadOnlyMode}>
					{this.filterList().map(licenseKeyGroup => (this.renderLicenseKeyGroupListItem(licenseKeyGroup, isReadOnlyMode)))}
				</ListEditorView>
				<Modal show={isDisplayModal} bsSize='large' animation={true} className='license-key-groups-modal'>
					<Modal.Header>
						<Modal.Title>{`${isModalInEditMode ? i18n('Edit License Key Group') : i18n('Create New License Key Group')}`}</Modal.Title>
					</Modal.Header>
					<Modal.Body>
						{
							isDisplayModal && (
								<LicenseKeyGroupsEditor licenseModelId={licenseModelId} isReadOnlyMode={isReadOnlyMode}/>
							)
						}
					</Modal.Body>
				</Modal>
				<LicenseKeyGroupsConfirmationModal licenseModelId={licenseModelId}/>

			</div>
		);
	}

	filterList() {
		let {licenseKeyGroupsList} = this.props;
		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return licenseKeyGroupsList.filter(({name = '', description = '', operationalScope = '', type = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter) || escape(this.extractValue(operationalScope)).match(filter) || escape(type).match(filter);
			});
		}
		else {
			return licenseKeyGroupsList;
		}
	}

	renderLicenseKeyGroupListItem(licenseKeyGroup, isReadOnlyMode) {
		let {id, name, description, operationalScope, type} = licenseKeyGroup;
		let {onEditLicenseKeyGroupClick, onDeleteLicenseKeyGroupClick} = this.props;
		return (
			<ListEditorItemView
				key={id}
				onSelect={() => onEditLicenseKeyGroupClick(licenseKeyGroup)}
				onDelete={() => onDeleteLicenseKeyGroupClick(licenseKeyGroup)}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Name')}</div>
					<div className='text name'>{name}</div>
				</div>

				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Operational Scope')}</div>
					<div className='text operational-scope'>{operationalScope && this.getOperationalScopes(operationalScope)}</div>

					<div className='title'>{i18n('Type')}</div>
					<div className='text type'>{InputOptions.getTitleByName(optionsInputValues, type)}</div>
				</div>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Description')}</div>
					<div className='text description'>{description}</div>
				</div>
			</ListEditorItemView>
		);
	}

	getOperationalScopes(operationalScope) {
		if(operationalScope.choices.toString() === i18n(optionInputOther.OTHER) && operationalScope.other !== '') {
			return operationalScope.other;
		}
		else {
			let allOpScopes = '';
			for (let opScope of operationalScope.choices) {
				allOpScopes += allOpScopes === '' ? InputOptions.getTitleByName(optionsInputValues, opScope) : `, ${InputOptions.getTitleByName(optionsInputValues, opScope)}`;
			}
			return allOpScopes;
		}
	}

	extractValue(item) {
		if (item === undefined) {
			return '';
		} //TODO fix it later

		return item ? item.choice === optionInputOther.OTHER ? item.other : InputOptions.getTitleByName(optionsInputValues, item.choice) : '';
	}
}

export default LicenseKeyGroupsListEditorView;



