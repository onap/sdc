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
 * Holds the buttons for save/reset for forms.
 * Used by the ValidationForm that changes the state of the buttons according to its own state.
 *
 * properties:
 * labledButtons - whether or not to use labeled buttons or icons only
 */
import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Button from 'react-bootstrap/lib/Button.js';
import SVGIcon from 'nfvo-components/icon/SVGIcon.jsx';

class ValidationButtons extends React.Component {

	static propTypes = {
		labledButtons: React.PropTypes.bool.isRequired,
		isReadOnlyMode: React.PropTypes.bool
	};

	state = {
		isValid: this.props.formValid
	};

	render() {
		var submitBtn = this.props.labledButtons ? i18n('Save') : <SVGIcon className='check' name='check'/>;
		var closeBtn = this.props.labledButtons ? i18n('Cancel') : <SVGIcon className='close' name='close'/>;
		return (
			<div className='validation-buttons'>
				{!this.props.isReadOnlyMode ?
					<div>
						<Button  bsStyle='primary' ref='submitbutton' type='submit' disabled={!this.state.isValid}>{submitBtn}</Button>
						<Button  type='reset'>{closeBtn}</Button>
					</div>
					: <Button  type='reset'>{i18n('Close')}</Button>
				}
			</div>
		);
	}
}
export default ValidationButtons;
