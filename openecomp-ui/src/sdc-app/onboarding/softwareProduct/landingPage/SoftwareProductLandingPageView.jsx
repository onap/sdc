/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import Dropzone from 'react-dropzone';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Configuration from 'sdc-app/config/Configuration.js';
import DraggableUploadFileBox from 'nfvo-components/fileupload/DraggableUploadFileBox.jsx';
import VnfRepositorySearchBox from 'nfvo-components/vnfMarketPlace/VnfRepositorySearchBox.jsx';

import SoftwareProductComponentsList from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponents.js';

const SoftwareProductPropType = PropTypes.shape({
    name: PropTypes.string,
    description: PropTypes.string,
    version: PropTypes.string,
    id: PropTypes.string,
    categoryId: PropTypes.string,
    vendorId: PropTypes.string,
    licenseType: PropTypes.string,
    status: PropTypes.string,
    licensingData: PropTypes.object,
    validationData: PropTypes.object
});

const ComponentPropType = PropTypes.shape({
    id: PropTypes.string,
    name: PropTypes.string,
    displayName: PropTypes.string,
    description: PropTypes.string
});

class SoftwareProductLandingPageView extends React.Component {
    state = {
        fileName: '',
        dragging: false,
        files: []
    };

    static propTypes = {
        currentSoftwareProduct: SoftwareProductPropType,
        isReadOnlyMode: PropTypes.bool,
        componentsList: PropTypes.arrayOf(ComponentPropType),
        version: PropTypes.object,
        onLicenseChange: PropTypes.func,
        onUpload: PropTypes.func,
        onUploadConfirmation: PropTypes.func,
        onInvalidFileSizeUpload: PropTypes.func,
        onComponentSelect: PropTypes.func,
        onAddComponent: PropTypes.func
    };
    componentDidMount() {
        const {
            onCandidateInProcess,
            currentSoftwareProduct,
            isCertified
        } = this.props;
        if (currentSoftwareProduct.candidateOnboardingOrigin && !isCertified) {
            onCandidateInProcess(currentSoftwareProduct.id);
        }
    }

    licenceChange = (e, currentSoftwareProduct, onLicenseChange) => {
        currentSoftwareProduct.licenseType = e.target.value;
        onLicenseChange(currentSoftwareProduct);
    };

    render() {
        let {
            currentSoftwareProduct,
            isReadOnlyMode,
            isManual,
            onLicenseChange
        } = this.props;
        let licenceChange = this.licenceChange;
        return (
            <div className="software-product-landing-wrapper">
                <Dropzone
                    className={classnames('software-product-landing-view', {
                        'active-dragging': this.state.dragging
                    })}
                    onDrop={files =>
                        this.handleImportSubmit(files, isReadOnlyMode, isManual)
                    }
                    onDragEnter={() =>
                        this.handleOnDragEnter(isReadOnlyMode, isManual)
                    }
                    onDragLeave={() => this.setState({ dragging: false })}
                    multiple={false}
                    disableClick={true}
                    ref="fileInput"
                    name="fileInput"
                    accept=".zip, .csar">
                    <div className="draggable-wrapper">
                        <div className="software-product-landing-view-top">
                            <div className="row">
                                <ProductSummary
                                    currentSoftwareProduct={
                                        currentSoftwareProduct
                                    }
                                    licenceChange={licenceChange}
                                    onLicenseChange={onLicenseChange}
                                />
                                {this.renderProductDetails(
                                    isManual,
                                    isReadOnlyMode
                                )}
                            </div>
                        </div>
                    </div>
                </Dropzone>
                <SoftwareProductComponentsList />
            </div>
        );
    }

    handleOnDragEnter(isReadOnlyMode, isManual) {
        if (!isReadOnlyMode && !isManual) {
            this.setState({ dragging: true });
        }
    }

    renderProductDetails(isManual, isReadOnlyMode) {
        let { onBrowseVNF, currentSoftwareProduct } = this.props;

        if (Configuration.get('showBrowseVNF')) {
            return (
                <div className="details-panel">
                    {!isManual && (
                        <div>
                            <div className="software-product-landing-view-heading-title">
                                {i18n('Software Product Attachments')}
                            </div>
                            <VnfRepositorySearchBox
                                dataTestId="upload-btn"
                                isReadOnlyMode={isReadOnlyMode}
                                className={classnames(
                                    'software-product-landing-view-top-block-col-upl showVnf',
                                    { disabled: isReadOnlyMode }
                                )}
                                onClick={() => this.refs.fileInput.open()}
                                onBrowseVNF={() =>
                                    onBrowseVNF(currentSoftwareProduct)
                                }
                            />
                        </div>
                    )}
                </div>
            );
        } else {
            return (
                <div className="details-panel">
                    {!isManual && (
                        <div>
                            <div className="software-product-landing-view-heading-title">
                                {i18n('Software Product Attachments')}
                            </div>
                            <DraggableUploadFileBox
                                dataTestId="upload-btn"
                                isReadOnlyMode={isReadOnlyMode}
                                className={classnames(
                                    'software-product-landing-view-top-block-col-upl',
                                    { disabled: isReadOnlyMode }
                                )}
                                onClick={() => this.refs.fileInput.open()}
                                onBrowseVNF={() => onBrowseVNF()}
                            />
                        </div>
                    )}
                </div>
            );
        }
    }

    handleImportSubmit(files, isReadOnlyMode, isManual) {
        if (isReadOnlyMode || isManual) {
            return;
        }
        if (files[0] && files[0].size) {
            this.setState({
                fileName: files[0].name,
                dragging: false,
                complete: '0'
            });
            this.startUploading(files);
        } else {
            this.setState({
                dragging: false
            });
            this.props.onInvalidFileSizeUpload();
        }
    }

    startUploading(files) {
        let {
            onUpload,
            currentSoftwareProduct,
            onUploadConfirmation
        } = this.props;

        let { validationData } = currentSoftwareProduct;

        if (!(files && files.length)) {
            return;
        }
        let file = files[0];
        let formData = new FormData();
        formData.append('upload', file);
        this.refs.fileInput.value = '';

        if (validationData) {
            onUploadConfirmation(currentSoftwareProduct.id, formData);
        } else {
            onUpload(currentSoftwareProduct.id, formData);
        }
    }
}

const ProductSummary = ({
    currentSoftwareProduct,
    licenceChange,
    onLicenseChange
}) => {
    let {
        name = '',
        description = '',
        vendorName = '',
        fullCategoryDisplayName = ''
    } = currentSoftwareProduct;
    return (
        <div className="details-panel">
            <div className="software-product-landing-view-heading-title">
                {i18n('Software Product Details')}
            </div>
            <div className="software-product-landing-view-top-block">
                <div className="details-container">
                    <div className="single-detail-section title-section">
                        <div className="single-detail-section title-text">
                            {name}
                        </div>
                    </div>
                    <div className="details-section">
                        <div className="multiple-details-section">
                            <div className="detail-col">
                                <div className="title">{i18n('Vendor')}</div>
                                <div className="description">{vendorName}</div>
                            </div>
                            <div className="detail-col">
                                <div className="title">{i18n('Category')}</div>
                                <div className="description">
                                    {fullCategoryDisplayName}
                                </div>
                            </div>
                            <div className="detail-col">
                                <div className="title extra-large">
                                    {i18n('License Agreement')}
                                </div>
                                <div className="description">
                                    <LicenseAgreement
                                        licenceChange={licenceChange}
                                        currentSoftwareProduct={
                                            currentSoftwareProduct
                                        }
                                        onLicenseChange={onLicenseChange}
                                    />
                                </div>
                            </div>
                        </div>
                        <div className="single-detail-section">
                            <div className="title">{i18n('Description')}</div>
                            <div className="description">{description}</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

const LicenseAgreement = ({
    licenceChange,
    currentSoftwareProduct,
    onLicenseChange
}) => {
    return (
        <div className="missing-license">
            <form>
                <input
                    type="radio"
                    value="INTERNAL"
                    id="INTERNAL"
                    onChange={event =>
                        licenceChange(
                            event,
                            currentSoftwareProduct,
                            onLicenseChange
                        )
                    }
                    checked={currentSoftwareProduct.licenseType === 'INTERNAL'}
                    name="license"
                />
                <div className="description licenceLabel">
                    {i18n('Internal license')}
                </div>
                <br />
                <input
                    type="radio"
                    value="EXTERNAL"
                    id="EXTERNAL"
                    onChange={event =>
                        licenceChange(
                            event,
                            currentSoftwareProduct,
                            onLicenseChange
                        )
                    }
                    checked={currentSoftwareProduct.licenseType === 'EXTERNAL'}
                    name="license"
                />
                <div className="description licenceLabel">
                    {i18n('External license')}
                </div>
            </form>
        </div>
    );
};

export default SoftwareProductLandingPageView;
