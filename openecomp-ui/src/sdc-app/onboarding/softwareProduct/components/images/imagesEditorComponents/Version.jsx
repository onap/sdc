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
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import {imageCustomValidations} from '../ImageValidations.js';


const Version = ({isManual, dataMap, qgenericFieldInfo, onQDataChanged}) => {
	return(
		<GridItem colSpan={1} lastColInRow>
			<Input
				disabled={!isManual}
				data-test-id='image-version'
				type='text'
				className='image-version'
				label={i18n('Version')}
				isRequired={true}
				onChange={(version) => onQDataChanged({'version' : version}, {'version' : imageCustomValidations['version']})}
				isValid={qgenericFieldInfo['version'].isValid}
				errorText={qgenericFieldInfo['version'].errorText}
				value={dataMap['version']}/>
		</GridItem>
	);
};
export default Version;

