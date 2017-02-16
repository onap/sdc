/**
 * NotificationModal options:
 *
 * show: whether to show notification or not,
 * type: the type of the notification. valid values are: 'default', 'error', 'warning', 'success'
 * msg: the notification content. could be a string or node (React component)
 * title: the notification title
 * timeout: timeout for the notification to fade out. if timeout == 0 then the notification is rendered until the user closes it
 *
 */
import React, {Component, PropTypes} from 'react';
import {connect} from 'react-redux';
import Button from 'react-bootstrap/lib/Button.js';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';
import SubmitErrorResponse from 'nfvo-components/SubmitErrorResponse.jsx';
import NotificationConstants from './NotificationConstants.js';

let typeClass = {
	'default': 'primary',
	error: 'danger',
	warning: 'warning',
	success: 'success'
};

const mapActionsToProps = (dispatch) => {
	return {onCloseClick: () => dispatch({type: NotificationConstants.NOTIFY_CLOSE})};
};

const mapStateToProps = ({notification}) => {

	let show = notification !== null && notification.title !== 'Conflict';
	let mapResult = {show};
	if (show) {
		mapResult = {show, ...notification};
	}

	return mapResult;
};

export class NotificationModal extends Component {

	static propTypes = {
		show: PropTypes.bool,
		type: PropTypes.oneOf(['default', 'error', 'warning', 'success']),
		title: PropTypes.string,
		msg: PropTypes.node,
		validationResponse: PropTypes.object,
		timeout: PropTypes.number
	};

	static defaultProps = {
		show: false,
		type: 'default',
		title: '',
		msg: '',
		timeout: 0
	};

	state = {type: undefined};

	componentWillReceiveProps(nextProps) {
		if (this.props.show !== nextProps.show && nextProps.show === false) {
			this.setState({type: this.props.type});
		}
		else {
			this.setState({type: undefined});
		}
	}

	componentDidUpdate() {
		if (this.props.timeout) {
			setTimeout(this.props.onCloseClick, this.props.timeout);
		}
	}

	render() {
		let {title, type, msg, show, validationResponse, onCloseClick} = this.props;
		if (!show) {
			type = this.state.type;
		}
		if (validationResponse) {
			msg = (<SubmitErrorResponse validationResponse={validationResponse}/>);
		}
		return (
			<Modal show={show} className={`notification-modal ${typeClass[type]}`}>
				<Modal.Header>
					<Modal.Title>{title}</Modal.Title>
				</Modal.Header>
				<Modal.Body>{msg}</Modal.Body>
				<Modal.Footer>
					<Button bsStyle={typeClass[type]} onClick={onCloseClick}>{i18n('OK')}</Button>
				</Modal.Footer>
			</Modal>
		);
	}
}

export default connect(mapStateToProps, mapActionsToProps)(NotificationModal);
