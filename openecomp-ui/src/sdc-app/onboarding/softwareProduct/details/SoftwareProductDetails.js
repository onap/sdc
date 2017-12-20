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

import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import SoftwareProductDetailsView from './SoftwareProductDetailsView.jsx';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {PRODUCT_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';


export const mapStateToProps = ({
	finalizedLicenseModelList,
	softwareProduct,
	licenseModel: {licenseAgreement, featureGroup}
}) => {

	let {softwareProductEditor: {data: currentSoftwareProduct, licensingVersionsList = [], genericFieldInfo}, softwareProductCategories, softwareProductQuestionnaire} = softwareProduct;
	let {licensingData = {}, licensingVersion} = currentSoftwareProduct;
	let licenseAgreementList = [], filteredFeatureGroupsList = [];
	licenseAgreementList = licensingVersion ?
		licenseAgreement.licenseAgreementList : [];
	if(licensingVersion && licensingData && licensingData.licenseAgreement) {
		let selectedLicenseAgreement = licenseAgreementList.find(la => la.id === licensingData.licenseAgreement);
		if (selectedLicenseAgreement) {
			let featureGroupsList = featureGroup.featureGroupsList.filter(({referencingLicenseAgreements}) => referencingLicenseAgreements.includes(selectedLicenseAgreement.id));
			if (featureGroupsList.length) {
				filteredFeatureGroupsList = featureGroupsList.map(featureGroup => ({enum: featureGroup.id, title: featureGroup.name}));
			}
		}
	}
	let {qdata, qgenericFieldInfo : qGenericFieldInfo, dataMap} = softwareProductQuestionnaire;

	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

	return {
		currentSoftwareProduct,
		softwareProductCategories,
		licenseAgreementList,
		licensingVersionsList,
		featureGroupsList: filteredFeatureGroupsList,
		finalizedLicenseModelList,
		qdata,
		isFormValid,
		genericFieldInfo,
		qGenericFieldInfo,
		dataMap
	};

};

export const mapActionsToProps = (dispatch, {version}) => {
	return {
		onDataChanged: (deltaData, formName) => ValidationHelper.dataChanged(dispatch, {deltaData, formName}),
		onVendorParamChanged: (deltaData, formName) => SoftwareProductActionHelper.softwareProductEditorVendorChanged(dispatch, {deltaData, formName}),
		onQDataChanged: (deltaData) => ValidationHelper.qDataChanged(dispatch, {deltaData, qName: PRODUCT_QUESTIONNAIRE}),
		onValidityChanged: isValidityData => SoftwareProductActionHelper.setIsValidityData(dispatch, {isValidityData}),
		onSubmit: (softwareProduct, qdata) => SoftwareProductActionHelper.updateSoftwareProduct(dispatch, {softwareProduct, qdata, version})
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductDetailsView);
