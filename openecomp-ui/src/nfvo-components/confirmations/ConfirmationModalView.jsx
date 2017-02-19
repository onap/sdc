import React from 'react';
import Button from 'react-bootstrap/lib/Button.js';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';

let typeClass = {
	'default': 'primary',
	error: 'danger',
	warning: 'warning',
	success: 'success'
};


class ConfirmationModalView extends React.Component {

	static propTypes = {
		show: React.PropTypes.bool,
		type: React.PropTypes.oneOf(['default', 'error', 'warning', 'success']),
		msg: React.PropTypes.node,
		title: React.PropTypes.string,
		confirmationDetails: React.PropTypes.object,
		confirmationButtonText: React.PropTypes.string,

	};

	static defaultProps = {
		show: false,
		type: 'warning',
		title: 'Warning',
		msg: '',
		confirmationButtonText: i18n('Delete')
	};

	render() {
		let {title, type, msg, show, confirmationButtonText} = this.props;

		return(
			<Modal show={show} className={`notification-modal ${typeClass[type]}`}>
				<Modal.Header>
					<Modal.Title>{title}</Modal.Title>
				</Modal.Header>
				<Modal.Body>{msg}</Modal.Body>
				<Modal.Footer>
					<Button bsStyle={typeClass[type]} onClick={() => this.props.onDeclined(this.props.confirmationDetails)}>{i18n('Cancel')}</Button>
					<Button bsStyle={typeClass[type]} onClick={() => this.props.onConfirmed(this.props.confirmationDetails)}>{confirmationButtonText}</Button>
				</Modal.Footer>
			</Modal>
		);
	};
}

export default ConfirmationModalView;
