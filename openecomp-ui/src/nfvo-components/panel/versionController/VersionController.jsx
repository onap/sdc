import React from 'react';
import classnames from 'classnames';
import i18n from 'nfvo-utils/i18n/i18n.js';

import Navbar from 'react-bootstrap/lib/Navbar.js';
import Nav from 'react-bootstrap/lib/Nav.js';
import ValidationInput from 'nfvo-components/input/validation/ValidationInput.jsx';
import {actionsEnum, statusEnum} from './VersionControllerConstants.js';


class VersionController extends React.Component {

	static propTypes = {
		version: React.PropTypes.string,
		viewableVersions: React.PropTypes.array,
		onVersionSwitching: React.PropTypes.func,
		isCheckedOut: React.PropTypes.bool.isRequired,
		status: React.PropTypes.string.isRequired,
		callVCAction: React.PropTypes.func,
		onSave: React.PropTypes.func,
		onClose: React.PropTypes.func,
		isFormDataValid: React.PropTypes.bool
	};

	render() {
		let {status, isCheckedOut, version = '', viewableVersions = [], onVersionSwitching, callVCAction, onSave, isFormDataValid, onClose} = this.props;
		let isCheckedIn = Boolean(status === statusEnum.CHECK_IN_STATUS);
		let isLatestVersion = Boolean(version === viewableVersions[viewableVersions.length - 1]);
		if (!isLatestVersion) {
			status = statusEnum.PREVIOUS_VERSION;
		}

		return (
			<div className='version-controller-bar'>
				<Navbar inverse className='navbar'>
					<Navbar.Collapse>
						<Nav className='items-in-left'>
							<div className='version-section'>
								<ValidationInput
									type='select'
									selectedEnum={version}
									onEnumChange={value => onVersionSwitching && onVersionSwitching(value)}>
									{viewableVersions && viewableVersions.map(viewVersion => {
										return (
											<option key={viewVersion} value={viewVersion}>{`V ${viewVersion}`}</option>
										);
									})
									}
									{!viewableVersions.includes(version) &&
									<option key={version} value={version}>{`V ${version}`}</option>}
								</ValidationInput>
							</div>
							<div className='vc-status'>
								<div className='onboarding-status-icon'></div>
								<div className='status-text'> {i18n('ONBOARDING')}
									<div className='status-text-dash'> -</div>
								</div>
								{this.renderStatus(status)}
							</div>
						</Nav>
						<Nav pullRight>
							<div className='items-in-right'>
								<div className='action-buttons'>
									{callVCAction &&
									<div className='version-control-buttons'>
										<div
											className={classnames('vc-nav-item-button button-submit', {'disabled': !isCheckedIn || !isLatestVersion})}
											onClick={() => this.submit(callVCAction)}>
											{i18n('Submit')}
										</div>
										<div
											className={classnames('vc-nav-item-button button-checkin-checkout', {'disabled': status === statusEnum.LOCK_STATUS || !isLatestVersion})}
											onClick={() => this.checkinCheckoutVersion(callVCAction)}>
											{`${isCheckedOut ? i18n('Check In') : i18n('Check Out')}`}
										</div>
										<div
											className={classnames('sprite-new revert-btn ng-scope ng-isolate-scope', {'disabled': !isCheckedOut || version === '0.1' || !isLatestVersion})}
											onClick={() => this.revertCheckout(callVCAction)}>
										</div>
									</div>
									}
									{onSave &&
									<div
										className={classnames('sprite-new save-btn ng-scope ng-isolate-scope', {'disabled': !isCheckedOut || !isFormDataValid || !isLatestVersion})}
										onClick={() => onSave()}>
									</div>
									}
								</div>
								<div className='vc-nav-item-close' onClick={() => onClose && onClose()}> X</div>
							</div>
						</Nav>
					</Navbar.Collapse>
				</Navbar>
			</div>
		);
	}

	renderStatus(status) {
		switch (status) {
			case statusEnum.CHECK_OUT_STATUS:
				return (
					<div className='checkout-status-icon'>
						<div className='catalog-tile-check-in-status sprite-new checkout-editable-status-icon'></div>
						<div className='status-text'> {i18n('CHECKED OUT')} </div>
					</div>
				);
			case statusEnum.LOCK_STATUS:
				return (
					<div className='status-text'> {i18n('LOCKED')} </div>
				);
			case statusEnum.CHECK_IN_STATUS:
				return (
					<div className='status-text'> {i18n('CHECKED IN')} </div>
				);
			case statusEnum.SUBMIT_STATUS:
				return (
					<div className='status-text'> {i18n('SUBMITTED')} </div>
				);
			default:
				return (
					<div className='status-text'> {i18n(status)} </div>
				);
		}
	}

	checkinCheckoutVersion(callVCAction) {
		if (this.props.isCheckedOut) {
			this.checkin(callVCAction);
		}
		else {
			this.checkout(callVCAction);
		}
	}

	checkin(callVCAction) {

		const action = actionsEnum.CHECK_IN;

		if (this.props.onSave) {
			this.props.onSave().then(()=>{
				 callVCAction(action);
			 });
		}else{
			callVCAction(action);
		}

	}

	checkout(callVCAction) {
		const action = actionsEnum.CHECK_OUT;
		callVCAction(action);
	}

	submit(callVCAction) {
		const action = actionsEnum.SUBMIT;
		callVCAction(action);
	}

	revertCheckout(callVCAction) {
		const action = actionsEnum.UNDO_CHECK_OUT;
		callVCAction(action);
	}
}

export default VersionController;
