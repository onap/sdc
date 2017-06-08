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
import {connect} from 'react-redux';

import Modal from 'nfvo-components/modal/Modal.jsx';
import Button from 'react-bootstrap/lib/Button.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {modalContentComponents} from 'sdc-app/common/modal/ModalContentMapper.js';
import {actionTypes, typeEnum} from './GlobalModalConstants.js';


const typeClass = {
	'default': 'primary',
	error: 'danger',
	warning: 'warning',
	success: 'success'
};


const ModalFooter = ({type, onConfirmed, onDeclined, onClose, confirmationButtonText, cancelButtonText}) => 
		<Modal.Footer>
			<Button bsStyle={typeClass[type]} onClick={onDeclined ? () => {
				onDeclined(); 
				onClose();} : () => onClose()}>
				{cancelButtonText}
				</Button>
			{onConfirmed && <Button bsStyle={typeClass[type]} onClick={() => {
				onConfirmed();
				onClose();
			}}>{confirmationButtonText}</Button>}
		</Modal.Footer>;

ModalFooter.defaultProps = {
	type: 'default',
	confirmationButtonText: i18n('OK'),
	cancelButtonText: i18n('Cancel')
};

export const mapStateToProps = ({modal}) => {
	const show = !!modal;
	return {
		show,
		...modal
	};
};

export const mapActionToProps = (dispatch) => {
	return {
		onClose:  () => dispatch({type: actionTypes.GLOBAL_MODAL_CLOSE})
	};
};


export class  GlobalModalView extends React.Component {

	static propTypes = {
		show: React.PropTypes.bool,
		type: React.PropTypes.oneOf(['default', 'error', 'warning', 'success']),
		title: React.PropTypes.string,
		modalComponentProps: React.PropTypes.object,
		modalComponentName: React.PropTypes.string,
		onConfirmed: React.PropTypes.func,
		onDeclined: React.PropTypes.func,
		confirmationButtonText: React.PropTypes.string,
		cancelButtonText: React.PropTypes.string
	};

	static defaultProps = {
		show: false,
		type: 'default',
		title: ''
	};

	render() {
		let {title, type, show, modalComponentName, modalComponentProps, 
		modalClassName, msg, onConfirmed, onDeclined, confirmationButtonText, cancelButtonText, onClose} = this.props;
		const  ComponentToRender = modalContentComponents[modalComponentName];
		return (
			<Modal show={show} bsSize={modalComponentProps && modalComponentProps.size} className={`onborading-modal ${modalClassName || ''} ${typeClass[type]}`}>
				<Modal.Header>
					<Modal.Title>{title}</Modal.Title>
				</Modal.Header>
				<Modal.Body>
					{ComponentToRender ? <ComponentToRender {...modalComponentProps}/> :  msg}				
				</Modal.Body>
				{(onConfirmed || onDeclined || type !== typeEnum.DEFAULT) &&
						<ModalFooter
							type={type}
							onConfirmed={onConfirmed}
							onDeclined={onDeclined}
							onClose={onClose}
							confirmationButtonText={confirmationButtonText}
							cancelButtonText={cancelButtonText}/>}
			</Modal>
		);
	}

	componentDidUpdate() {
		if (this.props.timeout) {
			setTimeout(this.props.onClose, this.props.timeout);
		}
	}	
};

export default connect(mapStateToProps, mapActionToProps, null, {withRef: true})(GlobalModalView);
