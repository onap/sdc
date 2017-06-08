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

function ItemInfo({name, description, children}) {
	return (
		<div className='list-item-section vlm-item-info'>
			<div className='vlm-list-item-title'>
				<div className='item-name' data-test-id='vlm-list-item-name'>{name}</div>
				{children}
			</div>
			<div className='vlm-list-item-description'>{description}</div>
		</div>
	);
}

ItemInfo.propTypes = {
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	children: React.PropTypes.oneOfType([
		React.PropTypes.arrayOf(React.PropTypes.node),
		React.PropTypes.node
	])
};

export default ItemInfo;
