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

const NameAndPurpose = ({onDataChanged, genericFieldInfo, isReadOnlyMode, name, description, isManual}) => {

	return (
		<GridSection hastLastColSet>
			<GridItem colSpan={2}>
				<Input
					label={i18n('Name')}
					value={name}
					data-test-id='nic-name'
					disabled={!isManual}
					isRequired={true}
					onChange={name => onDataChanged({name})}
					isValid={genericFieldInfo['name'].isValid}
					errorText={genericFieldInfo['name'].errorText}
					type='text' />
			</GridItem>
			<GridItem colSpan={2} lastColInRow>
				<Input
					label={i18n('Purpose of NIC')}
					value={description}
					data-test-id='nic-description'
					onChange={description => onDataChanged({description})}
					disabled={isReadOnlyMode}
					type='textarea' />
			</GridItem>
		</GridSection>
	);
};

NameAndPurpose.PropTypes = {
	name: PropTypes.string,
	description: PropTypes.array,
	onDataChanged: PropTypes.func,
	isReadOnlyMode: PropTypes.bool,
};

export default NameAndPurpose;
