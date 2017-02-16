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
import FontAwesome from 'react-fontawesome';

class ValidationButtons extends React.Component {

	static propTypes = {
		labledButtons: React.PropTypes.bool.isRequired,
		isReadOnlyMode: React.PropTypes.bool
	};

	state = {
		isValid: this.props.formValid
	};

	render() {
		var submitBtn = this.props.labledButtons ? i18n('Save') : <FontAwesome className='check' name='check'/>;
		var closeBtn = this.props.labledButtons ? i18n('Cancel') : <FontAwesome className='close' name='close'/>;
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
