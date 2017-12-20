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
import React, {Component} from 'react';
import PropTypes from 'prop-types';
import enhanceWithClickOutside from 'react-click-outside';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import Overlay from 'nfvo-components/overlay/Overlay.jsx';
import Permissions from './Permissions.jsx';

class ClickOutsideWrapper extends Component {
	handleClickOutside() {
		this.props.onClose();
	}
	render() {
		return <div>{this.props.children}</div>;
	}
}

const EnhancedClickOutsideWrapper = enhanceWithClickOutside(ClickOutsideWrapper);

const VCButton = ({name, tooltipText, disabled, onClick, dataTestId}) => {
	let onClickAction = disabled ? ()=>{} : onClick;
	return (
		<div className={`action-button-wrapper ${disabled ? 'disabled' : 'clickable'}`} onClick={onClickAction}>
			<div className='action-buttons-svg'>
				<SVGIcon label={tooltipText} labelPosition='bottom' labelClassName='action-button-label'
					 data-test-id={dataTestId} name={name} disabled={disabled}/>
			</div>
		</div>
	);
};

const Separator = () => (<div className='vc-separator'></div>);

const SubmitButton = ({onClick, disabled}) => (
	<div onClick={()=>onClick()} data-test-id='vc-submit-btn' className={`vc-submit-button ${disabled ? 'disabled' : ''}`}>
		<SVGIcon name='check' iconClassName='vc-v-submit' disabled={disabled} />
		{i18n('Submit')}
	</div>
);


const ActionButtons = ({isReadOnlyMode, onSubmit, onRevert, onSave, isFormDataValid, onClickPermissions, onSync, onCommit,
	onOpenCommentCommitModal, showPermissions, onClosePermissions, permissions, onManagePermissions, userInfo, onOpenRevisionsModal, isManual,
	itemPermissions: {isCertified, isCollaborator, isDirty, isOutOfSync, isUpToDate}}) => (
	<div className='action-buttons'>
		<EnhancedClickOutsideWrapper onClose={onClosePermissions}>
			<VCButton disabled={isManual} dataTestId='vc-permission-btn' onClick={onClickPermissions}
				name='version-controller-permissions' tooltipText={i18n('Permissons')} />
			{showPermissions &&
				<Overlay>
					<Permissions userInfo={userInfo} onManagePermissions={onManagePermissions} permissions={permissions} onClosePermissions={onClosePermissions}/>
				</Overlay>
			}
		</EnhancedClickOutsideWrapper>
		{isCollaborator && <div className='collaborator-action-buttons'>
			<Separator />
			{onSave && <div className='vc-save-section'>
					<VCButton dataTestId='vc-save-btn' onClick={() => onSave()}
						name='version-controller-save'  tooltipText={i18n('Save')} disabled={isReadOnlyMode || !isFormDataValid} />
					<Separator />
				</div>
			}
			<VCButton dataTestId='vc-sync-btn' onClick={onSync}
				name='version-controller-sync' tooltipText={i18n('Sync')} disabled={!isCollaborator || isUpToDate || isCertified} />
			<VCButton dataTestId='vc-commit-btn' onClick={() => onOpenCommentCommitModal({onCommit, title: i18n('Commit')})}
				name='version-controller-commit' tooltipText={i18n('Share')} disabled={isReadOnlyMode || !isDirty || isOutOfSync} />
			{onRevert &&
				<VCButton dataTestId='vc-revert-btn' onClick={onOpenRevisionsModal}
					name='version-controller-revert' tooltipText={i18n('Revert')} disabled={isReadOnlyMode || isOutOfSync} />
			}
			{onSubmit && (permissions.owner && permissions.owner.userId === userInfo.userId) &&
				<div className='vc-submit-section'>
					<Separator />
					<SubmitButton onClick={onSubmit}
						disabled={isReadOnlyMode || isOutOfSync || !isUpToDate || isCertified} />
				</div>
			}
		</div>}
	</div>
);

ActionButtons.propTypes = {
	version: PropTypes.object,
	onSubmit: PropTypes.func,
	onRevert: PropTypes.func,
	onSave: PropTypes.func,
	isLatestVersion: PropTypes.bool,
	isCheckedIn: PropTypes.bool,
	isCheckedOut: PropTypes.bool,
	isFormDataValid: PropTypes.bool,
	isReadOnlyMode: PropTypes.bool
};

export default ActionButtons;
