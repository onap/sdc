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
import {selectedButton} from '../LicenseModelOverviewConstants.js';

function ListButtons ({onTabSelect, selectedInUse}) {
	return (
		<div className='overview-buttons-section'>
			<div onClick={()=>onTabSelect(selectedButton.VLM_LIST_VIEW)}
				className={selectedInUse ? 'button-vlm-list-view  vlm-list-icon selected' : 'button-vlm-list-view   vlm-list-icon' }
				data-test-id='vlm-overview-vlmlist-tab'></div>
			<div onClick={()=>onTabSelect(selectedButton.NOT_IN_USE)}
				className={selectedInUse ? 'button-vlm-list-view entities-list-icon' : 'button-vlm-list-view  entities-list-icon selected'  }
				data-test-id='vlm-overview-orphans-tab' >
			</div>

		</div>
	);
}

ListButtons.propTypes = {
	onTabSelect: React.PropTypes.func,
	selectedInUse: React.PropTypes.bool
};

export default ListButtons;
