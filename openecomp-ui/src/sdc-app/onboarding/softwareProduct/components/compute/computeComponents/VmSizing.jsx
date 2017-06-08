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
		<GridSection title={i18n('VM Sizing')}>
			<GridItem>
				<Input
					data-test-id='numOfCPUs'
					type='number'
					label={i18n('Number of CPUs')}
					onChange={(tools) => onQDataChanged({'compute/vmSizing/numOfCPUs' : tools})}
					isValid={qgenericFieldInfo['compute/vmSizing/numOfCPUs'].isValid}
					errorText={qgenericFieldInfo['compute/vmSizing/numOfCPUs'].errorText}
					value={dataMap['compute/vmSizing/numOfCPUs']} />
			</GridItem>
			<GridItem>
				<Input
					data-test-id='fileSystemSizeGB'
					type='number'
					label={i18n('File System Size (GB)')}
					onChange={(tools) => onQDataChanged({'compute/vmSizing/fileSystemSizeGB' : tools})}
					isValid={qgenericFieldInfo['compute/vmSizing/fileSystemSizeGB'].isValid}
					errorText={qgenericFieldInfo['compute/vmSizing/fileSystemSizeGB'].errorText}
					value={dataMap['compute/vmSizing/fileSystemSizeGB']} />
			</GridItem>
			<GridItem>
				<Input
					data-test-id='persistentStorageVolumeSize'
					type='number'
					label={i18n('Persistent Storage/Volume Size (GB)')}
					onChange={(tools) => onQDataChanged({'compute/vmSizing/persistentStorageVolumeSize' : tools})}
					isValid={qgenericFieldInfo['compute/vmSizing/persistentStorageVolumeSize'].isValid}
					errorText={qgenericFieldInfo['compute/vmSizing/persistentStorageVolumeSize'].errorText}
					value={dataMap['compute/vmSizing/persistentStorageVolumeSize']} />
			</GridItem>
			<GridItem>
				<Input
					data-test-id='IOOperationsPerSec'
					type='number'
					label={i18n('I/O Operations (per second)')}
					onChange={(tools) => onQDataChanged({'compute/vmSizing/IOOperationsPerSec' : tools})}
					isValid={qgenericFieldInfo['compute/vmSizing/IOOperationsPerSec'].isValid}
					errorText={qgenericFieldInfo['compute/vmSizing/IOOperationsPerSec'].errorText}
					value={dataMap['compute/vmSizing/IOOperationsPerSec']} />
			</GridItem>
		</GridSection>
	);
};

export default VmSizing;
