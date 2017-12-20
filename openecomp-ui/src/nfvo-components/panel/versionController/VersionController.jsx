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
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';

import {actionsEnum} from './VersionControllerConstants.js';
import ActionButtons from './components/ActionButtons.jsx';
import NotificationsView from 'sdc-app/onboarding/userNotifications/NotificationsView.jsx';


class VersionController extends React.Component {

	static propTypes = {
		version: PropTypes.object,
		viewableVersions: PropTypes.array,
		onVersionSwitching: PropTypes.func,
		callVCAction: PropTypes.func,
		onSave: PropTypes.func,
		onClose: PropTypes.func,
		isFormDataValid: PropTypes.bool,
		onOpenCommentCommitModal: PropTypes.func,
		isReadOnlyMode: PropTypes.bool
	};

	state = {
		showPermissions: false,
		showRevisions: false
	};

	render() {
		let {version = {},  viewableVersions = [], onVersionSwitching, onMoreVersionsClick, callVCAction, onSave, isReadOnlyMode, itemPermission,
				isFormDataValid, onClose, onManagePermissions, permissions = {},  userInfo, usersList, itemName, onOpenCommentCommitModal, onOpenRevisionsModal, isManual} = this.props;
		return (
			<div className='version-controller-bar'>
				<div className='vc-container'>
					<div className='version-status-container'>
						<VersionSelector
							viewableVersions={viewableVersions}
							version={version}
							onVersionSwitching={onVersionSwitching}
							onMoreVersionsClick={() => onMoreVersionsClick({itemName, users: usersList})}/>
					</div>
					<div className='save-submit-cancel-container'>
						<ActionButtons onSubmit={callVCAction ? () => this.submit(callVCAction, version) : undefined}
							onRevert={callVCAction ? () => this.revert(callVCAction, version) : undefined}
							onOpenRevisionsModal={onOpenRevisionsModal}
							onSave={onSave ? () => onSave() : undefined}
							permissions={permissions}
							userInfo={userInfo}
							onManagePermissions={onManagePermissions}
							showPermissions={this.state.showPermissions}
							onClosePermissions={()=>this.setState({showPermissions: false})}
							onClickPermissions={() => this.onClickPermissions()}
							onSync={callVCAction ? () => this.sync(callVCAction, version) : undefined}
							onOpenCommentCommitModal={onOpenCommentCommitModal}
							onCommit={callVCAction ? (comment) => this.commit(callVCAction, version, comment) : undefined}
							isFormDataValid={isFormDataValid}
							itemPermissions={itemPermission}
							isReadOnlyMode={isReadOnlyMode}
							isManual={isManual} />
						<div className='vc-separator'></div>
						<NotificationsView />
						{onClose && <div className='vc-nav-item-close' onClick={() => onClose()} data-test-id='vc-cancel-btn'> X</div>}
					</div>
				</div>
			</div>
		);
	}

	onClickPermissions() {
		let {onOpenPermissions, usersList} = this.props;
		let {showPermissions} = this.state;
		let promise = showPermissions ? Promise.resolve() : onOpenPermissions({users: usersList});
		promise.then(() => this.setState({showPermissions: !showPermissions}));
	}


	submit(callVCAction, version) {
		const action = actionsEnum.SUBMIT;
		callVCAction(action, version);
	}

	revert(callVCAction, version) {
		const action = actionsEnum.REVERT;
		callVCAction(action, version);
	}

	sync(callVCAction, version) {
		const action = actionsEnum.SYNC;
		callVCAction(action, version);
	}

	commit(callVCAction, version, comment) {
		const action = actionsEnum.COMMIT;
		callVCAction(action, version, comment);
	}

	permissions() {

	}
}

function VersionSelector(props) {
	let {version = {}, onMoreVersionsClick, viewableVersions = [], onVersionSwitching} = props;
	const includedVersions = viewableVersions.filter(ver => {return ver.id === version.id;});
	return (<div className='version-section-wrapper'>
		<select className='version-selector'
			onChange={ev => onVersionSwitching && onVersionSwitching(viewableVersions.find(version => version.id === ev.target.value))}
			value={version.id}
			data-test-id='vc-versions-select-box'>
				{viewableVersions && viewableVersions.map(viewVersion => {
					return (
						<option key={viewVersion.id} value={viewVersion.id} data-test-id='vc-version-option'>{`V ${viewVersion.name} ${viewVersion.status}`}</option>
					);
				})
				}
				{!includedVersions.length &&
				<option key={version.id} value={version.id} data-test-id='vc-selected-version-option'>{`V ${version.name} ${version.status}`}</option>}
		</select>
		<span onClick={onMoreVersionsClick} className='version-selector-more-versions' data-test-id='vc-versions-page-link'>{i18n('Versions Page')}</span>
	</div>);
}

export default VersionController;
