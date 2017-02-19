import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';

import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';
import ValidationInput from'nfvo-components/input/validation/ValidationInput.jsx';


class SoftwareProductComponentsGeneralView extends React.Component {

	render() {
		let {qdata, qschema, onQDataChanged, onDataChanged, componentData: {displayName, description}, isReadOnlyMode} =  this.props;
		return(
			<div className='vsp-components-general'>
				<div className='general-data'>
					<ValidationForm
						isReadOnlyMode={isReadOnlyMode}
						hasButtons={false}>
					<div className=''>
						<h3 className='section-title'>{i18n('General')}</h3>
						<div className='rows-section'>
							<div className='row-flex-components input-row'>
							{/** disabled until backend will be ready to implement it
							 <div className='validation-input-wrapper'>
							 <div className='form-group'>
							 <label className='control-label'>{i18n('Name')}</label>
							 <div>{name}</div>
							 </div>
							 </div>

							*/}
								<div className='single-col'>
									<ValidationInput label={i18n('Name')} value={displayName}	disabled={true}	type='text'/>
								</div>
								<div className='two-col'>
								<ValidationInput
									label={i18n('Description')}
									onChange={description => onDataChanged({description})}
									disabled={isReadOnlyMode}
									value={description}
									type='textarea'/>
								</div>
								<div className='empty-col' />
							</div>
						</div>
					</div>
						</ValidationForm>
					{
						qschema &&
					<ValidationForm
						onDataChanged={onQDataChanged}
						data={qdata}
						schema={qschema}
						isReadOnlyMode={isReadOnlyMode}
						hasButtons={false}>
							<h3 className='section-title additional-validation-form'>{i18n('Hypervisor')}</h3>
							<div className='rows-section'>
								<div className='row-flex-components input-row'>
									<div className='single-col'>
										<ValidationInput
											label={i18n('Supported Hypervisors')}
											type='select'
											pointer='/general/hypervisor/hypervisor'/>
										</div>
									<div className='two-col'>
										<ValidationInput
											label={i18n('Hypervisor Drivers')}
											type='text'
											pointer='/general/hypervisor/drivers'/>
									</div>
									<div className='empty-col' />
								</div>
								<div className='row-flex-components input-row'>
									<div className='three-col'>
										<ValidationInput
											label={i18n('Describe Container Features')}
											type='textarea'
											pointer='/general/hypervisor/containerFeaturesDescription'/>
									</div>
									<div className='empty-col' />
								</div>
							</div>
							<h3 className='section-title'>{i18n('Image')}</h3>
							<div className='rows-section'>
								<div className='row-flex-components input-row'>
									<div className='single-col'>
										<ValidationInput
											label={i18n('Image format')}
											type='select'
											pointer='/general/image/format'/>
									</div>
									<div className='single-col'>
										<ValidationInput
											label={i18n('Image provided by')}
											type='select'
											pointer='/general/image/providedBy'/>
									</div>
									<div className='single-col'>
										<ValidationInput
											label={i18n('Size of boot disk per VM (GB)')}
											type='text'
											pointer='/general/image/bootDiskSizePerVM'/>
									</div>
									<ValidationInput
										label={i18n('Size of ephemeral disk per VM (GB)')}
										type='text'
										pointer='/general/image/ephemeralDiskSizePerVM'/>
								</div>
							</div>
							<h3 className='section-title'>{i18n('Recovery')}</h3>
							<div className='rows-section'>
								<div className='row-flex-components input-row'>
									<div className='single-col'>
										<ValidationInput
											label={i18n('VM Recovery Point Objective (Minutes)')}
											type='text'
											pointer='/general/recovery/pointObjective'/>
									</div>
									<ValidationInput
										label={i18n('VM Recovery Time Objective (Minutes)')}
										type='text'
										pointer='/general/recovery/timeObjective'/>
									<div className='empty-two-col' />
								</div>


								<div className='row-flex-components input-row'>
									<div className='two-col'>
										<ValidationInput
											className='textarea'
											label={i18n('How are in VM process failures handled?')}
											type='textarea'
											pointer='/general/recovery/vmProcessFailuresHandling'/>
									</div>
									<div className='empty-two-col' />
									{
										/** disabled until backend will be ready to implement it
											<div className='row'>
												<div className='col-md-3'>
													<ValidationInput
														label={i18n('VM Recovery Document')}
														type='text'
														pointer='/general/recovery/VMRecoveryDocument'/>
												</div>
											</div>
										 */
									}
									</div>
								</div>
								<h3 className='section-title'>{i18n('DNS Configuration')}</h3>
								<div className='rows-section'>
									<div className='row-flex-components input-row'>
										<div className='two-col'>
											<ValidationInput
												label={i18n('Do you have a need for DNS as a Service? Please describe.')}
												type='textarea'
												pointer='/general/dnsConfiguration'/>
										</div>
										<div className='empty-two-col' />
									</div>
								</div>
								<h3 className='section-title'>{i18n('Clone')}</h3>
								<div className='rows-section'>
									<div className='row-flex-components input-row'>
										<div className='two-col'>
											<ValidationInput
												label={i18n('Describe VM Clone Use')}
												type='textarea'
												pointer='/general/vmCloneUsage'/>
										</div>
										<div className='empty-two-col' />
									</div>
								</div>
					</ValidationForm>
					}
				</div>
			</div>
		);
	}

	save() {
		let {onSubmit, componentData, qdata} = this.props;
		return onSubmit({componentData, qdata});
	}
}

export default SoftwareProductComponentsGeneralView;
