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
import Input from 'nfvo-components/input/validation/Input.jsx';

class LicenseModelDescriptionEdit extends React.Component {
	render() {
		//TODO check if buttons
		let {onDataChanged, description, genericFieldInfo} = this.props;
		let {isValid, errorText} = genericFieldInfo.description;
		let saveButtonClassName = isValid ? 'description-save' : 'description-save disabled';
		return(
			<div className='vendor-description-edit'>

				<Input
					onChange={description => onDataChanged({description})}
					value={description}
					isValid={isValid}
					errorText={errorText}
					className='description-edit-textarea'
					type='textarea'/>
				<div className='buttons-row'>
					<div className='buttons-wrapper'>
						<div onClick={() => this.submit()}  className={saveButtonClassName} data-test-id='vlm-summary-vendor-desc-save-btn'>{i18n('Save')}</div>
						<div onClick={() => this.onClose()} className='description-button' data-test-id='vlm-summary-vendor-desc-cancel-btn'>{i18n('Cancel')}</div>
					</div>
				</div>
			</div>
		);
	}

	onClose() {
		this.props.onClose();
	}

	submit() {
		let {onSubmit, data, description} = this.props;
		onSubmit({
			...data,
			description: description.trim()
		});
	}
}

export default LicenseModelDescriptionEdit;
