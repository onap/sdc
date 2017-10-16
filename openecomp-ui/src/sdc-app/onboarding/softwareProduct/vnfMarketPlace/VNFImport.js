/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
import {connect} from 'react-redux';
import VNFImportView from './VNFImportView.jsx';
import VNFImportActionHelper  from './VNFImportActionHelper.js';

export const mapStateToProps = (response) => {
	const { softwareProduct: { VNFMarketPlaceImport : { vnfItems } } } = response;
	return {
		vnfItems: vnfItems
	};
};

export const mapActionsToProps = (dispatch) => {
	return {
		onCancel: () => VNFImportActionHelper.resetData(dispatch),
		onSubmit: (csarId, selectedVendor) => {
			VNFImportActionHelper.uploadData(selectedVendor, csarId, dispatch);
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(VNFImportView);
