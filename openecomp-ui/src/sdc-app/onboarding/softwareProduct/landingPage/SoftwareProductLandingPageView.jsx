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
import classnames from 'classnames';
import Dropzone from 'react-dropzone';


import i18n from 'nfvo-utils/i18n/i18n.js';
import VnfRepositorySearchBox from 'nfvo-components/fileupload/VnfRepositorySearchBox.jsx';

import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import SoftwareProductComponentsList from '../components/SoftwareProductComponentsList.js';

const SoftwareProductPropType = React.PropTypes.shape({
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	version: React.PropTypes.object,
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

		fileName: '',
		dragging: false,
		files: []
	};

	static propTypes = {
		currentSoftwareProduct: SoftwareProductPropType,
		isReadOnlyMode: React.PropTypes.bool,
		componentsList: React.PropTypes.arrayOf(ComponentPropType),
		onDetailsSelect: React.PropTypes.func,
		onUpload: React.PropTypes.func,
		onUploadConfirmation: React.PropTypes.func,
		onInvalidFileSizeUpload: React.PropTypes.func,
		onComponentSelect: React.PropTypes.func,
		onAddComponent: React.PropTypes.func
	};

	render() {
		let {currentSoftwareProduct, isReadOnlyMode, isManual, onDetailsSelect, componentsList} =  this.props;
		return (
			<div className='software-product-landing-wrapper'>
				<Dropzone
					className={classnames('software-product-landing-view', {'active-dragging': this.state.dragging})}
					onDrop={files => this.handleImportSubmit(files, isReadOnlyMode, isManual)}
					onDragEnter={() => this.handleOnDragEnter(isReadOnlyMode, isManual)}
					onDragLeave={() => this.setState({dragging:false})}
					multiple={false}
					disableClick={true}
					ref='fileInput'
					name='fileInput'
					accept='.zip, .csar'
					disabled>
					<div className='draggable-wrapper'>
						<div className='software-product-landing-view-top'>
							<div className='row'>
								<ProductSummary currentSoftwareProduct={currentSoftwareProduct} onDetailsSelect={onDetailsSelect} />
								{this.renderProductDetails(isManual, isReadOnlyMode)}
							</div>
						</div>
					</div>
				</Dropzone>
				<SoftwareProductComponentsList
					isReadOnlyMode={isReadOnlyMode}
					componentsList={componentsList}
					isManual={isManual}
					currentSoftwareProduct={currentSoftwareProduct}/>
			</div>
		);
	}

	handleOnDragEnter(isReadOnlyMode, isManual) {
		if (!isReadOnlyMode && !isManual) {
			this.setState({dragging: true});
		}
	}

	renderProductDetails(isManual, isReadOnlyMode) {
		let {onBrowseVNF, currentSoftwareProduct} = this.props;
		return (
			<div className='details-panel'>
				{ !isManual && <div>
					<div className='software-product-landing-view-heading-title'>{i18n('Software Product Attachments')}</div>
						<VnfRepositorySearchBox
							dataTestId='upload-btn'
							isReadOnlyMode={isReadOnlyMode}
							className={classnames('software-product-landing-view-top-block-col-upl', {'disabled': isReadOnlyMode})}
							onClick={() => this.refs.fileInput.open()} onBrowseVNF={() => onBrowseVNF(currentSoftwareProduct)}/>
					</div>
				}
			</div>
		);
	}

	handleImportSubmit(files, isReadOnlyMode, isManual) {
		if (isReadOnlyMode || isManual) {
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
			this.setState({
				dragging: false
			});
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

const ProductSummary = ({currentSoftwareProduct, onDetailsSelect}) => {
	let {name = '', description = '', vendorName = '', fullCategoryDisplayName = '', licenseAgreementName = ''}  = currentSoftwareProduct;
	return (
		<div className='details-panel'>
			<div className='software-product-landing-view-heading-title'>{i18n('Software Product Details')}</div>
			<div
				className='software-product-landing-view-top-block clickable'
				onClick={() => onDetailsSelect(currentSoftwareProduct)}>
				<div className='details-container'>
					<div className='single-detail-section title-section'>
						<div className='single-detail-section title-text'>
							{name}
						</div>
					</div>
					<div className='details-section'>
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
									<LicenseAgreement licenseAgreementName={licenseAgreementName}/>
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
		</div>
	);
};


const LicenseAgreement = ({licenseAgreementName}) => {
	if (!licenseAgreementName) {
		return (<div className='missing-license'><SVGIcon color='warning' name='exclamationTriangleFull'/><div className='warning-text'>{i18n('Missing')}</div></div>);
	}
	return <div>{licenseAgreementName}</div>;
};

export default SoftwareProductLandingPageView;
