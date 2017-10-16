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
/**
 * The HTML structure here is aligned with bootstrap HTML structure for form elements.
 * In this way we have proper styling and it is aligned with other form elements on screen.
 *
 * Select and MultiSelect options:
 *
 * label - the label to be shown which paired with the input
 *
 * all other "react-select" props - as documented on
 * http://jedwatson.github.io/react-select/
 * or
 * https://github.com/JedWatson/react-select
 */
import React, {Component} from 'react';
import DraggableUploadFileBox from 'nfvo-components/fileupload/DraggableUploadFileBox.jsx';
import Configuration from 'sdc-app/config/Configuration.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';

function VNFBrowse({onBrowseVNF, isReadOnlyMode}) {
	if(!Configuration.get('showBrowseVNF')) {
		return (
			<div/>
		);
	}
	else {
		return (
			<div className={`${'vnfRepo'}${isReadOnlyMode ? ' disabled' : ''}`} onClick={onBrowseVNF}>
				<div className={`${'searchRepo-text'}`}>{i18n('Search in Repository')}</div>
	            <SVGIcon name='search' color='positive' iconClassName='searchIcon'/>
			</div>
		);
	}
}

class VnfRepositorySearchBox extends Component {
	render() {
		let {className, onClick, onBrowseVNF, dataTestId, isReadOnlyMode} = this.props;
		let showVNF = Configuration.get('showBrowseVNF');
		return (
			<div
				className={`${className}${isReadOnlyMode ? ' disabled' : ''}`}>
				<DraggableUploadFileBox
					dataTestId={dataTestId}
					isReadOnlyMode={isReadOnlyMode}
					className={'upload'}
					onClick={onClick}/>

				<div className={`${'verticalLine'}${showVNF ? '' : ' hide'}`}></div>

				<VNFBrowse onBrowseVNF={onBrowseVNF} isReadOnlyMode={isReadOnlyMode}/>
			</div>
		);
		
	}
}
export default VnfRepositorySearchBox;
