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
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';

const maxContributors = 6;

function extraUsersTooltip (extraUsers) {
	return (
		<Tooltip className='extra-users-tooltip' id='extra-users-tooltip-id'>
			{extraUsers.map(extraUser => <div key={extraUser.userId} className='extra-user'>{extraUser.fullName}</div>)}
		</Tooltip>
	);
}

const User = ({user, isCurrentUser, dataTestId}) => (
	<SVGIcon className={`user-view ${isCurrentUser ? 'current-user' : ''}`} name='user' label={user.fullName} labelPosition='right' color='primary'
		data-test-id={dataTestId}/>
);

const Owner = ({owner, isCurrentUser}) => (
	<div className='owner-view'>
		<div className='permissions-view-title'>{i18n('Owner')}</div>
		<User user={owner} isCurrentUser={isCurrentUser} dataTestId='owner'/>
	</div>
);

const Contributors = ({contributors, owner, currentUser, onManagePermissions, isManual}) => {
	let extraUsers = contributors.length - maxContributors;
	return (
		<div className='contributors-view'>
			<div className='permissions-view-title'>{i18n('Contributors')}</div>
			{contributors.slice(0, maxContributors).map(contributor =>
				<User key={contributor.userId} user={contributor} isCurrentUser={contributor.userId === currentUser.userId} dataTestId='contributor'/>
			)}
			{extraUsers > 0 &&
				<OverlayTrigger placement='bottom' overlay={extraUsersTooltip(contributors.slice(maxContributors))}>
					<div className='extra-contributors'>{`+${extraUsers}`}</div>
				</OverlayTrigger>
			}
			{currentUser.userId === owner.userId && !isManual &&
				<span
					className='manage-permissions'
					onClick={onManagePermissions}
					data-test-id='versions-page-manage-permissions'>
					{i18n('Manage Permissions')}
				</span>
			}
	</div>
	);
};

const PermissionsView = ({owner, contributors, currentUser = {}, onManagePermissions, isManual}) => (
	<div className='versions-page-permissions-view-wrapper'>
		<div className='permissions-view-wrapper-title'>{i18n('Permissions')}</div>
		<div className='permissions-view-content'>
			<div className='permissions-view'>
				<Owner owner={owner} isCurrentUser={owner.userId === currentUser.userId} />
				<Contributors owner={owner} contributors={contributors} currentUser={currentUser} onManagePermissions={onManagePermissions} isManual={isManual}/>
			</div>
		</div>
	</div>
);

export default PermissionsView;
