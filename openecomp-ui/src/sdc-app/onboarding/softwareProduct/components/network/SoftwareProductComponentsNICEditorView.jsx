import React from 'react';

import i18n from 'nfvo-utils/i18n/i18n.js';

import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';
import ValidationInput from 'nfvo-components/input/validation/ValidationInput.jsx';

class SoftwareProductComponentsNetworkEditorView extends React.Component {

	render() {
		let {onCancel, isReadOnlyMode} = this.props;
		return (
			<ValidationForm
				ref='validationForm'
				hasButtons={true}
				onSubmit={ () => this.submit() }
				onReset={ () => onCancel() }
				labledButtons={true}
				isReadOnlyMode={isReadOnlyMode}
				className='vsp-components-network-editor'>
				{this.renderEditorFields()}
			</ValidationForm>
		);
	}

	renderEditorFields() {
		let {data = {}, qdata = {}, qschema = {}, onQDataChanged, onDataChanged, isReadOnlyMode} = this.props;
		let {name, description, networkName} = data;
		let netWorkValues = [{
			enum: networkName,
			title: networkName
		}];
		return(
			<div className='editor-data'>
				<div className='row'>
					<div className='col-md-6'>
						<ValidationInput
							label={i18n('Name')}
							value={name}
							disabled={true}
							type='text'/>
					</div>
					<div className='col-md-6'>
						<ValidationInput
							label={i18n('Purpose of NIC')}
							value={description}
							onChange={description => onDataChanged({description})}
							disabled={isReadOnlyMode}
							type='textarea'/>
					</div>
				</div>
				<ValidationForm
					onDataChanged={onQDataChanged}
					data={qdata}
					schema={qschema}
					isReadOnlyMode={isReadOnlyMode}
					hasButtons={false}>
					<div className='row'>
						<div className='part-title'>{i18n('Protocols')}</div>
						<div className='col-md-6'>
							<ValidationInput
								label={i18n('Protocols')}
								type='select'
								pointer='/protocols/protocols'/>
						</div>
						<div className='col-md-6'>
							<ValidationInput
								label={i18n('Protocol with Highest Traffic Profile')}
								type='select'
								pointer='/protocols/protocolWithHighestTrafficProfile'/>
						</div>
					</div>
					<div className='row'>
						<div className='part-title'>{i18n('IP Configuration')}</div>
						<div className='col-md-3'>
							<ValidationInput
								label={i18n('IPv4 Required')}
								type='checkbox'
								pointer='/ipConfiguration/ipv4Required'/>
						</div>
						<div className='col-md-9'>
							<ValidationInput
								label={i18n('IPv6 Required')}
								type='checkbox'
								pointer='/ipConfiguration/ipv6Required'/>
						</div>
					</div>
				</ValidationForm>
				<div className='row'>
					<div className='part-title'>{i18n('Network')}</div>
					<div className='col-md-2'>
						<ValidationInput
							label={i18n('Internal')}
							disabled
							checked={true}
							className='network-radio disabled'
							type='radio'/>
					</div>
					<div className='col-md-4'>
						<ValidationInput
							label={i18n('External')}
							disabled
							checked={false}
							className='network-radio disabled'
							type='radio'/>
					</div>
					<div className='col-md-6'>
						<ValidationInput
							label={i18n('Network')}
							type='select'
							disabled={true}
							values={netWorkValues}/>
					</div>
				</div>
				<ValidationForm
					onDataChanged={onQDataChanged}
					data={qdata}
					schema={qschema}
					isReadOnlyMode={isReadOnlyMode}
					hasButtons={false}>
					<div className='row'>
						<div className='part-title'>{i18n('Sizing')}</div>
						<div className='col-md-12'>
							<ValidationInput
								label={i18n('Describe Quality of Service')}
								type='textarea'
								pointer='/sizing/describeQualityOfService'/>
						</div>
					</div>

					<div className='row'>
						<div className='part-title'>{i18n('Inflow Traffic per second')}</div>
					</div>

					<div className='row'>
						<div className='col-md-6'>
							<div className='row'>
								<div className='part-title-small'>{i18n('Packets')}</div>
							</div>
							<div className='row'>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Peak')}
										type='text'
										pointer='/sizing/inflowTrafficPerSecond/packets/peak'/>
								</div>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Avg')}
										type='text'
										pointer='/sizing/inflowTrafficPerSecond/packets/avg'/>
								</div>
							</div>
						</div>
						<div className='col-md-6'>
							<div className='row'>
								<div className='part-title-small'>{i18n('Bytes')}</div>
							</div>
							<div className='row'>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Peak')}
										type='text'
										pointer='/sizing/inflowTrafficPerSecond/bytes/peak'/>

								</div>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Avg')}
										type='text'
										pointer='/sizing/inflowTrafficPerSecond/bytes/avg'/>
								</div>
							</div>
						</div>
					</div>

					<div className='row'>
						<div className='part-title'>{i18n('Outflow Traffic per second')}</div>
					</div>

					<div className='row'>
						<div className='col-md-6'>
							<div className='row'>
								<div className='part-title-small'>{i18n('Packets')}</div>
							</div>
							<div className='row'>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Peak')}
										type='text'
										pointer='/sizing/outflowTrafficPerSecond/packets/peak'/>
								</div>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Avg')}
										type='text'
										pointer='/sizing/outflowTrafficPerSecond/packets/avg'/>

								</div>
							</div>
						</div>
						<div className='col-md-6'>
							<div className='row'>
								<div className='part-title-small'>{i18n('Bytes')}</div>
							</div>
							<div className='row'>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Peak')}
										type='text'
										pointer='/sizing/outflowTrafficPerSecond/bytes/peak'/>

								</div>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Avg')}
										type='text'
										pointer='/sizing/outflowTrafficPerSecond/bytes/avg'/>

								</div>
							</div>
						</div>
					</div>

					<div className='row'>
						<div className='part-title'>{i18n('Flow Length')}</div>
					</div>

					<div className='row'>
						<div className='col-md-6'>
							<div className='row'>
								<div className='part-title-small'>{i18n('Packets')}</div>
							</div>
							<div className='row'>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Peak')}
										type='text'
										pointer='/sizing/flowLength/packets/peak'/>
								</div>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Avg')}
										type='text'
										pointer='/sizing/flowLength/packets/avg'/>
								</div>
							</div>
						</div>
						<div className='col-md-6'>
							<div className='row'>
								<div className='part-title-small'>{i18n('Bytes')}</div>
							</div>
							<div className='row'>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Peak')}
										type='text'
										pointer='/sizing/flowLength/bytes/peak'/>

								</div>
								<div className='col-md-6'>
									<ValidationInput
										label={i18n('Avg')}
										type='text'
										pointer='/sizing/flowLength/bytes/avg'/>
								</div>
							</div>
						</div>
					</div>

					<div className='row'>
						<div className='col-md-9'>
							<div className='row'>
								<div className='part-title-small'>{i18n('Acceptable Jitter')}</div>
							</div>
							<div className='row'>
								<div className='col-md-4'>
									<ValidationInput
										label={i18n('Min')}
										type='text'
										pointer='/sizing/acceptableJitter/mean'/>
								</div>
								<div className='col-md-4'>
									<ValidationInput
										label={i18n('Max')}
										type='text'
										pointer='/sizing/acceptableJitter/max'/>
								</div>
								<div className='col-md-4'>
									<ValidationInput
										label={i18n('Var')}
										type='text'
										pointer='/sizing/acceptableJitter/variable'/>
								</div>
							</div>
						</div>
						<div className='col-md-3'>
							<div className='row'>
								<div className='part-title-small'>{i18n('Acceptable Packet Loss %')}</div>
							</div>
							<div className='row'>
								<div className='col-md-12'>
									<ValidationInput
										label={i18n('In Percent')}
										type='text'
										pointer='/sizing/acceptablePacketLoss'/>
								</div>
							</div>
						</div>
					</div>
				</ValidationForm>
			</div>

		);
	}
	submit() {
		let {data, qdata, onSubmit} = this.props;
		onSubmit({data, qdata});
	}
}

export default SoftwareProductComponentsNetworkEditorView;
