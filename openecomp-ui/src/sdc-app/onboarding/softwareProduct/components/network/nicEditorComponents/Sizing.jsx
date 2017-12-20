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

const Sizing = ({qgenericFieldInfo, dataMap, onQDataChanged}) => {
	return(
		<GridSection title={i18n('Sizing')} hasLastColSet>
			<GridItem colSpan={4} lastColInRow>
				<Input
					label={i18n('Describe Quality of Service')}
					type='textarea'
					data-test-id='sizing-describeQualityOfService'
					isValid={qgenericFieldInfo['sizing/describeQualityOfService'].isValid}
					errorText={qgenericFieldInfo['sizing/describeQualityOfService'].errorText}
					value={dataMap['sizing/describeQualityOfService']}
					onChange={val => onQDataChanged({'sizing/describeQualityOfService' : val}) }/>
			</GridItem>
		</GridSection>
	);
};

export default Sizing;
