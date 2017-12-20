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
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';

const Acceptable = ({qgenericFieldInfo, dataMap, onQDataChanged}) => {
	return(
		<GridSection hasLastColSet>
			<GridItem colSpan={3}>
				<div className='part-title-small packets'>{i18n('Acceptable Jitter')}</div>
			</GridItem>
			<GridItem lastColInRow>
				<div className='part-title-small bytes'>{i18n('Allow Packet Loss')}</div>
			</GridItem>
			<GridItem>
				<Input
					label={i18n('Mean')}
					type='number'
					data-test-id='acceptableJitter-mean'
					isValid={qgenericFieldInfo['sizing/acceptableJitter/mean'].isValid}
					errorText={qgenericFieldInfo['sizing/acceptableJitter/mean'].errorText}
					value={dataMap['sizing/acceptableJitter/mean']}
					onChange={val => onQDataChanged({'sizing/acceptableJitter/mean' : val})} />
			</GridItem>
			<GridItem>
				<Input
					label={i18n('Max')}
					type='number'
					data-test-id='acceptableJitter-max'
					isValid={qgenericFieldInfo['sizing/acceptableJitter/max'].isValid}
					errorText={qgenericFieldInfo['sizing/acceptableJitter/max'].errorText}
					value={dataMap['sizing/acceptableJitter/max']}
					onChange={val => onQDataChanged({'sizing/acceptableJitter/max' : val})} />
			</GridItem>
			<GridItem>
				<Input
					label={i18n('Var')}
					type='number'
					data-test-id='acceptableJitter-variable'
					isValid={qgenericFieldInfo['sizing/acceptableJitter/variable'].isValid}
					errorText={qgenericFieldInfo['sizing/acceptableJitter/variable'].errorText}
					value={dataMap['sizing/acceptableJitter/variable']}
					onChange={val => onQDataChanged({'sizing/acceptableJitter/variable' : val})} />
			</GridItem>
			<GridItem lastColInRow>
				<Input
					label={i18n('In Percent')}
					type='number'
					data-test-id='acceptableJitter-acceptablePacketLoss'
					isValid={qgenericFieldInfo['sizing/acceptablePacketLoss'].isValid}
					errorText={qgenericFieldInfo['sizing/acceptablePacketLoss'].errorText}
					value={dataMap['sizing/acceptablePacketLoss']}
					onChange={val => onQDataChanged({'sizing/acceptablePacketLoss' : val})} />
			</GridItem>
		</GridSection>
	);
};

export default Acceptable;
