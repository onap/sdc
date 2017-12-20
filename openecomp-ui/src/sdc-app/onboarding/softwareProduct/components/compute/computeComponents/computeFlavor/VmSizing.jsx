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
const VmSizing = ({qgenericFieldInfo, dataMap, onQDataChanged}) => {
	return(
		<GridSection title={i18n('VM Sizing')} hasLastColSet>
			<GridItem>
				<Input
					data-test-id='numOfCPUs'
					type='number'
					label={i18n('Number of CPUs')}
					onChange={(tools) => onQDataChanged({'vmSizing/numOfCPUs' : tools})}
					isValid={qgenericFieldInfo['vmSizing/numOfCPUs'].isValid}
					errorText={qgenericFieldInfo['vmSizing/numOfCPUs'].errorText}
					value={dataMap['vmSizing/numOfCPUs']} />
			</GridItem>
			<GridItem>
				<Input
					data-test-id='fileSystemSizeGB'
					type='number'
					label={i18n('File System Size (GB)')}
					onChange={(tools) => onQDataChanged({'vmSizing/fileSystemSizeGB' : tools})}
					isValid={qgenericFieldInfo['vmSizing/fileSystemSizeGB'].isValid}
					errorText={qgenericFieldInfo['vmSizing/fileSystemSizeGB'].errorText}
					value={dataMap['vmSizing/fileSystemSizeGB']} />
			</GridItem>
			<GridItem>
				<Input
					data-test-id='persistentStorageVolumeSize'
					type='number'
					label={i18n('Persistent Storage/Volume Size (GB)')}
					onChange={(tools) => onQDataChanged({'vmSizing/persistentStorageVolumeSize' : tools})}
					isValid={qgenericFieldInfo['vmSizing/persistentStorageVolumeSize'].isValid}
					errorText={qgenericFieldInfo['vmSizing/persistentStorageVolumeSize'].errorText}
					value={dataMap['vmSizing/persistentStorageVolumeSize']} />
			</GridItem>
			<GridItem lastColInRow>
				<Input
					data-test-id='ioOperationsPerSec'
					type='number'
					label={i18n('I/O Operations (per second)')}
					onChange={(tools) => onQDataChanged({'vmSizing/ioOperationsPerSec' : tools})}
					isValid={qgenericFieldInfo['vmSizing/ioOperationsPerSec'].isValid}
					errorText={qgenericFieldInfo['vmSizing/ioOperationsPerSec'].errorText}
					value={dataMap['vmSizing/ioOperationsPerSec']} />
			</GridItem>
			<GridItem>
				<Input
					data-test-id='numOfVMs-cpuOverSubscriptionRatio'
					label={i18n('CPU Oversubscription Ratio')}
					type='select'
					groupClassName='bootstrap-input-options'
					className='input-options-select'
					isValid={qgenericFieldInfo['vmSizing/cpuOverSubscriptionRatio'].isValid}
					errorText={qgenericFieldInfo['vmSizing/cpuOverSubscriptionRatio'].errorText}
					value={dataMap['vmSizing/cpuOverSubscriptionRatio']}
					onChange={(e) => {
						const selectedIndex = e.target.selectedIndex;
						const val = e.target.options[selectedIndex].value;
						onQDataChanged({'vmSizing/cpuOverSubscriptionRatio' : val});}
					}>
					<option key='placeholder' value=''>{i18n('Select...')}</option>
					{qgenericFieldInfo['vmSizing/cpuOverSubscriptionRatio'].enum.map(cpuOSR => <option value={cpuOSR.enum} key={cpuOSR.enum}>{cpuOSR.title}</option>)}
				</Input>
			</GridItem>
			<GridItem>
				<Input
					data-test-id='numOfVMs-memoryRAM'
					type='select'
					label={i18n('Memory - RAM')}
					groupClassName='bootstrap-input-options'
					className='input-options-select'
					isValid={qgenericFieldInfo['vmSizing/memoryRAM'].isValid}
					errorText={qgenericFieldInfo['vmSizing/memoryRAM'].errorText}
					value={dataMap['vmSizing/memoryRAM']}
					onChange={(e) => {
						const selectedIndex = e.target.selectedIndex;
						const val = e.target.options[selectedIndex].value;
						onQDataChanged({'vmSizing/memoryRAM' : val});}
					}>
					<option key='placeholder' value=''>{i18n('Select...')}</option>
					{qgenericFieldInfo['vmSizing/memoryRAM'].enum.map(mRAM => <option value={mRAM.enum} key={mRAM.enum}>{mRAM.title}</option>)}
				</Input>
			</GridItem>
		</GridSection>
	);
};

export default VmSizing;
