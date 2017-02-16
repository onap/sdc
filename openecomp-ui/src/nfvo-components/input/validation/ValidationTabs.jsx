import React from 'react';
import ReactDOM from 'react-dom';
import Tabs from 'react-bootstrap/lib/Tabs.js';
import Overlay from 'react-bootstrap/lib/Overlay.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';

import i18n from 'nfvo-utils/i18n/i18n.js';

export default
class ValidationTab extends React.Component {

	static propTypes = {
		children: React.PropTypes.node
	};

	state = {
		invalidTabs: []
	};

	cloneTab(element) {
		const {invalidTabs} = this.state;
		return React.cloneElement(
			element,
			{
				key: element.props.eventKey,
				tabClassName: invalidTabs.indexOf(element.props.eventKey) > -1 ? 'invalid-tab' : 'valid-tab',
				onValidationStateChange: (eventKey, isValid) => this.validTabStateChanged(eventKey, isValid)
			}
		);
	}

	validTabStateChanged(eventKey, isValid) {
		let {invalidTabs} = this.state;
		let invalidTabIndex = invalidTabs.indexOf(eventKey);
		if (isValid && invalidTabIndex > -1) {
			this.setState({invalidTabs: invalidTabs.filter(otherEventKey => eventKey !== otherEventKey)});
		} else if (!isValid && invalidTabIndex === -1) {
			this.setState({invalidTabs: [...invalidTabs, eventKey]});
		}
	}

	showTabsError() {
		const {invalidTabs} = this.state;
		return invalidTabs.length > 0 && (invalidTabs.length > 1 || invalidTabs[0] !== this.props.activeKey);
	}

	render() {
		return (
			<div>
				<Tabs {...this.props} ref='tabsList'>
					{this.props.children.map(element => this.cloneTab(element))}
				</Tabs>
				<Overlay
					animation={false}
					show={this.showTabsError()}
					placement='bottom'
					target={() => {
						let target = ReactDOM.findDOMNode(this.refs.tabsList).querySelector('ul > li.invalid-tab:not(.active):nth-of-type(n)');
						return target && target.offsetParent ? target : undefined;
					}
					}
					container={this}>
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
