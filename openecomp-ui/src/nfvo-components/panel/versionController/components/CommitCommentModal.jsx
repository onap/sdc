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
import i18n from 'nfvo-utils/i18n/i18n.js';
import Form from 'nfvo-components/input/validation/Form.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import keyMirror from 'nfvo-utils/KeyMirror.js';

export const CommitModalType = keyMirror({
	COMMIT: null,
	COMMIT_SUBMIT: null

});

export const mapActionToProps = (dispatch) => {
	return {
		onClose: () => dispatch({
			type: modalActionTypes.GLOBAL_MODAL_CLOSE
		})
	};
};

class CommitCommentModal extends React.Component {

	state = {
		comment: ''
	};

	render() {
		const {onCommit, onClose, type} = this.props;
		const [commitButtonText, descriptionText] = type === CommitModalType.COMMIT ?
			[i18n('Commit'), i18n('You are about to commit your version')] :
			[i18n('Commit & Submit'), i18n('You must commit your changes before the submit')];

		return (
			<Form
				ref='validationForm'
				hasButtons={true}
				onSubmit={ () => {onCommit(this.state.comment); onClose();} }
				onReset={onClose}
				submitButtonText={commitButtonText}
				labledButtons={true}
				isValid={true}
				className='comment-commit-form'>
				<div className='commit-modal-text'>{descriptionText}</div>
				<Input
					data-test-id='commit-comment-text'
					onChange={comment => this.setState({comment: comment})}
					label={i18n('Enter Commit Comment:')}
					value={this.state.comment}
					type='textarea'/>
			</Form>
		);
	}
}

export default connect(null, mapActionToProps)(CommitCommentModal);
