import React from 'react';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import LicenseAgreementEditor from './LicenseAgreementEditor.js';
import InputOptions, {other as optionInputOther} from 'nfvo-components/input/inputOptions/InputOptions.jsx';
import {optionsInputValues} from './LicenseAgreementConstants';
import LicenseAgreementConfirmationModal from './LicenseAgreementConfirmationModal.jsx';


class LicenseAgreementListEditorView extends React.Component {
	static propTypes = {
		vendorName: React.PropTypes.string,
		licenseModelId: React.PropTypes.string.isRequired,
		licenseAgreementList: React.PropTypes.array,
		isReadOnlyMode: React.PropTypes.bool.isRequired,
		isDisplayModal: React.PropTypes.bool,
		isModalInEditMode: React.PropTypes.bool,
		onAddLicenseAgreementClick: React.PropTypes.func,
		onEditLicenseAgreementClick: React.PropTypes.func,
		onDeleteLicenseAgreement: React.PropTypes.func,
		onCallVCAction: React.PropTypes.func
	};

	static defaultProps = {
		licenseAgreementList: []
	};

	state = {
		localFilter: ''
	};

	render() {
		const {licenseModelId, vendorName, isReadOnlyMode, isDisplayModal, isModalInEditMode} = this.props;
		const {onAddLicenseAgreementClick} = this.props;
		const {localFilter} = this.state;

		return (
			<div className='license-agreement-list-editor'>
					<ListEditorView
						title={i18n('License Agreements for {vendorName} License Model', {vendorName})}
						plusButtonTitle={i18n('Add License Agreement')}
						onAdd={onAddLicenseAgreementClick}
						filterValue={localFilter}
						onFilter={filter => this.setState({localFilter: filter})}
						isReadOnlyMode={isReadOnlyMode}>
						{this.filterList().map(licenseAgreement => this.renderLicenseAgreementListItem(licenseAgreement, isReadOnlyMode))}
					</ListEditorView>
				<Modal show={isDisplayModal} bsSize='large' animation={true} className='license-agreement-modal'>
					<Modal.Header>
						<Modal.Title>{`${isModalInEditMode ? i18n('Edit License Agreement') : i18n('Create New License Agreement')}`}</Modal.Title>
					</Modal.Header>
					<Modal.Body>
						{
							isDisplayModal && (
								<LicenseAgreementEditor licenseModelId={licenseModelId} isReadOnlyMode={isReadOnlyMode} />
							)
						}
					</Modal.Body>
				</Modal>
				<LicenseAgreementConfirmationModal licenseModelId={licenseModelId}/>

			</div>
		);
	}

	filterList() {
		let {licenseAgreementList} = this.props;
		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return licenseAgreementList.filter(({name = '', description = '', licenseTerm = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter) || escape(this.extractValue(licenseTerm)).match(filter);
			});
		}
		else {
			return licenseAgreementList;
		}
	}

	renderLicenseAgreementListItem(licenseAgreement, isReadOnlyMode) {
		let {id, name, description, licenseTerm, featureGroupsIds = []} = licenseAgreement;
		let {onEditLicenseAgreementClick, onDeleteLicenseAgreement} = this.props;
		return (
			<ListEditorItemView
				key={id}
				onSelect={() => onEditLicenseAgreementClick(licenseAgreement)}
				onDelete={() => onDeleteLicenseAgreement(licenseAgreement)}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Name')}</div>
					<div className='text name'>{name}</div>
				</div>
				<div className='list-editor-item-view-field'>
					<div className='list-editor-item-view-field-tight'>
						<div className='title'>{i18n('Type')}</div>
						<div className='text type'>{this.extractValue(licenseTerm)}</div>
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

	extractValue(item) {
		if (item === undefined) {
			return '';
		} //TODO fix it later

		return item ? item.choice === optionInputOther.OTHER ? item.other : InputOptions.getTitleByName(optionsInputValues, item.choice) : '';
	}
}

export default LicenseAgreementListEditorView;
