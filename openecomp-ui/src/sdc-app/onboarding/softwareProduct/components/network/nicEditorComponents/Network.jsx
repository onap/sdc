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
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import { networkTypes } from '../SoftwareProductComponentsNetworkConstants.js';

const Network = ({networkValues, networkType, networkDescription, onDataChanged, isReadOnlyMode}) => {
	const isExternal = networkType === networkTypes.EXTERNAL;
	return (
		<GridSection title={i18n('Network')} hasLastColSet>
			<GridItem>
				<Input
					label={i18n('Internal')}
					disabled
					checked={!isExternal}
					data-test-id='nic-internal'
					className='network-radio disabled'
					type='radio' />
			</GridItem>
			<GridItem>
				<Input
					label={i18n('External')}
					disabled
					checked={isExternal}
					data-test-id='nic-external'
					className='network-radio disabled'
					type='radio' />
			</GridItem>
			<GridItem colSpan={2} lastColInRow>
			{
				isExternal ?
					<Input
						label={i18n('Network Description')}
						value={networkDescription}
						data-test-id='nic-network-description'
						onChange={networkDescription => onDataChanged({networkDescription})}
						disabled={isReadOnlyMode}
						type='text' />
				:
					<Input
						label={i18n('Network')}
						data-test-id='nic-network'
						type='select'
						className='input-options-select'
						groupClassName='bootstrap-input-options'
						disabled={true}>
						{networkValues.map(val => <option key={val.enum} value={val.enum}>{val.title}</option>)}
					</Input>
			}
			</GridItem>
		</GridSection>
	);
};

Network.PropTypes = {
	networkValues: PropTypes.array
};

export default  Network;
