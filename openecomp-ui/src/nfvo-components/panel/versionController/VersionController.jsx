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
import i18n from 'nfvo-utils/i18n/i18n.js';

import {actionsEnum, statusEnum, statusBarTextMap } from './VersionControllerConstants.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';


class VersionController extends React.Component {

	static propTypes = {
		version: React.PropTypes.object,
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
		let {status, isCheckedOut, version = {},  viewableVersions = [], onVersionSwitching, callVCAction, onSave, isFormDataValid, onClose} = this.props;
		let isCheckedIn = Boolean(status === statusEnum.CHECK_IN_STATUS);
		let isLatestVersion = Boolean(version.id === viewableVersions[viewableVersions.length - 1].id);
		if (!isLatestVersion) {
			status = statusEnum.PREVIOUS_VERSION;
		}
		return (
			<div className='version-controller-bar'>
				<div className='vc-container'>
					<div className='version-status-container'>
						<VersionSelector viewableVersions={viewableVersions} version={version} onVersionSwitching={onVersionSwitching} />
						<StatusBarUpdates status={status}/>
					</div>
					<div className='save-submit-cancel-container'>
						<ActionButtons onSubmit={callVCAction ? () => this.submit(callVCAction, version) : undefined}
							onRevert={callVCAction ? () => this.revertCheckout(callVCAction, version) : undefined}
							status={status}
							onCheckinCheckout={callVCAction ? () => this.checkinCheckoutVersion(callVCAction, version) : undefined}
							onSave={onSave ? () => onSave() : undefined}
							isLatestVersion={isLatestVersion}
							isCheckedOut={isCheckedOut}
							isCheckedIn={isCheckedIn} isFormDataValid={isFormDataValid} version={version}/>
						{onClose && <div className='vc-nav-item-close' onClick={() => onClose()} data-test-id='vc-cancel-btn'> X</div>}
					</div>
				</div>
			</div>
		);
	}

	submit(callVCAction, version) {
		const action = actionsEnum.SUBMIT;
		callVCAction(action, version);
	}

	revertCheckout(callVCAction, version) {
		const action = actionsEnum.UNDO_CHECK_OUT;
		callVCAction(action, version);
	}

	checkinCheckoutVersion(callVCAction, version) {
		if (this.props.isCheckedOut) {
			this.checkin(callVCAction, version);
		}
		else {
			this.checkout(callVCAction, version);
		}
	}
	checkin(callVCAction, version) {
		const action = actionsEnum.CHECK_IN;
		if (this.props.onSave) {
			this.props.onSave().then(()=>{
				callVCAction(action, version);
			});
		}else{
			callVCAction(action, version);
		}

	}
	checkout(callVCAction, version) {
		const action = actionsEnum.CHECK_OUT;
		callVCAction(action, version);
	}
}

class ActionButtons extends React.Component {
	static propTypes = {
		version: React.PropTypes.object,
		onSubmit: React.PropTypes.func,
		onRevert: React.PropTypes.func,
		onSave: React.PropTypes.func,
		isLatestVersion: React.PropTypes.bool,
		isCheckedIn: React.PropTypes.bool,
		isCheckedOut: React.PropTypes.bool,
		isFormDataValid: React.PropTypes.bool
	};
	render() {
		const {onSubmit, onRevert, onSave, isLatestVersion, isCheckedIn, isCheckedOut, isFormDataValid, version, status, onCheckinCheckout} = this.props;
		const [checkinBtnIconSvg, checkinCheckoutBtnTitle] = status === statusEnum.CHECK_OUT_STATUS ?
			['versionControllerLockOpen', i18n('Check In')] :
			['versionControllerLockClosed', i18n('Check Out')];
		const disabled = (isLatestVersion && onCheckinCheckout && status !== statusEnum.LOCK_STATUS) ? false : true;
		return (
			<div className='action-buttons'>
				<VCButton dataTestId='vc-checkout-btn' onClick={onCheckinCheckout} isDisabled={disabled}
					name={checkinBtnIconSvg} tooltipText={checkinCheckoutBtnTitle}/>
				{onSubmit && onRevert &&
					<div className='version-control-buttons'>
						<VCButton dataTestId='vc-submit-btn' onClick={onSubmit}  isDisabled={!isCheckedIn || !isLatestVersion}
							name='versionControllerSubmit' tooltipText={i18n('Submit')}/>
						<VCButton dataTestId='vc-revert-btn' onClick={onRevert} isDisabled={!isCheckedOut || version.label === '0.1' || !isLatestVersion}
							name='versionControllerRevert' tooltipText={i18n('Revert')}/>
					</div>
				}
				{onSave &&
					<VCButton dataTestId='vc-save-btn' onClick={() => onSave()} isDisabled={!isCheckedOut || !isFormDataValid || !isLatestVersion}
						name='versionControllerSave'  tooltipText={i18n('Save')}/>
				}
			</div>
		);
	}
}

function StatusBarUpdates({status}) {
	return (
		<div className='vc-status'>
			<span className='status-text'>{i18n(statusBarTextMap[status])}</span>
		</div>
	);
}

function VCButton({name, tooltipText, isDisabled, onClick, dataTestId}) {
	let onClickAction = isDisabled ? ()=>{} : onClick;
	let disabled = isDisabled ? 'disabled' : '';

	return (
		<OverlayTrigger placement='top' overlay={<Tooltip id='vc-tooltip'>{tooltipText}</Tooltip>}>
			<div disabled={disabled} className='action-buttons-svg'>
				<SVGIcon data-test-id={dataTestId} disabled={isDisabled} onClick={onClickAction ? onClickAction : undefined} name={name}/>
			</div>
		</OverlayTrigger>
	);
}

function VersionSelector(props) {
	let {version = {}, viewableVersions = [], onVersionSwitching} = props;
	const includedVersions = viewableVersions.filter(ver => {return ver.id === version.id;});
	return (<div className='version-section-wrapper'>
		<select className='version-selector'
			onChange={ev => onVersionSwitching && onVersionSwitching({id: ev.target.value, label: ev.target.value})}
			value={version.label}>
				{viewableVersions && viewableVersions.map(viewVersion => {
					return (
						<option key={viewVersion.id} value={viewVersion.id} data-test-id='vc-version-option'>{`V ${viewVersion.label}`}</option>
					);
				})
				}
				{!includedVersions.length &&
				<option key={version.id} value={version.id}>{`V ${version.label}`}</option>}
		</select>
	</div>);
}

export default VersionController;
