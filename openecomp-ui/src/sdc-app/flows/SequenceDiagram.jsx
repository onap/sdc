import React, {Component, PropTypes} from 'react';
import Button from 'react-bootstrap/lib/Button.js';
import Sequencer from 'dox-sequence-diagram-ui';

import i18n from 'nfvo-utils/i18n/i18n.js';

class SequenceDiagram extends Component {

	static propTypes = {
		onSave: PropTypes.func.isRequired,
		onClose: PropTypes.func.isRequired,
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
					<Button className='primary-btn' onClick={() => this.onSave()}>{i18n('Save')}</Button>
					<Button className='primary-btn' onClick={this.props.onClose}>{i18n('Close')}</Button>
				</div>
			</div>
		);
	}

}

export default SequenceDiagram;
