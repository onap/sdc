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
import {connect} from 'react-redux';
import i18n from 'nfvo-utils/i18n/i18n.js';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import LandingPageView from './SoftwareProductLandingPageView.jsx';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {onboardingMethod} from '../SoftwareProductConstants.js';
import VNFImportActionHelper from '../vnfMarketPlace/VNFImportActionHelper.js';

export const mapStateToProps = ({softwareProduct, licenseModel: {licenseAgreement}}) => {
	let {softwareProductEditor: {data:currentSoftwareProduct = {}}, softwareProductComponents, softwareProductCategories = []} = softwareProduct;
	let {licensingData = {}} = currentSoftwareProduct;
	let {licenseAgreementList} = licenseAgreement;
	let {componentsList} = softwareProductComponents;
	let licenseAgreementName = licenseAgreementList.find(la => la.id === licensingData.licenseAgreement);
	if (licenseAgreementName) {
		licenseAgreementName = licenseAgreementName.name;
	} else if (licenseAgreementList.length === 0) { // otherwise the state of traingle svgicon will be updated post unmounting
		licenseAgreementName = null;
	}

	let categoryName = '', subCategoryName = '', fullCategoryDisplayName = '';
	const category = softwareProductCategories.find(ca => ca.uniqueId === currentSoftwareProduct.category);
	if (category) {
		categoryName = category.name;
		const subcategories = category.subcategories || [];
		const subcat = subcategories.find(sc => sc.uniqueId === currentSoftwareProduct.subCategory);
		subCategoryName = subcat && subcat.name ? subcat.name : '';
	}
	fullCategoryDisplayName = `${subCategoryName} (${categoryName})`;

	const isReadOnlyMode = VersionControllerUtils.isReadOnly(currentSoftwareProduct);

	return {
		currentSoftwareProduct: {
			...currentSoftwareProduct,
			licenseAgreementName,
			fullCategoryDisplayName
		},
		isReadOnlyMode,
		componentsList,
		isManual: currentSoftwareProduct.onboardingMethod === onboardingMethod.MANUAL
	};
};

const mapActionsToProps = (dispatch, {version}) => {
	return {
		onDetailsSelect: ({id: softwareProductId, vendorId: licenseModelId, version}) => OnboardingActionHelper.navigateToSoftwareProductDetails(dispatch, {
			softwareProductId,
			licenseModelId,
			version
		}),
		onUpload: (softwareProductId, formData) =>
			SoftwareProductActionHelper.uploadFile(dispatch, {
				softwareProductId,
				formData,
				failedNotificationTitle: i18n('Upload validation failed'),
				version
			}),

		onUploadConfirmation: (softwareProductId, formData) =>
			dispatch({
				type: modalActionTypes.GLOBAL_MODAL_WARNING,
				data:{
					msg: i18n('Upload will erase existing data. Do you want to continue?'),
					confirmationButtonText: i18n('Continue'),
					title: i18n('Warning'),
					onConfirmed: ()=>SoftwareProductActionHelper.uploadFile(dispatch, {
						softwareProductId,
						formData,
						failedNotificationTitle: i18n('Upload validation failed'),
						version
					}),
					onDeclined: () => dispatch({
						type: modalActionTypes.GLOBAL_MODAL_CLOSE
					})
				}
			}),

		onInvalidFileSizeUpload: () => dispatch({
			type: modalActionTypes.GLOBAL_MODAL_ERROR,
			data: {
				title: i18n('Upload Failed'),
				confirmationButtonText: i18n('Continue'),
				msg: i18n('no zip or csar file was uploaded or expected file doesn\'t exist')
			}
		}),
		onComponentSelect: ({id: softwareProductId, componentId}) => {
			OnboardingActionHelper.navigateToSoftwareProductComponentGeneralAndUpdateLeftPanel(dispatch, {softwareProductId, componentId, version });
		},
		/** for the next version */
		onAddComponent: () => SoftwareProductActionHelper.addComponent(dispatch),

		onBrowseVNF: (currentSoftwareProduct) => {
			VNFImportActionHelper.open(dispatch, currentSoftwareProduct);
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(LandingPageView);
