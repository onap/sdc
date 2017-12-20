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
import {Tile, TileInfo, TileInfoLine, TileFooter, TileFooterCell, Button} from 'sdc-ui/lib/react';

import VSPOverlay from './VSPOverlay.jsx';
import {TooltipWrapper} from './Tooltip.jsx';

class VendorItem extends React.Component {

	static PropTypes = {
		softwareProductList: PropTypes.array,
		vendor: PropTypes.object,
		shouldShowOverlay: PropTypes.bool,
		onSelectVSP: PropTypes.func,
		onVendorSelect: PropTypes.func,
		onAddVSP: PropTypes.func,
		onVSPButtonClick: PropTypes.func
	};

	render() {
		let {vendor, onSelectVSP, shouldShowOverlay, onVendorSelect, onMigrate} = this.props;
		let {softwareProductList = [], name} = vendor;
		return (
			<Tile
				iconName='vendor'
				onClick={() => onVendorSelect(vendor)}>
				<TileInfo align='center'>
					<TileInfoLine type='title'>
						<TooltipWrapper className='with-overlay' dataTestId='catalog-item-name'>{name}</TooltipWrapper>
					</TileInfoLine>
					<TileInfoLine>
						<Button
							btnType='outline-rounded'
							color='dark-gray'
							onClick={e => this.handleVspCountClick(e)}
							data-test-id='catalog-vsp-count'
							disabled={!softwareProductList.length}>
							{i18n('{length} VSPs', {length: softwareProductList.length})}
						</Button>
						{shouldShowOverlay && softwareProductList.length > 0 &&
							<VSPOverlay
								onMigrate={onMigrate}
								VSPList={softwareProductList}
								onSelectVSP={onSelectVSP}
								onSeeMore={() => onVendorSelect(vendor)} />
						}
					</TileInfoLine>
				</TileInfo>
				<TileFooter align='center'>
					<TileFooterCell dataTestId='catalog-create-new-vsp-from-vendor'>
						<Button
							btnType='link'
							color='primary'
							iconName='plusThin'
							onClick={e => this.onCreateVspClick(e)}>
							{i18n('Create new VSP')}
						</Button>
					</TileFooterCell>
				</TileFooter>
			</Tile>
		);
	}

	onCreateVspClick(e) {
		e.stopPropagation();
		e.preventDefault();
		const {onAddVSP, vendor: {id}} = this.props;
		onAddVSP(id);
	}

	handleVspCountClick(e){
		e.stopPropagation();
		e.preventDefault();
		const {onVSPButtonClick, vendor: {softwareProductList}} = this.props;
		const hasVSP = Boolean(softwareProductList.length);
		onVSPButtonClick(hasVSP);
	}

}

export default VendorItem;
