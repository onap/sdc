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
import InputOptions from 'nfvo-components/input/validation/InputOptions.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';

const Protocols = ({protocols, qgenericFieldInfo, dataMap, onQDataChanged}) => {
	return (
		<GridSection title={i18n('Protocols')} hasLastColSet>
				<GridItem colSpan={2}>
					<InputOptions
						data-test-id='nic-protocols'
						label={i18n('Protocols')}
						type='select'
						isMultiSelect={true}
						isValid={qgenericFieldInfo['protocols/protocols'].isValid}
						errorText={qgenericFieldInfo['protocols/protocols'].errorText}
						onInputChange={()=>{}}
						onEnumChange={protocols => {
							onQDataChanged({'protocols/protocols' : protocols});}
						}
						multiSelectedEnum={dataMap['protocols/protocols']}
						clearable={false}
						values={qgenericFieldInfo['protocols/protocols'].enum}/>
				</GridItem>
				<GridItem colSpan={2} lastColInRow>
					<Input
						data-test-id='nic-protocolWithHighestTrafficProfile'
						label={i18n('Protocol with Highest Traffic Profile')}
						type='select'
						groupClassName='bootstrap-input-options'
						className='input-options-select'
						isValid={qgenericFieldInfo['protocols/protocolWithHighestTrafficProfile'].isValid}
						errorText={qgenericFieldInfo['protocols/protocolWithHighestTrafficProfile'].errorText}
						value={dataMap['protocols/protocolWithHighestTrafficProfile']}
						onChange={(e) => {
							const selectedIndex = e.target.selectedIndex;
							const val = e.target.options[selectedIndex].value;
							onQDataChanged({'protocols/protocolWithHighestTrafficProfile' : val});}
						}>
						{(protocols.length === 0) &&
							<option key={'You must select protocols first...'} value=''>{i18n('You must select protocols first...')}</option>
						}
						{protocols.map(protocol => <option key={protocol} value={protocol}>{protocol}</option>)}
					</Input>
				</GridItem>
		</GridSection>
	);
};

Protocols.PropTypes = {
	protocols: PropTypes.array,
	onQDataChanged:  PropTypes.function,
	dataMap: PropTypes.object,
	qgenericFieldInfo: PropTypes.object
};

export default Protocols;
