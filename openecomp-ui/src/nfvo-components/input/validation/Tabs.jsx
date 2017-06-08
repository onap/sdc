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
import ReactDOM from 'react-dom';
import {default as BTabs} from 'react-bootstrap/lib/Tabs.js';
import Overlay from 'react-bootstrap/lib/Overlay.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';

import i18n from 'nfvo-utils/i18n/i18n.js';

export default
class Tabs extends React.Component {

	static propTypes = {
		children: React.PropTypes.node
	};

	cloneTab(element) {
		const {invalidTabs} = this.props;
		return React.cloneElement(
			element,
			{
				key: element.props.eventKey,
				tabClassName: invalidTabs.indexOf(element.props.eventKey) > -1 ? 'invalid-tab' : 'valid-tab'
			}
		);
	}

	showTabsError() {
		const {invalidTabs} = this.props;
		const showError = ((invalidTabs.length === 1 && invalidTabs[0] !== this.props.activeKey) || (invalidTabs.length > 1));
		return showError;
	}

	render() {
		// eslint-disable-next-line no-unused-vars
		let {invalidTabs, ...tabProps} = this.props;
		return (
			<div>
				<BTabs {...tabProps} ref='tabsList' id='tabsList' >
					{this.props.children.map(element => this.cloneTab(element))}
				</BTabs>
				<Overlay
					animation={false}
					show={this.showTabsError()}
					placement='bottom'
					containerPadding={50}
					target={() => {
						let target = ReactDOM.findDOMNode(this.refs.tabsList).querySelector('ul > li.invalid-tab:not(.active):nth-of-type(n)');
						return target && target.offsetParent ? target : undefined;
					}
					}
					container={() => {
						let target = ReactDOM.findDOMNode(this.refs.tabsList).querySelector('ul > li.invalid-tab:not(.active):nth-of-type(n)');
						return target && target.offsetParent ? target.offsetParent : this;
					}}>
					<Tooltip
						id='error-some-tabs-contain-errors'
						className='validation-error-message'>
						{i18n('One or more tabs are invalid')}
					</Tooltip>
				</Overlay>
			</div>
		);
	}
}
