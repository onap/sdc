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
import i18n from 'nfvo-utils/i18n/i18n.js';
import classnames from 'classnames';
import DetailsCatalogView from 'sdc-app/onboarding/onboard/DetailsCatalogView.jsx';
import VendorCatalogView from './VendorCatalogView.jsx';
import { tabsMapping} from './OnboardingCatalogConstants.js';
import {tabsMapping as WCTabsMapping} from 'sdc-app/onboarding/onboard/OnboardConstants.js';
import featureToggle from 'sdc-app/features/featureToggle.js';

const Separator = () => (
	<div className='tab-separator'/>
);

const Tab = ({onTabPress, title, dataTestId, activeTab, tabMapping}) => (
	<div
		className={classnames('catalog-header-tab', {'active': activeTab === tabMapping })}
		onClick={() => onTabPress(tabMapping)}
		data-test-id={dataTestId}>
		{title}
	</div>
);

const ArchiveTab = featureToggle('ARCHIVE_ITEM')(Tab);
const ArchiveTabSeparator = featureToggle('ARCHIVE_ITEM')(Separator);

const CatalogHeaderTabs = (props) => (
	<div className='catalog-header-tabs'>
		<Tab {...props} title={i18n('ACTIVE')}  dataTestId='catalog-all-tab' tabMapping={tabsMapping.ACTIVE}/>
		<Separator/>
		<Tab {...props} title={i18n('BY VENDOR')} dataTestId='catalog-header-tab' tabMapping={tabsMapping.BY_VENDOR}/>
		<ArchiveTabSeparator/>
		<ArchiveTab {...props} title={i18n('ARCHIVE')} dataTestId='catalog-archive-tab' tabMapping={tabsMapping.ARCHIVE}/>
	</div>
);

const CatalogHeader = ({activeTab, onTabPress}) => (
	<div className='catalog-header'>
		<CatalogHeaderTabs activeTab={activeTab} onTabPress={onTabPress} />
	</div>
);

const FilterCatalogHeader = () => (
	<div className='catalog-header'>
		<div className='catalog-header-tabs'>
			<div className='catalog-header-tab active'>
				{i18n('ONBOARD CATALOG')}
			</div>
		</div>	
	</div>
);


const FeaturedCatalogHeader = featureToggle('FILTER')({AComp: FilterCatalogHeader, BComp: CatalogHeader});

class OnboardingCatalogView extends React.Component {
	renderViewByTab(activeTab){
		const {finalizedLicenseModelList: licenseModelList, fullLicenseModelList, users, vspOverlay, finalizedSoftwareProductList: softwareProductList, onSelectLicenseModel, onSelectSoftwareProduct,
				onAddLicenseModelClick, onAddSoftwareProductClick, onVspOverlayChange, onVendorSelect, selectedVendor, searchValue, onMigrate,
				archivedSoftwareProductList, archivedLicenseModelList} = this.props;

		switch (activeTab){
			case tabsMapping.ARCHIVE:
				return (
					<DetailsCatalogView
						VLMList={archivedLicenseModelList}
						VSPList={archivedSoftwareProductList}
						users={users}						
						onSelectVLM={(item, users) => onSelectLicenseModel(item, users, WCTabsMapping.CATALOG)}
						onSelectVSP={(item, users) => onSelectSoftwareProduct(item, users, WCTabsMapping.CATALOG)}
						filter={searchValue}
						onMigrate={onMigrate}/>
				);
			case tabsMapping.ACTIVE:
				return (
					<DetailsCatalogView
						VLMList={licenseModelList}
						VSPList={softwareProductList}
						users={users}
						onAddVLM={onAddLicenseModelClick}
						onAddVSP={onAddSoftwareProductClick}
						onSelectVLM={(item, users) => onSelectLicenseModel(item, users, WCTabsMapping.CATALOG)}
						onSelectVSP={(item, users) => onSelectSoftwareProduct(item, users, WCTabsMapping.CATALOG)}
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
						onSelectVSP={(item, users) => onSelectSoftwareProduct(item, users, WCTabsMapping.CATALOG)}
						onSelectVLM={(item, users) => onSelectLicenseModel(item, users, WCTabsMapping.CATALOG)}
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
				{!selectedVendor && <FeaturedCatalogHeader
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
