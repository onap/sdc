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
import DetailsCatalogView from '../DetailsCatalogView.jsx';
import {statusEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {tabsMapping} from 'sdc-app/onboarding/onboard/OnboardConstants.js';

const WorkspaceView = (props) => {
	let {
		licenseModelList, softwareProductList, onAddLicenseModelClick,
		onAddSoftwareProductClick, onSelectLicenseModel, onSelectSoftwareProduct, searchValue, onMigrate
	} = props;

	let {getCheckOutStatusKindByUserID} = VersionControllerUtils;
	let unfinalizedLicenseModelList = licenseModelList.filter(vlm => {
		let {status} = getCheckOutStatusKindByUserID(vlm.status, vlm.lockingUser);
		return status !== statusEnum.SUBMIT_STATUS && status !== statusEnum.LOCK_STATUS;
	});
	let unfinalizedSoftwareProductList = softwareProductList.filter(vsp =>{
		let {status} = getCheckOutStatusKindByUserID(vsp.status, vsp.lockingUser);
		return status !== statusEnum.SUBMIT_STATUS && status !== statusEnum.LOCK_STATUS;
	});

	return (
		<div className='catalog-wrapper workspace-view'>
			<div className='catalog-header workspace-header'>
				{i18n('WORKSPACE')}
			</div>
			<DetailsCatalogView
				VLMList={unfinalizedLicenseModelList}
				VSPList={unfinalizedSoftwareProductList}
				onAddVLM={onAddLicenseModelClick}
				onAddVSP={onAddSoftwareProductClick}
				onSelectVLM={onSelectLicenseModel}
				onSelectVSP={onSelectSoftwareProduct}
				onMigrate={onMigrate}
				filter={searchValue}
				tileType={tabsMapping.WORKSPACE} />
		</div>);
};

export default WorkspaceView;
