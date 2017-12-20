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
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';

const SoftwareProductListHeader = ({selectedVendor, onBack}) => (
	<div className='vendor-page-header'>
		<SVGIcon name='back' onClick={onBack}/>
		<div className='tab-separator' />
		<div className='vendor-name'>{selectedVendor.name}</div>
	</div>
);

const CatalogList = ({children, onAddVLM, onAddVSP, vendorPageOptions}) => (
	<div className='catalog-list'>
		{vendorPageOptions && <SoftwareProductListHeader onBack={vendorPageOptions.onBack} selectedVendor={vendorPageOptions.selectedVendor}/>}
		<div className='catalog-items'>
			<div className='create-catalog-item-wrapper'>
				{onAddVLM && <CreateItemTile onClick={onAddVLM} dataTestId={'catalog-add-new-vlm'} className='vlm-type' title={i18n('CREATE NEW VLM')}/>}
				{onAddVSP && <CreateItemTile onClick={() => onAddVSP()} dataTestId={'catalog-add-new-vsp'} className='vsp-type' title={i18n('CREATE NEW VSP')}/>}
			</div>
			{children}
		</div>
	</div>
);

const CreateItemTile = ({onClick, dataTestId, title, className = ''}) => {
	//TODO check for buttons
	return (
			<div className={`create-catalog-item tile ${className}`} onClick={onClick} data-test-id={dataTestId}>
				<div className='create-item-plus-icon'><SVGIcon name='plus' /></div>
				<div className='create-item-text'>{title}</div>
			</div>
	);
};

export default CatalogList;
