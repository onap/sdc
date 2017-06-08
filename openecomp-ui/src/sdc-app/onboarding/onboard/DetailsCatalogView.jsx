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
import {catalogItemTypes, modalMapper, catalogItemTypeClasses} from './onboardingCatalog/OnboardingCatalogConstants.js';
import {filterCatalogItemsByType} from './onboardingCatalog/OnboardingCatalogUtils.js';
import CatalogList from './CatalogList.jsx';
import CatalogItemDetails from './CatalogItemDetails.jsx';

class DetailsCatalogView extends React.Component{

	static propTypes = {
		VLMList: React.PropTypes.array,
		VSPList: React.PropTypes.array,
		onSelectVLM: React.PropTypes.func.isRequired,
		onSelectVSP: React.PropTypes.func.isRequired,
		onAddVLM: React.PropTypes.func.isRequired,
		onAddVSP: React.PropTypes.func.isRequired,
		filter: React.PropTypes.string.isRequired
	};

	renderCatalogItems(items, type, filter, onSelect, onMigrate, tileType){
		return filterCatalogItemsByType(items, type, filter).map(item =>
		<CatalogItemDetails
			key={item.id}
			catalogItemData={type === catalogItemTypes.LICENSE_MODEL ? {...item, name: item.vendorName} : item}
			catalogItemTypeClass={catalogItemTypeClasses[modalMapper[type]]}
			onMigrate={onMigrate}
			onSelect={() => onSelect(item)}
			tileType={tileType} />
		);
	}

	render() {
		let {VLMList, VSPList, onAddVSP, onAddVLM, onSelectVLM, onSelectVSP, filter = '', onMigrate, tileType} = this.props;
		return (
			<CatalogList onAddVLM={onAddVLM} onAddVSP={onAddVSP}>
				{this.renderCatalogItems(VLMList, catalogItemTypes.LICENSE_MODEL, filter, onSelectVLM, onMigrate, tileType)}
				{this.renderCatalogItems(VSPList, catalogItemTypes.SOFTWARE_PRODUCT, filter, onSelectVSP, onMigrate, tileType)}
			</CatalogList>
		);
	}
}
export default DetailsCatalogView;
