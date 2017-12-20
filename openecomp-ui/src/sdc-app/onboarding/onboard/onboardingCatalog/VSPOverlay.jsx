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
import {migrationStatusMapper} from './OnboardingCatalogConstants.js';

const VSPOverlay = ({VSPList, onSelectVSP, onSeeMore, onMigrate}) => (
	<div className='vsp-overlay-wrapper' onClick={(e) => {
		e.stopPropagation();
		e.preventDefault();
	}}>
		<div className='vsp-overlay-arrow'></div>
		<div className='vsp-overlay'>
			<div className='vsp-overlay-title'>{i18n('Recently Edited')}</div>
			<div className='vsp-overlay-list'>
				{VSPList.slice(0, 5).map(vsp => <div key={vsp.id} className='vsp-overlay-detail' onClick={() => {
					if (vsp.isOldVersion && vsp.isOldVersion === migrationStatusMapper.OLD_VERSION) {
						onMigrate({
							softwareProduct: vsp
						});
					} else {
						onSelectVSP(vsp);
					}
				}
				}>{i18n(vsp.name)}</div>)}
			</div>
			{VSPList.length > 5 && <div className='vsp-overlay-see-more' onClick={onSeeMore}>{i18n('See More')}</div>}
		</div>
	</div>
);

VSPOverlay.PropTypes = {
	VSPList: PropTypes.array,
	onSelectVSP: PropTypes.func
};

export default VSPOverlay;
