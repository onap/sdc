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
import Form from 'nfvo-components/input/validation/Form.jsx';
import Select from 'nfvo-components/input/SelectInput.jsx';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

import {permissionTypes, changeOwnerMessage} from './PermissionsConstants.js';

export const askForRightsMsg = () => {
	return (
		<div>
			<p>{i18n('Send a Contributor rights reguest to Owner')}</p>
		</div>
	);
};


class Permissions extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			itemUsers: props.itemUsers,
			newOwnerId: '',
			showChangeOwner: false
		};
	}

	buildUserOptions() {
		let {users, owner} = this.props;
		return users.filter(user => user.userId !== owner.userId).map(item => {return {label: item.fullName, value: item.userId};});
	}

	render() {
		let {onCancel, owner} = this.props;
		let {newOwnerId} = this.state;
		return (
			<div className='manage-permissions-page'>
				<Form
					hasButtons={true}
					onSubmit={() => this.onsaveItemUsers()}
					onReset={() => onCancel() }
					labledButtons={true}>
					<div className='manage-permissions-title'>{i18n('Owner')}</div>
					<div className='owner-details'>
						<span>{owner.fullName}</span>
						<span className='change-owner' onClick={() => this.setState({showChangeOwner: !this.state.showChangeOwner})}>{i18n('Change Owner')}</span>
					</div>
					{this.state.showChangeOwner && <div className='change-owner-wrapper'>
						<div className='change-owner-title'>
							<span className='manage-permissions-title' data-test-id='change-owner'>{i18n('Change Owner')}</span>
							<OverlayTrigger placement='right' trigger='click' overlay={
								<Tooltip id='manage-permissions-owner-tooltip' className='manage-permissions-owner-tooltip'>{i18n(changeOwnerMessage)}</Tooltip> }>
								<SVGIcon name='questionMark' />
							</OverlayTrigger>
						</div>
						<Select
							data-test-id='selected-owner'
							value={newOwnerId}
							onChange={(item) => this.setState({newOwnerId: item ? item.value : ''})}
							options={this.buildUserOptions()} />
					</div>}
					<div className='manage-permissions-title'>{i18n('Contributors')}</div>
					<Select
						data-test-id='selected-contributors'
						value={this.state.itemUsers.map(item => item.userId)}
						className='options-input contributors-select'
						clearable={false}
						onMultiSelectChanged={(value) => {this.onChangeItemUsers({itemUsers: value});}}
						options={this.buildUserOptions()}
						multi/>
				</Form>
			</div>
		);
	}

	onChangeItemUsers({itemUsers}) {
		this.setState({
			itemUsers: itemUsers.map(contributor => {
				let contributorFromProps = this.props.itemUsers.find(user => user.userId === contributor.userId);
				return {
					userId: contributor.value,
					fullName: contributor.label,
					permission: contributorFromProps ? contributorFromProps.permission : permissionTypes.CONTRIBUTOR
				};
			})
		});
	}

	onsaveItemUsers() {
		let {itemUsers: newUsers, newOwnerId} = this.state;
		let {itemUsers: oldUsers, onSubmit, itemId, users} = this.props;
		let addedUsersIds = newUsers.filter(newUser => !oldUsers.map(oldUser => oldUser.userId).includes(newUser.userId))
			.map(user => user.userId);
		let removedUsersIds = oldUsers.filter(oldUser => !newUsers.map(newUser => newUser.userId).includes(oldUser.userId))
			.map(user => user.userId);
		onSubmit({itemId, addedUsersIds, removedUsersIds, allUsers: users, newOwnerId});
	}
}

export default Permissions;
