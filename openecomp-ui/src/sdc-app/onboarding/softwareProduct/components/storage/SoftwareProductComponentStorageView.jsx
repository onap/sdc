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
import Form from 'nfvo-components/input/validation/Form.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import classnames from 'classnames';

const BackupSection = ({isReadOnlyMode,dataMap, onQDataChanged, qgenericFieldInfo}) => (
	<GridSection title={i18n('Backup')}>
		<GridItem>
			<div className='vertical-flex'>
				<label key='label' className={classnames('control-label',{'disabled': isReadOnlyMode})}>{i18n('Backup Type')}</label>
				<div className='radio-options-content-row'>
					{qgenericFieldInfo['storage/backup/backupType'].enum.map(onSite => (
						<Input
							data-test-id='backupType'
							type='radio'
							key={onSite.enum}
							name={'compute/guestOS/bitSize'}
							className='radio-field'
							value={onSite.enum}
							label={onSite.title}
							onChange={(site) => onQDataChanged({'storage/backup/backupType' :  site})}
							isValid={qgenericFieldInfo['storage/backup/backupType'].isValid}
							errorText={qgenericFieldInfo['storage/backup/backupType'].errorText}
							checked={dataMap['storage/backup/backupType'] === onSite.enum} /> )) }
				</div>
			</div>
		</GridItem>
		<GridItem>
			<Input
				className='section-field'
				data-test-id='backupSolution'
				onChange={(backupSolution) => onQDataChanged({'storage/backup/backupSolution' : backupSolution})}
				label={i18n('Backup Solution')}
				type='text'
				isValid={qgenericFieldInfo['storage/backup/backupSolution'].isValid}
				errorText={qgenericFieldInfo['storage/backup/backupSolution'].errorText}
				value={dataMap['storage/backup/backupSolution']}/>
		</GridItem>
		<GridItem>
			<Input
				className='section-field'
				data-test-id='backupStorageSize'
				onChange={(backupStorageSize) => onQDataChanged({'storage/backup/backupStorageSize' : backupStorageSize})}
				label={i18n('Backup Storage Size (GB)')}
				type='number'
				isValid={qgenericFieldInfo['storage/backup/backupStorageSize'].isValid}
				errorText={qgenericFieldInfo['storage/backup/backupStorageSize'].errorText}
				value={dataMap['storage/backup/backupStorageSize']}/>
		</GridItem>
		<GridItem>
			<Input
				data-test-id='backupNIC'
				label={i18n('Backup NIC')}
				type='select'
				className='input-options-select section-field'
				groupClassName='bootstrap-input-options'
				isValid={qgenericFieldInfo['storage/backup/backupNIC'].isValid}
				errorText={qgenericFieldInfo['storage/backup/backupNIC'].errorText}
				value={dataMap['storage/backup/backupNIC']}
				onChange={(e) => {
					const selectedIndex = e.target.selectedIndex;
					const val = e.target.options[selectedIndex].value;
					onQDataChanged({'storage/backup/backupNIC' : val});}
				}>
				<option key='placeholder' value=''>{i18n('Select...')}</option>
				{qgenericFieldInfo['storage/backup/backupNIC'].enum.map(hv => <option value={hv.enum} key={hv.enum}>{hv.title}</option>)}
			</Input>
		</GridItem>
	</GridSection>
);

const SnapshotBackupSection = ({dataMap, onQDataChanged, qgenericFieldInfo}) => (
	<GridSection title={i18n('Snapshot Backup')}>
		<GridItem>
			<Input
				className='section-field'
				data-test-id='snapshotFrequency'
				onChange={(snapshotFrequency) => onQDataChanged({'storage/snapshotBackup/snapshotFrequency' : snapshotFrequency})}
				label={i18n('Backup Storage Size (GB)')}
				type='number'
				isValid={qgenericFieldInfo['storage/snapshotBackup/snapshotFrequency'].isValid}
				errorText={qgenericFieldInfo['storage/snapshotBackup/snapshotFrequency'].errorText}
				value={dataMap['storage/snapshotBackup/snapshotFrequency']}/>
		</GridItem>
	</GridSection>
);

const LogBackupSection = ({dataMap, onQDataChanged, qgenericFieldInfo}) => (
	<GridSection title={i18n('Log Backup')}>
		<GridItem>
			<Input
				className='section-field'
				data-test-id='sizeOfLogFiles'
				onChange={(sizeOfLogFiles) => onQDataChanged({'storage/logBackup/sizeOfLogFiles' : sizeOfLogFiles})}
				label={i18n('Backup Storage Size (GB)')}
				type='number'
				isValid={qgenericFieldInfo['storage/logBackup/sizeOfLogFiles'].isValid}
				errorText={qgenericFieldInfo['storage/logBackup/sizeOfLogFiles'].errorText}
				value={dataMap['storage/logBackup/sizeOfLogFiles']}/>
		</GridItem>
		<GridItem>
			<Input
				className='section-field'
				label={i18n('Log Retention Period (days)')}
				data-test-id='logRetentionPeriod'
				onChange={(logRetentionPeriod) => onQDataChanged({'storage/logBackup/logRetentionPeriod' : logRetentionPeriod})}
				type='number'
				isValid={qgenericFieldInfo['storage/logBackup/logRetentionPeriod'].isValid}
				errorText={qgenericFieldInfo['storage/logBackup/logRetentionPeriod'].errorText}
				value={dataMap['storage/logBackup/logRetentionPeriod']}/>
		</GridItem>
		<GridItem>
			<Input
				className='section-field'
				label={i18n('Log Backup Frequency (days)')}
				data-test-id='logBackupFrequency'
				onChange={(logBackupFrequency) => onQDataChanged({'storage/logBackup/logBackupFrequency' : logBackupFrequency})}
				type='number'
				isValid={qgenericFieldInfo['storage/logBackup/logBackupFrequency'].isValid}
				errorText={qgenericFieldInfo['storage/logBackup/logBackupFrequency'].errorText}
				value={dataMap['storage/logBackup/logBackupFrequency']}/>
		</GridItem>
		<GridItem>
			<Input
				className='section-field'
				label={i18n('Log File Location')}
				data-test-id='logFileLocation'
				onChange={(logFileLocation) => onQDataChanged({'storage/logBackup/logFileLocation' : logFileLocation})}
				type='text'
				isValid={qgenericFieldInfo['storage/logBackup/logFileLocation'].isValid}
				errorText={qgenericFieldInfo['storage/logBackup/logFileLocation'].errorText}
				value={dataMap['storage/logBackup/logFileLocation']}/>
		</GridItem>
	</GridSection>
);

class SoftwareProductComponentStorageView extends React.Component {

	static propTypes = {
		componentId: PropTypes.string,
		onQDataChanged: PropTypes.func,
		onSubmit: PropTypes.func,
		isReadOnlyMode: PropTypes.bool
	};

	render() {
		let {onQDataChanged, dataMap, qGenericFieldInfo, isReadOnlyMode, onSubmit, qdata} =  this.props;

		return(
			<div className='vsp-component-questionnaire-view'>
				{qGenericFieldInfo && <Form
					ref={form => this.form = form }
					isValid={true}
					formReady={null}
					onSubmit={() => onSubmit({qdata})}
					className='component-questionnaire-validation-form'
					isReadOnlyMode={isReadOnlyMode}
					hasButtons={false}>
					<BackupSection isReadOnlyMode={isReadOnlyMode} onQDataChanged={onQDataChanged} dataMap={dataMap} qgenericFieldInfo={qGenericFieldInfo}/>
					<SnapshotBackupSection  onQDataChanged={onQDataChanged} dataMap={dataMap} qgenericFieldInfo={qGenericFieldInfo}/>
					<LogBackupSection  onQDataChanged={onQDataChanged} dataMap={dataMap} qgenericFieldInfo={qGenericFieldInfo}/>
				</Form> }
			</div>
		);
	}

	save(){		
		const {qdata, onSubmit} = this.props;
		return onSubmit({qdata});
	}
}

export default SoftwareProductComponentStorageView;
