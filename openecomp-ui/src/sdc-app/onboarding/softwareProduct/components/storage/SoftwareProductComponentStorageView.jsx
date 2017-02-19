import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';
import ValidationInput from'nfvo-components/input/validation/ValidationInput.jsx';


class SoftwareProductComponentStorageView extends React.Component {

	static propTypes = {
		componentId: React.PropTypes.string,
		onQDataChanged: React.PropTypes.func,
		onSubmit: React.PropTypes.func,
		isReadOnlyMode: React.PropTypes.bool
	};

	render() {
		let {qdata, qschema, onQDataChanged, onSubmit, isReadOnlyMode} = this.props;

		return(
			<div className='vsp-component-questionnaire-view'>
				<ValidationForm
					ref='storageValidationForm'
					hasButtons={false}
					onSubmit={() => onSubmit({qdata})}
					className='component-questionnaire-validation-form'
					isReadOnlyMode={isReadOnlyMode}
					onDataChanged={onQDataChanged}
					data={qdata}
					schema={qschema}>

					<div className='section-title'>{i18n('Backup')}</div>
					<div className='rows-section'>
						<div className='row-flex-components input-row'>
							<div className='single-col'>
								<div className='vertical-flex'>
									<label key='label' className='control-label'>{i18n('Backup Type')}</label>
									<div className='radio-options-content-row'>
										<ValidationInput
											label={i18n('On Site')}
											type='radiogroup'
											pointer={'/storage/backup/backupType'}
											className='radio-field'/>
									</div>
								</div>
							</div>
							<div className='single-col'>
								<ValidationInput
									type='text'
									label={i18n('Backup Solution')}
									pointer={'/storage/backup/backupSolution'}
									className='section-field'/>
							</div>
							<div className='single-col'>
								<ValidationInput
									type='text'
									label={i18n('Backup Storage Size (GB)')}
									pointer={'/storage/backup/backupStorageSize'}
									className='section-field'/>
							</div>
							<ValidationInput
								type='select'
								label={i18n('Backup NIC')}
								pointer={'/storage/backup/backupNIC'}
								className='section-field'/>
						</div>
					</div>

					<div className='section-title'>{i18n('Snapshot Backup')}</div>
					<div className='rows-section'>
						<div className='row-flex-components input-row'>
							<div className='single-col'>
								<ValidationInput
									type='text'
									label={i18n('Snapshot Frequency (hours)')}
									pointer={'/storage/snapshotBackup/snapshotFrequency'}
									className='section-field'/>
							</div>
							<div className='empty-two-col' />
							<div className='empty-col' />
						</div>
					</div>

					<div className='section-title'>{i18n('Log Backup')}</div>
					<div className='rows-section'>
						<div className='row-flex-components input-row'>
							<div className='single-col'>
								<ValidationInput
									type='text'
									label={i18n('Size of Log Files (GB)')}
									pointer={'/storage/logBackup/sizeOfLogFiles'}
									className='section-field'/>
								</div>
							<div className='single-col'>
							<ValidationInput
								type='text'
								label={i18n('Log Retention Period (days)')}
								pointer={'/storage/logBackup/logRetentionPeriod'}
								className='section-field'/>
								</div>
							<div className='single-col'>
							<ValidationInput
								type='text'
								label={i18n('Log Backup Frequency (days)')}
								pointer={'/storage/logBackup/logBackupFrequency'}
								className='section-field'/>
							</div>
							<ValidationInput
								type='text'
								label={i18n('Log File Location')}
								pointer={'/storage/logBackup/logFileLocation'}
								className='section-field'/>
						</div>
					</div>
				</ValidationForm>
			</div>
		);
	}

	save(){
		return this.refs.storageValidationForm.handleFormSubmit(new Event('dummy'));
	}
}

export default SoftwareProductComponentStorageView;
