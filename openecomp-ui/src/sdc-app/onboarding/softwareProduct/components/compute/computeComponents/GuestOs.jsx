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


const GuestOs = ({qgenericFieldInfo, dataMap, onQDataChanged}) => {
	return(
		<div>
			<GridSection title={i18n('Guest OS')} hasLastColSet>
				<GridItem>
					<div className='vertical-flex'>
						<label key='label' className='control-label'>{i18n('OS Bit Size')}</label>
						<div className='radio-options-content-row'>
							{qgenericFieldInfo['compute/guestOS/bitSize'].enum.map(bitSize => (
							<Input
								data-test-id='guestOS-bitSize'
								type='radio'
								key={bitSize.enum}
								name={'compute/guestOS/bitSize'}
								className='radio-field'
								value={bitSize.enum}
								label={bitSize.title}
								onChange={(bit) => onQDataChanged({'compute/guestOS/bitSize' :  Number(bit)})}
								isValid={qgenericFieldInfo['compute/guestOS/bitSize'].isValid}
								errorText={qgenericFieldInfo['compute/guestOS/bitSize'].errorText}
								checked={dataMap['compute/guestOS/bitSize'] === bitSize.enum} /> )) }
						</div>
					</div>
				</GridItem>
				<GridItem colSpan={2}/>
				<GridItem colSpan={2}>
					<Input
						data-test-id='guestOS-name'
						label={i18n('Guest OS')}
						type='textarea'
						onChange={(tools) => onQDataChanged({'compute/guestOS/name' : tools})}
						isValid={qgenericFieldInfo['compute/guestOS/name'].isValid}
						errorText={qgenericFieldInfo['compute/guestOS/name'].errorText}
						value={dataMap['compute/guestOS/name']} />
				</GridItem>
				<GridItem colSpan={2} lastColInRow>
					<Input
						data-test-id='guestOS-tools'
						type='textarea'
						label={i18n('Guest OS Tools:')}
						onChange={(tools) => onQDataChanged({'compute/guestOS/tools' : tools})}
						isValid={qgenericFieldInfo['compute/guestOS/tools'].isValid}
						errorText={qgenericFieldInfo['compute/guestOS/tools'].errorText}
						value={dataMap['compute/guestOS/tools']} />
				</GridItem>
			</GridSection>


		</div>
	);
};

export default GuestOs;
