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
import PropTypes from 'prop-types';
import Button from 'sdc-ui/lib/react/Button.js';
import Sequencer from 'dox-sequence-diagram-ui';

import i18n from 'nfvo-utils/i18n/i18n.js';

class SequenceDiagram extends Component {

	static propTypes = {
		onSave: PropTypes.func.isRequired,
		onClose: PropTypes.func,
		model: PropTypes.object.isRequired
	};

	onSave() {
		this.props.onSave(this.refs.sequencer.getModel());
	}

	render() {
		return (
			<div className='sequence-diagram'>
				<div className='sequence-diagram-sequencer'>
					<Sequencer ref='sequencer' options={{useHtmlSelect: true}} model={this.props.model} />
				</div>
				<div className='sequence-diagram-action-buttons'>
					<Button onClick={() => this.onSave()}>{i18n('Save')}</Button>
					<Button onClick={this.props.onClose}>{i18n('Close')}</Button>
				</div>
			</div>
		);
	}

}

export default SequenceDiagram;
