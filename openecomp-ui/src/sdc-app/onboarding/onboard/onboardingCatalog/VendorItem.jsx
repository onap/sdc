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
import {catalogItemTypeClasses} from './OnboardingCatalogConstants.js';
import CatalogTile from '../CatalogTile.jsx';
import classnames from 'classnames';
import VSPOverlay from './VSPOverlay.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import tooltip from './Tooltip.jsx';


class VendorItem extends React.Component {

	static PropTypes = {
		softwareProductList: React.PropTypes.array,
		vendor: React.PropTypes.object,
		onSelectVSP: React.PropTypes.func,
		shouldShowOverlay: React.PropTypes.boolm,
		onVendorSelect: React.PropTypes.func,
		onAddVSP: React.PropTypes.func,
		onVSPIconClick: React.PropTypes.func,

	};

	render() {
		let {vendor, onSelectVSP, shouldShowOverlay, onVendorSelect, onMigrate} = this.props;
		let {softwareProductList = [], vendorName} = vendor;
		return (
			<CatalogTile
				catalogItemTypeClass={catalogItemTypeClasses.VENDOR}
				onSelect={() =>  onVendorSelect(vendor)}>
				<div className='catalog-tile-top'>
					<div className='catalog-tile-icon vendor-type'>
						<div className='icon'><SVGIcon name='vendor'/></div>
					</div>
					<OverlayTrigger placement='top' overlay={tooltip(vendorName)}>
						<div className='catalog-tile-item-name'>{vendorName}</div>
					</OverlayTrigger>
					<div
						className={classnames('catalog-tile-vsp-count', {active: shouldShowOverlay}, {clickable: softwareProductList.length})}
						onClick={(event) => this.handleVspCountClick(event)}
						data-test-id='catalog-vsp-count'>
						{i18n(`${softwareProductList.length} VSPs`)}
					</div>
					<div className='catalog-tile-content' onClick={(event) => this.onCreateVspClick(event)} data-test-id='catalog-create-new-vsp-from-vendor'>
						<div className='create-new-vsp-button'>
							<SVGIcon name='plus'/>&nbsp;&nbsp;&nbsp;{i18n('Create new VSP')}
						</div>
					</div>
				</div>

				{shouldShowOverlay && softwareProductList.length > 0
				&& <VSPOverlay onMigrate={onMigrate} VSPList={softwareProductList} onSelectVSP={onSelectVSP} onSeeMore={() => onVendorSelect(vendor)}/>}
			</CatalogTile>
		);
	}

	onClick(vlm) {
		this.setState({
			licenseModelToShow: vlm
		});
	}

	onCreateVspClick(event) {
		let {onAddVSP, vendor: {id}} = this.props;
		event.stopPropagation();
		event.preventDefault();
		onAddVSP(id);
	}

	handleVspCountClick(e){
		let {onVSPIconClick, vendor: {softwareProductList}} = this.props;
		e.stopPropagation();
		e.preventDefault();
		const hasVSP = Boolean(softwareProductList.length);
		onVSPIconClick(hasVSP);
	}

}

export default VendorItem;
