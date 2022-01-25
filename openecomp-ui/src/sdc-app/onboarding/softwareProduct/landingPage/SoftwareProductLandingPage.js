/*!
 * Copyright © 2016-2018 European Support Limited
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
import { connect } from 'react-redux';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import LandingPageView from './SoftwareProductLandingPageView.jsx';
import { actionTypes as modalActionTypes } from 'nfvo-components/modal/GlobalModalConstants.js';
import { onboardingMethod } from '../SoftwareProductConstants.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import { enums, screenTypes } from 'sdc-app/onboarding/OnboardingConstants.js';
import VNFImportActionHelper from '../vnfMarketPlace/VNFImportActionHelper.js';

export const mapStateToProps = ({
    features,
    softwareProduct,
    licenseModel: { licenseAgreement },
    currentScreen: { itemPermission: { isCertified } }
}) => {
    let {
        softwareProductEditor: { data: currentSoftwareProduct = {} },
        softwareProductComponents,
        softwareProductCategories = []
    } = softwareProduct;
    let { licensingData = {} } = currentSoftwareProduct;
    let { licenseAgreementList } = licenseAgreement;
    let { componentsList } = softwareProductComponents;
    let licenseAgreementName = licenseAgreementList.find(
        la => la.id === licensingData.licenseAgreement
    );
    if (licenseAgreementName) {
        licenseAgreementName = licenseAgreementName.name;
    } else if (licenseAgreementList.length === 0) {
        // otherwise the state of traingle svgicon will be updated post unmounting
        licenseAgreementName = null;
    }

    let categoryName = '',
        subCategoryName = '',
        fullCategoryDisplayName = '';
    const category = softwareProductCategories.find(
        ca => ca.uniqueId === currentSoftwareProduct.category
    );
    if (category) {
        categoryName = category.name;
        const subcategories = category.subcategories || [];
        const subcat = subcategories.find(
            sc => sc.uniqueId === currentSoftwareProduct.subCategory
        );
        subCategoryName = subcat && subcat.name ? subcat.name : '';
    }
    fullCategoryDisplayName = `${subCategoryName} (${categoryName})`;

    return {
        features,
        currentSoftwareProduct: {
            ...currentSoftwareProduct,
            licenseAgreementName,
            fullCategoryDisplayName
        },
        isCertified,
        componentsList,
        isManual:
            currentSoftwareProduct.onboardingMethod === onboardingMethod.MANUAL
    };
};

function handleScreenChange(softwareProduct, dispatch, version) {
    const softwareProductId = softwareProduct.id;
    if (softwareProduct.licenseType === 'INTERNAL') {
        ScreensHelper.loadScreen(dispatch, {
            screen: enums.SCREEN.SOFTWARE_PRODUCT_DETAILS,
            screenType: screenTypes.SOFTWARE_PRODUCT,
            props: { softwareProductId, version }
        });
    } else {
        ScreensHelper.loadScreen(dispatch, {
            screen: enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE,
            screenType: screenTypes.SOFTWARE_PRODUCT,
            props: { softwareProductId, version }
        });
    }
}

const mapActionsToProps = (dispatch, { version }) => {
    return {
        onLicenseChange: softwareProduct => {
            SoftwareProductActionHelper.updateSoftwareProductData(dispatch, {
                softwareProduct,
                version
            }).then(() => {});
            handleScreenChange(softwareProduct, dispatch, version);
        },
        onCandidateInProcess: softwareProductId =>
            ScreensHelper.loadScreen(dispatch, {
                screen: enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS_SETUP,
                screenType: screenTypes.SOFTWARE_PRODUCT,
                props: { softwareProductId, version }
            }),
        onUpload: (
            softwareProductId,
            formData,
            onUploadStart = () => {
                // do nothing by default
            },
            onUploadProgress = undefined,
            onUploadFinished = () => {
                // do nothing by default
            }
        ) => {
            SoftwareProductActionHelper.uploadFile(dispatch, {
                softwareProductId,
                formData,
                failedNotificationTitle: i18n('Upload validation failed'),
                version,
                onUploadProgress
            }).finally(() => {
                onUploadFinished();
            });
            onUploadStart();
        },

        onUploadConfirmation: (
            softwareProductId,
            formData,
            onUploadStart = () => {
                // do nothing by default
            },
            onUploadProgress = undefined,
            onUploadFinished = () => {
                // do nothing by default
            }
        ) =>
            dispatch({
                type: modalActionTypes.GLOBAL_MODAL_WARNING,
                data: {
                    msg: i18n(
                        'Upload will erase existing data. Do you want to continue?'
                    ),
                    confirmationButtonText: i18n('Continue'),
                    title: i18n('Warning'),
                    onConfirmed: () => {
                        SoftwareProductActionHelper.uploadFile(dispatch, {
                            softwareProductId,
                            formData,
                            failedNotificationTitle: i18n(
                                'Upload validation failed'
                            ),
                            version,
                            onUploadProgress
                        }).finally(value => {
                            console.log('upload finished', value);
                            onUploadFinished();
                        });
                        onUploadStart();
                    },
                    onDeclined: () =>
                        dispatch({
                            type: modalActionTypes.GLOBAL_MODAL_CLOSE
                        })
                }
            }),

        onInvalidFileSizeUpload: () =>
            dispatch({
                type: modalActionTypes.GLOBAL_MODAL_ERROR,
                data: {
                    title: i18n('Upload Failed'),
                    confirmationButtonText: i18n('Continue'),
                    msg: i18n(
                        "no zip or csar file was uploaded or expected file doesn't exist"
                    )
                }
            }),

        fetchUploadStatus: softwareProductId => {
            return SoftwareProductActionHelper.fetchUploadStatus(
                softwareProductId,
                version.id
            );
        },

        onComponentSelect: ({ id: softwareProductId, componentId }) =>
            ScreensHelper.loadScreen(dispatch, {
                screen: screenTypes.SOFTWARE_PRODUCT_COMPONENT_DEFAULT_GENERAL,
                screenType: screenTypes.SOFTWARE_PRODUCT,
                props: { softwareProductId, version, componentId }
            }),
        /** for the next version */
        onAddComponent: () =>
            SoftwareProductActionHelper.addComponent(dispatch),

        onBrowseVNF: currentSoftwareProduct => {
            VNFImportActionHelper.open(dispatch, currentSoftwareProduct);
        }
    };
};

export default connect(mapStateToProps, mapActionsToProps, null, {
    withRef: true
})(LandingPageView);
