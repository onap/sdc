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
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import Configuration from 'sdc-app/config/Configuration.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {IMAGE_QUESTIONNAIRE} from './SoftwareProductComponentsImageConstants.js';
import {actionTypes} from './SoftwareProductComponentsImageConstants.js';

function baseUrl(softwareProductId, version, componentId) {
	const versionId = version.id;
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/components/${componentId}/images`;
}

function fetchImagesList({softwareProductId, componentId, version}) {
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version, componentId)}`);
}

function fetchImage({softwareProductId, componentId, imageId, version}) {
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version, componentId)}/${imageId}`);
}

function destroyImage({softwareProductId, componentId, version, imageId}) {
	return RestAPIUtil.destroy(`${baseUrl(softwareProductId, version, componentId)}/${imageId}`);
}

function createImage({softwareProductId, componentId, version, data}) {
	return RestAPIUtil.post(baseUrl(softwareProductId, version, componentId), {
		fileName: data.fileName
	});
}

function fetchImageQuestionnaire({softwareProductId, componentId, imageId, version}) {
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version, componentId)}/${imageId}/questionnaire`);
}

function saveImage({softwareProductId, version, componentId, image: {id, fileName}}) {
	return RestAPIUtil.put(`${baseUrl(softwareProductId, version, componentId)}/${id}`,{
		fileName
	});

}

function saveImageQuestionnaire({softwareProductId, componentId, version, imageId, qdata}) {
	return RestAPIUtil.put(`${baseUrl(softwareProductId, version, componentId)}/${imageId}/questionnaire`, qdata);
}

const SoftwareProductComponentImagesActionHelper = {
	fetchImagesList(dispatch, {softwareProductId, componentId, version}) {
		dispatch({
			type: actionTypes.IMAGES_LIST_UPDATE,
			response: []
		});

		return fetchImagesList({softwareProductId, componentId, version}).then((response) => {
			dispatch({
				type: actionTypes.IMAGES_LIST_UPDATE,
				response: response.results,
				componentId : componentId
			});
		});
	},

	deleteImage(dispatch, {softwareProductId, componentId, version, imageId}) {
		return destroyImage({softwareProductId, componentId, version, imageId}).then(() => {
			return SoftwareProductComponentImagesActionHelper.fetchImagesList(dispatch, {softwareProductId, componentId, version});
		});
	},

	loadImageData({softwareProductId, componentId, imageId, version}) {
		return fetchImage({softwareProductId, componentId, imageId, version});
	},

	openEditImageEditor(dispatch, {image, softwareProductId, componentId, version, isReadOnlyMode}) {
		return SoftwareProductComponentImagesActionHelper.loadImageData({softwareProductId, componentId, imageId: image.id, version}).then(({data}) => {
			SoftwareProductComponentImagesActionHelper.loadImageQuestionnaire(dispatch, {
				softwareProductId,
				componentId,
				imageId: image.id,
				version
			}).then(() => {
				SoftwareProductComponentImagesActionHelper.openImageEditor(dispatch, {
					softwareProductId,
					componentId,
					version,
					isReadOnlyMode,
					image,
					data
				});
			});
		});
	},

	openImageEditor(dispatch, {image = {}, data = {}, softwareProductId, componentId, version, isReadOnlyMode}) {

		let {id} = image;
		let title = id ?  i18n('Edit Image') : i18n('Create New Image');
		let className = id ? 'image-modal-edit' : 'image-modal-new';

		dispatch({
			type: actionTypes.ImageEditor.OPEN,
			image: {...data, id}
		});

		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_SHOW,
			data: {
				modalComponentName: modalContentMapper.SOFTWARE_PRODUCT_COMPONENT_IMAGE_EDITOR,
				title: title,
				modalClassName: className,
				modalComponentProps: {softwareProductId, componentId, version, isReadOnlyMode}
			}
		});

	},

	closeImageEditor(dispatch) {

		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_CLOSE
		});

		dispatch({
			type: actionTypes.ImageEditor.CLOSE
		});

	},

	loadImageQuestionnaire(dispatch, {softwareProductId, componentId, imageId, version}) {
		return fetchImageQuestionnaire({softwareProductId, componentId, imageId, version}).then((response) => {
			ValidationHelper.qDataLoaded(dispatch, {qName: IMAGE_QUESTIONNAIRE ,response: {
				qdata: response.data ? JSON.parse(response.data) : {},
				qschema: JSON.parse(response.schema)
			}});
		});
	},

	saveImageDataAndQuestionnaire(dispatch, {softwareProductId, componentId, version, data, qdata}) {
		SoftwareProductComponentImagesActionHelper.closeImageEditor(dispatch);
		if (data !== null && data.id) {
			// editor in edit mode
			return Promise.all([
				saveImageQuestionnaire({softwareProductId, version, componentId, imageId: data.id, qdata}),
				saveImage({softwareProductId, version, componentId, image: data}).then(() => {
					return SoftwareProductComponentImagesActionHelper.fetchImagesList(dispatch, {softwareProductId, componentId, version});
				})
			]);
		} else {
			// editor in create mode
			createImage({softwareProductId, componentId, version, data}).then(() => {
				return SoftwareProductComponentImagesActionHelper.fetchImagesList(dispatch, {softwareProductId, componentId, version});
			});
		}
	}
};

export default SoftwareProductComponentImagesActionHelper;
