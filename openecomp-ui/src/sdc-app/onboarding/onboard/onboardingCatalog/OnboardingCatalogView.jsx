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
import i18n from 'nfvo-utils/i18n/i18n.js';
import classnames from 'classnames';
import DetailsCatalogView from 'sdc-app/onboarding/onboard/DetailsCatalogView.jsx';
import VendorCatalogView from './VendorCatalogView.jsx';
import { tabsMapping} from './OnboardingCatalogConstants.js';

const CatalogHeaderTabs = ({onTabPress, activeTab}) => (
	<div className='catalog-header-tabs'>
		<div
			className={classnames('catalog-header-tab', {'active': activeTab === tabsMapping.ALL })}
			onClick={() => onTabPress(tabsMapping.ALL)}
			data-test-id='catalog-all-tab'>
			{i18n('ALL')}
		</div>
		<div className='tab-separator'/>
		<div
			className={classnames('catalog-header-tab', {'active': activeTab === tabsMapping.BY_VENDOR })}
			onClick={() => onTabPress(tabsMapping.BY_VENDOR)}
			data-test-id='catalog-by-vendor-tab'>
			{i18n('BY VENDOR')}
		</div>
	</div>
);

const CatalogHeader = ({activeTab, onTabPress}) => (
	<div className='catalog-header'>
		<CatalogHeaderTabs activeTab={activeTab} onTabPress={onTabPress} />
	</div>
);

class OnboardingCatalogView extends React.Component {
	renderViewByTab(activeTab){
		const {finalizedLicenseModelList: licenseModelList, fullLicenseModelList, users, vspOverlay, finalizedSoftwareProductList: softwareProductList, onSelectLicenseModel, onSelectSoftwareProduct,
				onAddLicenseModelClick, onAddSoftwareProductClick, onVspOverlayChange, onVendorSelect, selectedVendor, searchValue, onMigrate} = this.props;

		switch (activeTab){
			case tabsMapping.ALL:
				return (
					<DetailsCatalogView
						VLMList={licenseModelList}
						VSPList={softwareProductList}
						users={users}
						onAddVLM={onAddLicenseModelClick}
						onAddVSP={onAddSoftwareProductClick}
						onSelectVLM={onSelectLicenseModel}
						onSelectVSP={onSelectSoftwareProduct}
						filter={searchValue}
						onMigrate={onMigrate}/>
				);
			case tabsMapping.BY_VENDOR:
			default:
				return (
					<VendorCatalogView
						licenseModelList={fullLicenseModelList}
						users={users}
						onAddVSP={onAddSoftwareProductClick}
						onAddVLM={onAddLicenseModelClick}
						onSelectVSP={onSelectSoftwareProduct}
						onSelectVLM={onSelectLicenseModel}
						vspOverlay={vspOverlay}
						onVendorSelect={onVendorSelect}
						selectedVendor={selectedVendor}
						onVspOverlayChange={onVspOverlayChange}
						onMigrate={onMigrate}
						filter={searchValue}/>
				);
		}
	}

	render() {
		const {selectedVendor, catalogActiveTab: activeTab, onCatalogTabClick, onSearch, searchValue} = this.props;
		return (
			<div className='catalog-wrapper'>
				{!selectedVendor && <CatalogHeader
					onSearch={event => onSearch(event.target.value)}
					activeTab={activeTab}
					onTabPress={tab => onCatalogTabClick(tab)}
					searchValue={searchValue}/>}
				{this.renderViewByTab(activeTab)}
			</div>
		);
	}
}

export default OnboardingCatalogView;
