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
import React, {Component} from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {default as VendorDataView} from './summary/VendorDataView.js';
import {default as SummaryCountList} from './summary/SummaryCountList.js';

class SummaryView extends Component {
	render() {
		const {isReadOnlyMode} = this.props;
		return(
			<div className='overview-top-section'>
				<div className='page-title'>{i18n('overview')}</div>
				<div className='license-model-overview-top'>
					<VendorDataView isReadOnlyMode={isReadOnlyMode}/>
					<SummaryCountList isReadOnlyMode={isReadOnlyMode}/>
				</div>
			</div>
		);
	}
}

export default SummaryView;
