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
import {forms} from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';

import Format from './Format.jsx';
import Version from './Version.jsx';

const FileDetails = ({editingMode, fileName, onDataChanged, isManual, dataMap, onQDataChanged, genericFieldInfo, qgenericFieldInfo}) => {
	let fileNameCols = (editingMode) ? 3 : 4;
	return(
		<GridSection hasLastColSset>
			<GridItem colSpan={fileNameCols} lastColInRow={!editingMode}>
				<Input
					disabled={!isManual}
					onChange={fileName => onDataChanged({fileName}, forms.IMAGE_EDIT_FORM)}
					label={i18n('Image Name')}
					data-test-id='image-filename'
					value={fileName}
					isValid={genericFieldInfo.fileName.isValid}
					errorText={genericFieldInfo.fileName.errorText}
					isRequired={true}
					type='text'
					className='image-filename'/>
			</GridItem>
			{!editingMode && <div className='note-text'>{i18n('After image creation you must go to Edit Image and add File Version')}</div>}
			{editingMode && <Version isManual={isManual} dataMap={dataMap} qgenericFieldInfo={qgenericFieldInfo} onQDataChanged={onQDataChanged}/>}
			{editingMode && <Format isManual={isManual} qgenericFieldInfo={qgenericFieldInfo} dataMap={dataMap} onQDataChanged={onQDataChanged}/>}
		</GridSection>
	);
};
export default FileDetails;
