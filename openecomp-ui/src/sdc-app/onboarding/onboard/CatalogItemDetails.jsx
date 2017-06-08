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
import {catalogItemTypeClasses, migrationStatusMapper} from './onboardingCatalog/OnboardingCatalogConstants.js';
import CatalogTile from './CatalogTile.jsx';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import {statusEnum, statusBarTextMap} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import SVGIcon from 'nfvo-components/icon/SVGIcon.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import tooltip from './onboardingCatalog/Tooltip.jsx';



const CatalogTileIcon = ({catalogItemTypeClass}) => (
		<div className={'catalog-tile-icon ' + catalogItemTypeClass}>
			<div className='icon'><SVGIcon
				name={catalogItemTypeClass === catalogItemTypeClasses.LICENSE_MODEL ? 'vlm' : 'vsp' }/>
			</div>
		</div>
);

const ItemTypeTitle = ({catalogItemTypeClass}) => {
	const itemTypeTitle = catalogItemTypeClass === catalogItemTypeClasses.LICENSE_MODEL ? i18n('VLM') : i18n('VSP');
	return(		
		<div className={`catalog-tile-type ${catalogItemTypeClass}`}>{itemTypeTitle}</div>
	);
};

const CatalogTileVendorName = ({vendorName, catalogItemTypeClass}) => { 
	const name = catalogItemTypeClass === catalogItemTypeClasses.SOFTWARE_PRODUCT ? vendorName : '';	
	return(
		<div>
			<OverlayTrigger placement='top' overlay={tooltip(name)}>
				<div className='catalog-tile-vendor-name'>{name}</div>
			</OverlayTrigger>
		</div>
	);
};

const CatalogTileItemName = ({name}) => (
	<div>
		<OverlayTrigger placement='top'  overlay={tooltip(name && name.toUpperCase())}>
			<div className='catalog-tile-item-name'>{name}</div>
		</OverlayTrigger>	
	</div>
);

const VersionInfo = ({version}) => (
	<div className='catalog-tile-version-info'>
		<div className='catalog-tile-item-version' data-test-id='catalog-item-version'>
			V {version}
		</div>
	</div>	
);

const EntityDetails = ({catalogItemData, catalogItemTypeClass}) => {
	const {vendorName, name, version} = catalogItemData;
	return (
		<div className='catalog-tile-entity-details'>
			<CatalogTileVendorName catalogItemTypeClass={catalogItemTypeClass} vendorName={vendorName}/>
			<CatalogTileItemName name={name}/>
			<VersionInfo version={version.label} />
		</div>			
	);
};


const  ItemStatusInfo = ({catalogItemTypeClass, lockingUser, itemStatus}) => {	
	const status = statusBarTextMap[itemStatus];
	const lockedBy = lockingUser ? ` by ${lockingUser}` : '';
	const toolTipMsg = `${status}${lockedBy}`;

	return (
		<div className={'catalog-tile-content ' + catalogItemTypeClass}>						
			<div className='catalog-tile-locking-user-name'>{i18n(status)}</div>
			<OverlayTrigger placement='top'  overlay={tooltip(toolTipMsg)}>							
				<div className='catalog-tile-check-in-status'><SVGIcon
					name={itemStatus === statusEnum.CHECK_OUT_STATUS ? 'unlocked' : 'locked'}
					data-test-id={itemStatus === statusEnum.CHECK_IN_STATUS ? 'catalog-item-checked-in' : 'catalog-item-checked-out'}/>
				</div>
			</OverlayTrigger>													
		</div>
		
	);
};

const  CatalogItemDetails = ({catalogItemData, catalogItemTypeClass, onSelect, onMigrate}) =>  {
	
	let {status: itemStatus} = VersionControllerUtils.getCheckOutStatusKindByUserID(catalogItemData.status, catalogItemData.lockingUser);
	
	return (
		<CatalogTile catalogItemTypeClass={catalogItemTypeClass} onSelect={() => {
			if (catalogItemData.isOldVersion && catalogItemData.isOldVersion === migrationStatusMapper.OLD_VERSION) {
				onMigrate({
					softwareProduct: catalogItemData
				});
			} else {
				onSelect();
			}
		}} data-test-id={catalogItemTypeClass}>
			<div className='catalog-tile-top item-details'>			
				<ItemTypeTitle catalogItemTypeClass={catalogItemTypeClass}/>				
				<CatalogTileIcon catalogItemTypeClass={catalogItemTypeClass}/>										
				<EntityDetails catalogItemTypeClass={catalogItemTypeClass} catalogItemData={catalogItemData} />
				<ItemStatusInfo itemStatus={itemStatus} catalogItemTypeClass={catalogItemTypeClass} lockingUser={catalogItemData.lockingUser} />								
			</div>
		</CatalogTile>
	);
	
};

CatalogItemDetails.PropTypes = {
	catalogItemData: React.PropTypes.obj,
	catalogItemTypeClass: React.PropTypes.string,
	onSelect: React.PropTypes.func,
	onMigrate: React.PropTypes.func
};

export default CatalogItemDetails;

