import React from 'react';
import classnames from 'classnames';
import Dropzone from 'react-dropzone';


import i18n from 'nfvo-utils/i18n/i18n.js';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';

import FontAwesome from 'react-fontawesome';
import SoftwareProductLandingPageUploadConfirmationModal from './SoftwareProductLandingPageUploadConfirmationModal.jsx';


const SoftwareProductPropType = React.PropTypes.shape({
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	version: React.PropTypes.string,
	id: React.PropTypes.string,
	categoryId: React.PropTypes.string,
	vendorId: React.PropTypes.string,
	status: React.PropTypes.string,
	licensingData: React.PropTypes.object,
	validationData: React.PropTypes.object
});

const ComponentPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	displayName: React.PropTypes.string,
	description: React.PropTypes.string
});

class SoftwareProductLandingPageView extends React.Component {

	state = {
		localFilter: '',
		fileName: '',
		dragging: false,
		files: []
	};

	static propTypes = {
		currentSoftwareProduct: SoftwareProductPropType,
		isReadOnlyMode: React.PropTypes.bool,
		componentsList: React.PropTypes.arrayOf(ComponentPropType),
		onDetailsSelect: React.PropTypes.func,
		onAttachmentsSelect: React.PropTypes.func,
		onUpload: React.PropTypes.func,
		onUploadConfirmation: React.PropTypes.func,
		onInvalidFileSizeUpload: React.PropTypes.func,
		onComponentSelect: React.PropTypes.func,
		onAddComponent: React.PropTypes.func
	};

	render() {
		let {currentSoftwareProduct, isReadOnlyMode, componentsList = []} =  this.props;
		return (
			<div className='software-product-landing-wrapper'>
				<Dropzone
					className={classnames('software-product-landing-view', {'active-dragging': this.state.dragging})}
					onDrop={files => this.handleImportSubmit(files, isReadOnlyMode)}
					onDragEnter={() => this.handleOnDragEnter(isReadOnlyMode)}
					onDragLeave={() => this.setState({dragging:false})}
					multiple={false}
					disableClick={true}
					ref='fileInput'
					name='fileInput'
					accept='.zip'
					disabled>
					<div className='draggable-wrapper'>
						<div className='software-product-landing-view-top'>
							<div className='row'>
								{this.renderProductSummary(currentSoftwareProduct)}
								{this.renderProductDetails(currentSoftwareProduct, isReadOnlyMode)}
							</div>
						</div>
					</div>
				</Dropzone>
				{
					componentsList.length > 0 && this.renderComponents()
				}
				<SoftwareProductLandingPageUploadConfirmationModal confirmationButtonText={i18n('Continue')}/>
			</div>
		);
	}

	handleOnDragEnter(isReadOnlyMode) {
		if (!isReadOnlyMode) {
			this.setState({dragging: true});
		}
	}

	renderProductSummary(currentSoftwareProduct) {
		let {name = '', description = '', vendorName = '', fullCategoryDisplayName = '', licenseAgreementName = ''}  = currentSoftwareProduct;
		let {onDetailsSelect} = this.props;
		return (
			<div className='details-panel'>
				<div className='software-product-landing-view-heading-title'>{i18n('Software Product Details')}</div>
				<div
					className='software-product-landing-view-top-block clickable'
					onClick={() => onDetailsSelect(currentSoftwareProduct)}>
					<div className='details-container'>
						<div className='single-detail-section title-section'>
							<div>
								<div>{name}</div>
							</div>
						</div>
						<div className='multiple-details-section'>
							<div className='detail-col' >
								<div className='title'>{i18n('Vendor')}</div>
								<div className='description'>{vendorName}</div>
							</div>
							<div className='detail-col'>
								<div className='title'>{i18n('Category')}</div>
								<div className='description'>{fullCategoryDisplayName}</div>
							</div>
							<div className='detail-col'>
								<div className='title extra-large'>{i18n('License Agreement')}</div>
								<div className='description'>
									{this.renderLicenseAgreement(licenseAgreementName)}
								</div>
							</div>
						</div>
						<div className='single-detail-section'>
							<div className='title'>{i18n('Description')}</div>
							<div className='description'>{description}</div>
						</div>
					</div>
				</div>
			</div>
		);
	}

	renderProductDetails(currentSoftwareProduct, isReadOnlyMode) {
		let {validationData} = currentSoftwareProduct;
		let {onAttachmentsSelect} = this.props;
		let details = {
			heatTemplates: validationData ? '1' : '0',
			images: '0',
			otherArtifacts: '0'
		};

		return (
			<div className='details-panel'>
				<div className='software-product-landing-view-heading-title'>{i18n('Software Product Attachments')}</div>
				<div className='software-product-landing-view-top-block'>
					<div
						className='software-product-landing-view-top-block-col'
						onClick={() => onAttachmentsSelect(currentSoftwareProduct)}>
						<div>
							<div className='attachment-details'>{i18n('HEAT Templates')} (<span
								className='attachment-details-count'>{details.heatTemplates}</span>)
							</div>
							<div className='attachment-details'>{i18n('Images')} (<span
								className='attachment-details-count'>{details.images}</span>)
							</div>
							<div className='attachment-details'>{i18n('Other Artifacts')} (<span
								className='attachment-details-count'>{details.otherArtifacts}</span>)
							</div>
						</div>
					</div>
					<div
						className={classnames('software-product-landing-view-top-block-col-upl', {'disabled': isReadOnlyMode})}>
						<div className='drag-text'>{i18n('Drag & drop for upload')}</div>
						<div className='or-text'>{i18n('or')}</div>
						<div className='upload-btn primary-btn' onClick={() => this.refs.fileInput.open()}>
							<span className='primary-btn-text'>{i18n('Select file')}</span>
						</div>
					</div>
				</div>
			</div>
		);
	}

	renderComponents() {
		const {localFilter} = this.state;

		return (
			<ListEditorView
				title={i18n('Virtual Function Components')}
				filterValue={localFilter}
				placeholder={i18n('Filter Components')}
				onFilter={filter => this.setState({localFilter: filter})}>
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

	renderLicenseAgreement(licenseAgreementName) {
		if (!licenseAgreementName) {
			return (<FontAwesome name='exclamation-triangle' className='warning-icon'/>);
		}
		return (licenseAgreementName);
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

	handleImportSubmit(files, isReadOnlyMode) {
		if (isReadOnlyMode) {
			return;
		}
		if (files[0] && files[0].size) {
			this.setState({
				fileName: files[0].name,
				dragging: false,
				complete: '0',
			});
			this.startUploading(files);
		}
		else {
			this.props.onInvalidFileSizeUpload();
		}

	}

	startUploading(files) {
		let {onUpload, currentSoftwareProduct, onUploadConfirmation} = this.props;

		let {validationData} = currentSoftwareProduct;

		if (!(files && files.length)) {
			return;
		}
		let file = files[0];
		let formData = new FormData();
		formData.append('upload', file);
		this.refs.fileInput.value = '';

		if (validationData) {
			onUploadConfirmation(currentSoftwareProduct.id, formData);
		}else {
			onUpload(currentSoftwareProduct.id, formData);
		}

	}
}

export default SoftwareProductLandingPageView;
