/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

import {connect} from 'react-redux';

import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import SoftwareProductDetailsView from './SoftwareProductDetailsView.jsx';

export const mapStateToProps = ({finalizedLicenseModelList, softwareProduct, licenseModel: {licenseAgreement, featureGroup}}) => {
	let {softwareProductEditor: {data: currentSoftwareProduct}, softwareProductCategories, softwareProductQuestionnaire} = softwareProduct;
	let {licensingData = {}, licensingVersion} = currentSoftwareProduct;
	let licenseAgreementList = [], filteredFeatureGroupsList = [];
	if(licensingVersion && licensingVersion !== '') {
		licenseAgreementList = licenseAgreement.licenseAgreementList;
		let selectedLicenseAgreement = licenseAgreementList.find(la => la.id === licensingData.licenseAgreement);
		if (selectedLicenseAgreement) {
			let featureGroupsList = featureGroup.featureGroupsList.filter(({referencingLicenseAgreements}) => referencingLicenseAgreements.includes(selectedLicenseAgreement.id));
			if (featureGroupsList.length) {
				filteredFeatureGroupsList = featureGroupsList.map(featureGroup => ({enum: featureGroup.id, title: featureGroup.name}));
			}
		}
	}
	let {qdata, qschema} = softwareProductQuestionnaire;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(currentSoftwareProduct);

	return {
		currentSoftwareProduct,
		softwareProductCategories,
		licenseAgreementList,
		featureGroupsList: filteredFeatureGroupsList,
		finalizedLicenseModelList,
		qdata,
		qschema,
		isReadOnlyMode
	};
};

export const mapActionsToProps = (dispatch) => {
	return {
		onDataChanged: deltaData => SoftwareProductActionHelper.softwareProductEditorDataChanged(dispatch, {deltaData}),
		onVendorParamChanged: deltaData => SoftwareProductActionHelper.softwareProductEditorVendorChanged(dispatch, {deltaData}),
		onQDataChanged: ({data}) => SoftwareProductActionHelper.softwareProductQuestionnaireUpdate(dispatch, {data}),
		onValidityChanged: isValidityData => SoftwareProductActionHelper.setIsValidityData(dispatch, {isValidityData}),
		onSubmit: (softwareProduct, qdata) =>{ return SoftwareProductActionHelper.updateSoftwareProduct(dispatch, {softwareProduct, qdata});}
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductDetailsView);
