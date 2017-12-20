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

const Contributor = ({name, role, id, userInfo}) => {

	const selected = id === userInfo.userId ? 'selected' : '';

	return(
		<div className='contributor'>
			<div className='contributor-content'>
				<div className={`contributor-icon-circle ${selected}`}>
					<div className={`contributer-icon ${selected}`}>
						<SVGIcon name='user'/>
					</div>
				</div>
				<div className='contributer-info'>
					<div className='contributer-name'>{name}</div>
					<div className='contributer-role'><p>{role}</p></div>
				</div>
			</div>
		</div>
	);
};

const Permissions = ({permissions: {owner, contributors}, onManagePermissions, userInfo, onClosePermissions}) => {

	return (
		<div className='permissions-overlay'>
			<div className='permissions-overlay-header'>
					<h4 className='permissions-overlay-header-title'>{i18n('PERMISSIONS')}</h4>
				</div>
				<div className='permissions-overlay-content'>
					<Contributor userInfo={userInfo} id={owner.userId} key={owner.fullName} name={owner.fullName} role={owner.role}/>
					{contributors.map(item => item.userId !== owner.userId && <Contributor userInfo={userInfo} id={item.userId} key={item.fullName} name={item.fullName} role={item.role}/>)}
				</div>
				<div className='permissions-overlay-footer'>
				{
				 owner.userId === userInfo.userId &&
					<div onClick={() => { onClosePermissions(); onManagePermissions(); }} className='manage-permissions-btn'>
						{i18n('Manage Permissions')}
					</div>
				}
				</div>
		</div>
	);
};

export default Permissions;
