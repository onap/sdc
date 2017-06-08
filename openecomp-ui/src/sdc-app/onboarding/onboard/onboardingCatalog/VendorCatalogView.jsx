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
import {catalogItemTypes, catalogItemTypeClasses} from './OnboardingCatalogConstants.js';
import {filterCatalogItemsByType} from './OnboardingCatalogUtils.js';

const VendorList = ({onAddVLM, onAddVSP, onSelectVSP, licenseModelList = [], vspOverlay: currentOverlay, onVspOverlayChange, onVendorSelect, filter, onMigrate}) => {
	return(
		<CatalogList onAddVLM={onAddVLM} onAddVSP={onAddVSP}>
			{
				filterCatalogItemsByType(licenseModelList, catalogItemTypes.LICENSE_MODEL, filter).map(vlm =>
					<VendorItem
						key={vlm.id}
						onAddVSP={onAddVSP}
						onSelectVSP={onSelectVSP}
						shouldShowOverlay={currentOverlay === vlm.id}
						onVSPIconClick={(hasVSP) => onVspOverlayChange(vlm.id === currentOverlay || !hasVSP ? null : vlm)}
						onVendorSelect={onVendorSelect}
						onMigrate={onMigrate}
						vendor={vlm}/>)
			}
		</CatalogList>
	);
};

const SoftwareProductListByVendor = ({onAddVSP, selectedVendor, onVendorSelect, onSelectVSP, onSelectVLM, filter, onMigrate}) => {
	return(
		<div>
			<CatalogList onAddVSP={()=>{onAddVSP(selectedVendor.id);}} vendorPageOptions={{selectedVendor, onBack: () => onVendorSelect(false)}}>
				<CatalogItemDetails
					key={selectedVendor.id}
					onSelect={() => onSelectVLM(selectedVendor)}
					catalogItemTypeClass={catalogItemTypeClasses.LICENSE_MODEL}
					onMigrate={onMigrate}
					catalogItemData={{...selectedVendor, name: selectedVendor.vendorName}}/>
				{
					filterCatalogItemsByType(selectedVendor.softwareProductList, catalogItemTypes.SOFTWARE_PRODUCT, filter).map(vsp =>
						<CatalogItemDetails
							key={vsp.id}
							catalogItemTypeClass={catalogItemTypeClasses.SOFTWARE_PRODUCT}
							onMigrate={onMigrate}
							onSelect={() => onSelectVSP(vsp)}
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
