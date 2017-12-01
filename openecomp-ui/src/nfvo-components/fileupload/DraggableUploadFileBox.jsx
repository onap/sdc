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
import Button from 'sdc-ui/lib/react/Button.js';

class DraggableUploadFileBox extends Component {
	render() {
		let {className, onClick, dataTestId, isReadOnlyMode} = this.props;
		return (
			<div
				className={`${className}${isReadOnlyMode ? ' disabled' : ''}`}>
				<div className={`${'drag-text'}${isReadOnlyMode ? ' disabled' : ''}`}>{i18n('Drag & drop for upload')}</div>
				<div className='or-text'>{i18n('or')}</div>
				<Button type='button' data-test-id={dataTestId} btnType='outline' onClick={onClick} disabled={isReadOnlyMode === true}>{i18n('Select File')}</Button>
			</div>
		);
	}
}
export default DraggableUploadFileBox;
