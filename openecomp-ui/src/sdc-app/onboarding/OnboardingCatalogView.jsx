import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';
import objectValues from 'lodash/values.js';
import LicenseModelCreation from './licenseModel/creation/LicenseModelCreation.js';
import SoftwareProductCreation from './softwareProduct/creation/SoftwareProductCreation.js';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import classnames from 'classnames';
import ExpandableInput from 'nfvo-components/input/ExpandableInput.jsx';

export const catalogItemTypes = Object.freeze({
	LICENSE_MODEL: 'license-model',
	SOFTWARE_PRODUCT: 'software-product'
});

const catalogItemTypeClasses = {
	LICENSE_MODEL: 'license-model-type',
	SOFTWARE_PRODUCT: 'software-product-type'
};

class OnboardingCatalogView extends React.Component {

	constructor(props) {
		super(props);
		this.state = {searchValue: ''};
		this.handleSearch = this.handleSearch.bind(this);
	}

	handleSearch(event){
		this.setState({searchValue: event.target.value});
	}

	static propTypes = {
		licenseModelList: React.PropTypes.array,
		softwareProductList: React.PropTypes.array,
		modalToShow: React.PropTypes.oneOf(objectValues(catalogItemTypes)),
		onSelectLicenseModel: React.PropTypes.func.isRequired,
		onSelectSoftwareProduct: React.PropTypes.func.isRequired,
		onAddLicenseModelClick: React.PropTypes.func.isRequired,
		onAddSoftwareProductClick: React.PropTypes.func.isRequired
	};

	getModalDetails() {
		const {modalToShow} = this.props;
		switch (modalToShow) {
			case catalogItemTypes.LICENSE_MODEL:
				return {
					title: i18n('New License Model'),
					element: <LicenseModelCreation/>
				};
			case catalogItemTypes.SOFTWARE_PRODUCT:
				return {
					title: i18n('New Software Product'),
					element: <SoftwareProductCreation/>
				};
		}
	}

	render() {
		const modalDetails = this.getModalDetails();
		const {licenseModelList, softwareProductList, onSelectLicenseModel, onSelectSoftwareProduct, onAddLicenseModelClick, onAddSoftwareProductClick, modalToShow} = this.props;

		return (
			<div className='catalog-view'>
				<div className='catalog-header'>
					<div className='catalog-header-title'>{i18n('Onboarding Catalog')}</div>
					<ExpandableInput
						onChange={this.handleSearch}
						iconType='search'/>
				</div>
				<div className='catalog-list'>

					<div className='create-catalog-item tile'>
						<div className='plus-section'>
							<div className='plus-icon-button'/>
							<span>{i18n('ADD')}</span>
						</div>
						<div className='primary-btn new-license-model'>
							<span
								className='primary-btn-text'
								onClick={() => onAddLicenseModelClick()}>{i18n('New License Model')}</span></div>
						<div className='primary-btn'>
							<span
								className='primary-btn-text'
								onClick={() => onAddSoftwareProductClick()}>{i18n('New Vendor Software Product')}</span>
						</div>
					</div>
					{licenseModelList.filter(vlm => vlm.vendorName.toLowerCase().indexOf(this.state.searchValue.toLowerCase()) > -1).map(licenseModel => this.renderTile(
						{
							...licenseModel,
							name: licenseModel.vendorName
						},
						catalogItemTypeClasses.LICENSE_MODEL,
						() => onSelectLicenseModel(licenseModel))
					)}
					{softwareProductList.filter(vsp => vsp.name.toLowerCase().indexOf(this.state.searchValue.toLowerCase()) > -1).map(softwareProduct => this.renderTile(softwareProduct,
						catalogItemTypeClasses.SOFTWARE_PRODUCT,
						() => onSelectSoftwareProduct(softwareProduct))
					)}
				</div>
				<Modal
					show={Boolean(modalDetails)}
					className={`${this.getCatalogItemTypeClassByItemType(modalToShow)}-modal`}>
					<Modal.Header>
						<Modal.Title>{modalDetails && modalDetails.title}</Modal.Title>
					</Modal.Header>
					<Modal.Body>
						{
							modalDetails && modalDetails.element
						}
					</Modal.Body>
				</Modal>
			</div>
		);

	}

	getCatalogItemTypeClassByItemType(catalogItemType) {
		switch (catalogItemType) {
			case catalogItemTypes.LICENSE_MODEL:
				return catalogItemTypeClasses.LICENSE_MODEL;
			case catalogItemTypes.SOFTWARE_PRODUCT:
				return catalogItemTypeClasses.SOFTWARE_PRODUCT;
		}
	}

	renderTile(catalogItemData, catalogItemTypeClass, onSelect) {
		let {status: itemStatus} = VersionControllerUtils.getCheckOutStatusKindByUserID(catalogItemData.status, catalogItemData.lockingUser);
		return (
			<div className='catalog-tile tile' key={catalogItemData.id} onClick={() => onSelect()}>
				<div className={`catalog-tile-type ${catalogItemTypeClass}`}/>
				<div className='catalog-tile-icon'>
					<div className={`icon ${catalogItemTypeClass}-icon`}></div>
				</div>
				<div className='catalog-tile-content'>
					<div className='catalog-tile-item-details'>
						<div className='catalog-tile-item-name'>{catalogItemData.name}</div>
						<div className='catalog-tile-item-version'>V {catalogItemData.version}</div>
					</div>
					<div className={classnames('catalog-tile-check-in-status', {'sprite-new checkout-editable-status-icon': itemStatus === 'Locked'})}>
					</div>
				</div>
			</div>
		);
	}
}
export default OnboardingCatalogView;
