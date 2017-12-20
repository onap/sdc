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

import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import SoftwareProductComponentsImageListView from './SoftwareProductComponentsImageListView.jsx';
import ImageHelper from './SoftwareProductComponentsImageActionHelper.js';
import SoftwareProductComponentsImagesActionHelper from './SoftwareProductComponentsImageActionHelper.js';
import SoftwareProductComponentsActionHelper from '../SoftwareProductComponentsActionHelper.js';
import {COMPONENTS_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';

import {onboardingMethod as onboardingMethodTypes} from '../../SoftwareProductConstants.js';

export const mapStateToProps = ({softwareProduct}) => {

	let {softwareProductEditor: {data: currentSoftwareProduct = {}, isValidityData = true}, softwareProductComponents} = softwareProduct;
	let {images: {imagesList = []}, componentEditor: {data: componentData, qdata, dataMap, qgenericFieldInfo}} = softwareProductComponents;
	let {onboardingMethod} = currentSoftwareProduct;
	let isManual =  onboardingMethod === onboardingMethodTypes.MANUAL;

	return {
		componentData,
		qdata,
		dataMap,
		qgenericFieldInfo,
		isValidityData,
		imagesList,
		isManual : isManual
	};
};

const mapActionsToProps = (dispatch, {softwareProductId, componentId, version}) => {
	return {
		onQDataChanged: (deltaData) => ValidationHelper.qDataChanged(dispatch, {deltaData,
			qName: COMPONENTS_QUESTIONNAIRE}),
		onAddImage: (isReadOnlyMode) => {
			SoftwareProductComponentsImagesActionHelper.openImageEditor(dispatch,
				{isReadOnlyMode, softwareProductId,
					componentId, version}
			);},
		onDeleteImage: (image) => {
			let shortenedFileName = (image.fileName.length > 40) ? image.fileName.substr(0,40) + '...' : image.fileName;
			dispatch({
				type: modalActionTypes.GLOBAL_MODAL_WARNING,
				data: {
					msg: i18n('Are you sure you want to delete "{shortenedFileName}"?', {shortenedFileName: shortenedFileName}),
					onConfirmed: () => ImageHelper.deleteImage(dispatch, {
						softwareProductId,
						componentId,
						version,
						imageId: image.id
					})
				}
			});
		},
		onEditImageClick: (image, isReadOnlyMode) => {
			SoftwareProductComponentsImagesActionHelper.openEditImageEditor(dispatch, {
				image, isReadOnlyMode, softwareProductId, componentId, version, modalClassName: 'image-modal-edit'}
			);
		},
		onSubmit: (qdata) => { return SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(dispatch,
			{softwareProductId,
				vspComponentId: componentId,
				version,
				qdata});
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductComponentsImageListView);
