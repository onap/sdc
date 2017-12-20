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
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {catalogItemTypes, migrationStatusMapper} from './onboardingCatalog/OnboardingCatalogConstants.js';
import {Tile, TileInfo, TileInfoLine} from 'sdc-ui/lib/react';
import {TooltipWrapper} from './onboardingCatalog/Tooltip.jsx';

const ITEM_TYPE_MAP = {
	[catalogItemTypes.LICENSE_MODEL]: {
		headerText: i18n('VLM'),
		contentIconName: 'vlm',
		color: 'purple'
	},
	[catalogItemTypes.SOFTWARE_PRODUCT]: {
		headerText: i18n('VSP'),
		contentIconName: 'vsp',
		color: 'blue'
	}
};

const CatalogItemDetails = ({catalogItemData, catalogItemTypeClass, onSelect, onMigrate}) => {

	let {vendorName, name} = catalogItemData;
	let {headerText, color, contentIconName} = ITEM_TYPE_MAP[catalogItemTypeClass];

	let onClick = (e) => {
		e.stopPropagation();
		e.preventDefault();
		if (catalogItemData.isOldVersion && catalogItemData.isOldVersion === migrationStatusMapper.OLD_VERSION) {
			onMigrate({softwareProduct: catalogItemData});
		} else {
			onSelect();
		}
	};

	return (
		<Tile
			headerText={headerText}
			headerColor={color}
			iconName={contentIconName}
			iconColor={color}
			onClick={onClick}
			dataTestId={catalogItemTypeClass}>
			<TileInfo data-test-id='catalog-item-content'>
				{vendorName &&
					<TileInfoLine type='supertitle'>
						<TooltipWrapper className='with-overlay' tooltipClassName='tile-super-info' dataTestId='catalog-item-vendor-name'>{vendorName}</TooltipWrapper>
					</TileInfoLine>
				}
				<TileInfoLine type='title'>
					<TooltipWrapper className='with-overlay' tooltipClassName='tile-title-info' dataTestId='catalog-item-name'>{name}</TooltipWrapper>
				</TileInfoLine>
			</TileInfo>
		</Tile>
	);

};

CatalogItemDetails.PropTypes = {
	catalogItemData: PropTypes.obj,
	catalogItemTypeClass: PropTypes.string,
	onSelect: PropTypes.func,
	onMigrate: PropTypes.func
};

export default CatalogItemDetails;
