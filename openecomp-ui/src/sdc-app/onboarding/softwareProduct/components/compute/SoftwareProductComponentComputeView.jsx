import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';
import ValidationInput from'nfvo-components/input/validation/ValidationInput.jsx';


class SoftwareProductComponentComputeView extends React.Component {

	static propTypes = {
		qdata: React.PropTypes.object,
		qschema: React.PropTypes.object,
		isReadOnlyMode: React.PropTypes.bool,
		minNumberOfVMsSelectedByUser: React.PropTypes.number,
		onQDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired
	};

	render() {
		let {qdata, qschema, isReadOnlyMode, minNumberOfVMsSelectedByUser, onQDataChanged, onSubmit} = this.props;

		return (
			<div className='vsp-component-questionnaire-view'>
				<ValidationForm
					ref='computeValidationForm'
					hasButtons={false}
					onSubmit={() => onSubmit({qdata})}
					className='component-questionnaire-validation-form'
					isReadOnlyMode={isReadOnlyMode}
					onDataChanged={onQDataChanged}
					data={qdata}
					schema={qschema}>

					<div className='section-title'>{i18n('VM Sizing')}</div>
					<div className='rows-section'>
						<div className='row-flex-components input-row'>
							<div className='single-col'>
								<ValidationInput
									type='text'
									label={i18n('Number of CPUs')}
									pointer={'/compute/vmSizing/numOfCPUs'}/>
							</div>
							<div className='single-col'>
								<ValidationInput
									type='text'
									label={i18n('File System Size (GB)')}
									pointer={'/compute/vmSizing/fileSystemSizeGB'}/>
							</div>
							<div className='single-col'>
								<ValidationInput
									type='text'
									label={i18n('Persistent Storage/Volume Size (GB)')}
									pointer={'/compute/vmSizing/persistentStorageVolumeSize'}/>
							</div>
							<ValidationInput
								type='text'
								label={i18n('I/O Operations (per second)')}
								pointer={'/compute/vmSizing/IOOperationsPerSec'}/>
						</div>
					</div>
					<div className='section-title'>{i18n('Number of VMs')}</div>
					<div className='rows-section'>
						<div className='row-flex-components input-row'>
							<div className='single-col'>
								<ValidationInput
									type='text'
									label={i18n('Minimum')}
									pointer={'/compute/numOfVMs/minimum'}/>
							</div>
							<div className='single-col'>
								<ValidationInput
									type='text'
									label={i18n('Maximum')}
									pointer={'/compute/numOfVMs/maximum'}
									validations={{minValue: minNumberOfVMsSelectedByUser}}/>
							</div>
							<div className='single-col'>
								<ValidationInput
									type='select'
									label={i18n('CPU Oversubscription Ratio')}
									pointer={'/compute/numOfVMs/CpuOverSubscriptionRatio'}/>
							</div>
							<ValidationInput
								type='select'
								label={i18n('Memory - RAM')}
								pointer={'/compute/numOfVMs/MemoryRAM'}/>
						</div>
					</div>

					<div className='section-title'>{i18n('Guest OS')}</div>
					<div className='rows-section'>
						<div className='section-field row-flex-components input-row'>
							<div className='two-col'>
								<ValidationInput
									label={i18n('Guest OS')}
									type='text'
									pointer={'/compute/guestOS/name'}/>
							</div>
							<div className='empty-two-col'/>
						</div>
						<div className='vertical-flex input-row'>
							<label key='label' className='control-label'>{i18n('OS Bit Size')}</label>
							<div className='radio-options-content-row input-row'>
								<ValidationInput
									type='radiogroup'
									pointer={'/compute/guestOS/bitSize'}
									className='radio-field'/>
							</div>
						</div>
						<div className='section-field row-flex-components input-row'>
							<div className='two-col'>
								<ValidationInput
									type='textarea'
									label={i18n('Guest OS Tools:')}
									pointer={'/compute/guestOS/tools'}/>
							</div>
							<div className='empty-two-col'/>
						</div>
					</div>
				</ValidationForm>
			</div>
		);
	}

	save(){
		return this.refs.computeValidationForm.handleFormSubmit(new Event('dummy'));
	}
}

export default SoftwareProductComponentComputeView;
