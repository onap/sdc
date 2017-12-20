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

function AdditionalDataCol({children}) {
	return (
		<div className='list-item-section list-item-additional-data-col'>
			<div className='additional-data-col-border'></div>
			<div className='additional-data'>
				{children}
			</div>
		</div>
	);
}

AdditionalDataCol.propTypes = {
	children: PropTypes.oneOfType([
		PropTypes.arrayOf(PropTypes.node),
		PropTypes.node
	])
};

function AdditionalDataElement({className, name, value}) {
	return (
		<div className={className}>
			<span className='additional-data-name'>{name}: </span>
			<span className='additional-data-value'>{value}</span>
		</div>
	);
}

AdditionalDataElement.propTypes = {
	name: PropTypes.string,
	value: PropTypes.string,
	className: PropTypes.string
};

export {AdditionalDataCol, AdditionalDataElement};
