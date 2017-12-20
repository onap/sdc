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
import VendorItem from './VendorItem.jsx';
import CatalogList from '../CatalogList.jsx';
import CatalogItemDetails from '../CatalogItemDetails.jsx';
import {catalogItemTypes} from './OnboardingCatalogConstants.js';
import {filterCatalogItemsByType} from './OnboardingCatalogUtils.js';

const VendorList = ({onAddVLM, onAddVSP, onSelectVSP, licenseModelList = [], vspOverlay: currentOverlay, onVspOverlayChange, onVendorSelect, filter, onMigrate, users}) => {
	return(
		<CatalogList onAddVLM={onAddVLM} onAddVSP={onAddVSP}>
			{
				filterCatalogItemsByType({items: licenseModelList, filter}).map(vlm =>
					<VendorItem
						key={vlm.id}
						onAddVSP={onAddVSP}
						onSelectVSP={(vsp) => onSelectVSP(vsp, users)}
						shouldShowOverlay={currentOverlay === vlm.id}
						onVSPButtonClick={(hasVSP) => onVspOverlayChange(vlm.id === currentOverlay || !hasVSP ? null : vlm)}
						onVendorSelect={onVendorSelect}
						onMigrate={onMigrate}
						vendor={vlm}/>)
			}
		</CatalogList>
	);
};

const SoftwareProductListByVendor = ({onAddVSP, selectedVendor, onVendorSelect, onSelectVSP, onSelectVLM, filter, onMigrate, users}) => {
	return(
		<div>
			<CatalogList onAddVSP={()=>{onAddVSP(selectedVendor.id);}} vendorPageOptions={{selectedVendor, onBack: () => onVendorSelect(false)}}>
				<CatalogItemDetails
					key={selectedVendor.id}
					onSelect={() => onSelectVLM(selectedVendor, users)}
					catalogItemTypeClass={catalogItemTypes.LICENSE_MODEL}
					onMigrate={onMigrate}
					catalogItemData={selectedVendor}/>
				{
					filterCatalogItemsByType({items: selectedVendor.softwareProductList, filter}).map(vsp =>
						<CatalogItemDetails
							key={vsp.id}
							catalogItemTypeClass={catalogItemTypes.SOFTWARE_PRODUCT}
							onMigrate={onMigrate}
							onSelect={() => onSelectVSP(vsp, users)}
							catalogItemData={vsp}/>
					)
				}
			</CatalogList>
		</div>
	);
};

class VendorCatalogView extends React.Component {
	render() {
		let {selectedVendor} = this.props;
		return( selectedVendor ? <SoftwareProductListByVendor {...this.props}/> : <VendorList {...this.props}/>);
	}
}

export default VendorCatalogView;
