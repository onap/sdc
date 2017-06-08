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
import Form from 'nfvo-components/input/validation/Form.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';

const GeneralSection = ({onDataChanged, displayName, vfcCode, description, isReadOnlyMode, genericFieldInfo}) => (
	<GridSection title={i18n('General')}>
		{/* disabled until backend will be ready to implement it
			<div className='validation-input-wrapper'>
			<div className='form-group'>
			<label className='control-label'>{i18n('Name')}</label>
			<div>{name}</div>
			</div>
			</div>

		*/}
		<GridItem>
			<Input
				data-test-id='name'
				label={i18n('Name')}
				value={displayName}
				disabled={true}
				type='text'/>
			<Input
				data-test-id='vfcCode'
				label={i18n('Naming Code')}
				value={vfcCode}
				isValid={genericFieldInfo.vfcCode.isValid}
				errorText={genericFieldInfo.vfcCode.errorText}
				onChange={vfcCode => onDataChanged({vfcCode})}
				disabled={isReadOnlyMode}
				type='text'/>
		</GridItem>
		<GridItem colSpan={2}>
			<Input
				label={i18n('Description')}
				isValid={genericFieldInfo.description.isValid}
				errorText={genericFieldInfo.description.errorText}
				onChange={description => onDataChanged({description})}
				disabled={isReadOnlyMode}
				value={description}
				groupClassName='multi-line-textarea'
				data-test-id='description'
				type='textarea'/>
		</GridItem>
		<GridItem />
	</GridSection>
		);

const HypervisorSection = ({dataMap, onQDataChanged, qgenericFieldInfo}) => (
	<GridSection title={i18n('Hypervisor')}>
		<GridItem>
			<Input
				data-test-id='hypervisor'
				label={i18n('Supported Hypervisors')}
				type='select'
				className='input-options-select'
				groupClassName='bootstrap-input-options'
				isValid={qgenericFieldInfo['general/hypervisor/hypervisor'].isValid}
				errorText={qgenericFieldInfo['general/hypervisor/hypervisor'].errorText}
				value={dataMap['general/hypervisor/hypervisor']}
				onChange={(e) => {
					const selectedIndex = e.target.selectedIndex;
					const val = e.target.options[selectedIndex].value;
					onQDataChanged({'general/hypervisor/hypervisor' : val});}
				}>
				<option key='placeholder' value=''>{i18n('Select...')}</option>
				{qgenericFieldInfo['general/hypervisor/hypervisor'].enum.map(hv => <option value={hv.enum} key={hv.enum}>{hv.title}</option>)}
			</Input>
		</GridItem>
		<GridItem colSpan={2}>
			<Input
				data-test-id='drivers'
				onChange={(driver) => onQDataChanged({'general/hypervisor/drivers' : driver})}
				label={i18n('Hypervisor Drivers')}
				type='text'
				isValid={qgenericFieldInfo['general/hypervisor/drivers'].isValid}
				errorText={qgenericFieldInfo['general/hypervisor/drivers'].errorText}
				value={dataMap['general/hypervisor/drivers']}/>
		</GridItem>
		<GridItem colSpan={3}>
			<Input
				data-test-id='containerFeaturesDescription'
				label={i18n('Describe Container Features')}
				type='textarea'
				onChange={(containerFeaturesDescription) => onQDataChanged({'general/hypervisor/containerFeaturesDescription' : containerFeaturesDescription})}
				isValid={qgenericFieldInfo['general/hypervisor/containerFeaturesDescription'].isValid}
				errorText={qgenericFieldInfo['general/hypervisor/containerFeaturesDescription'].errorText}
				value={dataMap['general/hypervisor/containerFeaturesDescription']}/>
		</GridItem>
	</GridSection>
);

const ImageSection = ({dataMap, onQDataChanged, qgenericFieldInfo}) => (
	<GridSection title={i18n('Image')}>
		<GridItem>
			<Input
				data-test-id='format'
				label={i18n('Image format')}
				type='select'
				className='input-options-select'
				groupClassName='bootstrap-input-options'
				isValid={qgenericFieldInfo['general/image/format'].isValid}
				errorText={qgenericFieldInfo['general/image/format'].errorText}
				value={dataMap['general/image/format']}
				onChange={(e) => {
					const selectedIndex = e.target.selectedIndex;
					const val = e.target.options[selectedIndex].value;
					onQDataChanged({'general/image/format' : val});}
				}>
				<option key='placeholder' value=''>{i18n('Select...')}</option>
				{qgenericFieldInfo['general/image/format'].enum.map(hv => <option value={hv.enum} key={hv.enum}>{hv.title}</option>)}
			</Input>
		</GridItem>
		<GridItem>
			<Input
				data-test-id='providedBy'
				label={i18n('Image provided by')}
				type='select'
				className='input-options-select'
				groupClassName='bootstrap-input-options'
				isValid={qgenericFieldInfo['general/image/providedBy'].isValid}
				errorText={qgenericFieldInfo['general/image/providedBy'].errorText}
				value={dataMap['general/image/providedBy']}
				onChange={(e) => {
					const selectedIndex = e.target.selectedIndex;
					const val = e.target.options[selectedIndex].value;
					onQDataChanged({'general/image/providedBy' : val});}
				}>
				<option key='placeholder' value=''>{i18n('Select...')}</option>
				{qgenericFieldInfo['general/image/providedBy'].enum.map(hv => <option value={hv.enum} key={hv.enum}>{hv.title}</option>)}
			</Input>
		</GridItem>
		<GridItem>
			<Input
				data-test-id='bootDiskSizePerVM'
				onChange={(bootDiskSizePerVM) => onQDataChanged({'general/image/bootDiskSizePerVM' : bootDiskSizePerVM})}
				label={i18n('Size of boot disk per VM (GB)')}
				type='number'
				isValid={qgenericFieldInfo['general/image/bootDiskSizePerVM'].isValid}
				errorText={qgenericFieldInfo['general/image/bootDiskSizePerVM'].errorText}
				value={dataMap['general/image/bootDiskSizePerVM']}/>
		</GridItem>
		<GridItem>
			<Input
				data-test-id='ephemeralDiskSizePerVM'
				onChange={(ephemeralDiskSizePerVM) => onQDataChanged({'general/image/ephemeralDiskSizePerVM' : ephemeralDiskSizePerVM})}
				label={i18n('Size of ephemeral disk per VM (GB)')}
				type='number'
				isValid={qgenericFieldInfo['general/image/ephemeralDiskSizePerVM'].isValid}
				errorText={qgenericFieldInfo['general/image/ephemeralDiskSizePerVM'].errorText}
				value={dataMap['general/image/ephemeralDiskSizePerVM']}/>
		</GridItem>
	</GridSection>
);

const RecoverySection = ({dataMap, onQDataChanged, qgenericFieldInfo}) => (
	<GridSection title={i18n('Recovery')}>
		<GridItem>
			<Input
				data-test-id='pointObjective'
				label={i18n('VM Recovery Point Objective (Minutes)')}
				type='number'
				onChange={(pointObjective) => onQDataChanged({'general/recovery/pointObjective' : pointObjective})}
				isValid={qgenericFieldInfo['general/recovery/pointObjective'].isValid}
				errorText={qgenericFieldInfo['general/recovery/pointObjective'].errorText}
				value={dataMap['general/recovery/pointObjective']}/>
		</GridItem>
		<GridItem>
			<Input
				data-test-id='timeObjective'
				label={i18n('VM Recovery Time Objective (Minutes)')}
				type='number'
				onChange={(timeObjective) => onQDataChanged({'general/recovery/timeObjective' : timeObjective})}
				isValid={qgenericFieldInfo['general/recovery/timeObjective'].isValid}
				errorText={qgenericFieldInfo['general/recovery/timeObjective'].errorText}
				value={dataMap['general/recovery/timeObjective']}/>
			<div className='empty-two-col' />
		</GridItem>
		<GridItem colSpan={2} />
		<GridItem colSpan={2}>
			<Input
				data-test-id='vmProcessFailuresHandling'
				className='textarea'
				label={i18n('How are in VM process failures handled?')}
				type='textarea'
				onChange={(vmProcessFailuresHandling) => onQDataChanged({'general/recovery/vmProcessFailuresHandling' : vmProcessFailuresHandling})}
				isValid={qgenericFieldInfo['general/recovery/vmProcessFailuresHandling'].isValid}
				errorText={qgenericFieldInfo['general/recovery/vmProcessFailuresHandling'].errorText}
				value={dataMap['general/recovery/vmProcessFailuresHandling']}/>
			<div className='empty-two-col' />

		</GridItem>
		{
			/** disabled until backend will be ready to implement it
			<div className='row'>
			<div className='col-md-3'>
			<Input
			label={i18n('VM Recovery Document')}
			type='text'
			pointer='/general/recovery/VMRecoveryDocument'/>
			</div>
			</div>
			*/
		}
	</GridSection>
);

const DNSConfigurationSection = ({dataMap, onQDataChanged, qgenericFieldInfo}) => (
	<GridSection title={i18n('DNS Configuration')}>
		<GridItem colSpan={2}>
			<Input
				data-test-id='dnsConfiguration'
				label={i18n('Do you have a need for DNS as a Service? Please describe.')}
				type='textarea'
				onChange={(dnsConfiguration) => onQDataChanged({'general/dnsConfiguration' : dnsConfiguration})}
				isValid={qgenericFieldInfo['general/dnsConfiguration'].isValid}
				errorText={qgenericFieldInfo['general/dnsConfiguration'].errorText}
				value={dataMap['general/dnsConfiguration']}/>
		</GridItem>
	</GridSection>
);

const CloneSection = ({dataMap, onQDataChanged, qgenericFieldInfo}) => (
	<GridSection title={i18n('Clone')}>
		<GridItem colSpan={2}>
			<Input
				data-test-id='vmCloneUsage'
				label={i18n('Describe VM Clone Use')}
				type='textarea'
				onChange={(vmCloneUsage) => onQDataChanged({'general/vmCloneUsage' : vmCloneUsage})}
				isValid={qgenericFieldInfo['general/vmCloneUsage'].isValid}
				errorText={qgenericFieldInfo['general/vmCloneUsage'].errorText}
				value={dataMap['general/vmCloneUsage']}/>
		</GridItem>
	</GridSection>
);

class SoftwareProductComponentsGeneralView extends React.Component {

	render() {
		let {onQDataChanged, onDataChanged,	genericFieldInfo, dataMap, qGenericFieldInfo, componentData: {displayName, vfcCode, description}, isReadOnlyMode} =  this.props;
		return(
			<div className='vsp-components-general'>
				<div className='general-data'>
					{genericFieldInfo && qGenericFieldInfo && <Form
						isValid={this.props.isFormValid}
						formReady={null}
						isReadOnlyMode={isReadOnlyMode}
						onValidityChanged={(isValidityData) => this.props.onValidityChanged(isValidityData)}
						hasButtons={false}>
						<GeneralSection
							onDataChanged={onDataChanged}
							displayName={displayName}
							vfcCode={vfcCode}
							description={description}
							isReadOnlyMode={isReadOnlyMode}
							genericFieldInfo={genericFieldInfo}/>
						<HypervisorSection  onQDataChanged={onQDataChanged} dataMap={dataMap} qgenericFieldInfo={qGenericFieldInfo}/>
						<ImageSection  onQDataChanged={onQDataChanged} dataMap={dataMap} qgenericFieldInfo={qGenericFieldInfo}/>
						<RecoverySection  onQDataChanged={onQDataChanged} dataMap={dataMap} qgenericFieldInfo={qGenericFieldInfo}/>
						<DNSConfigurationSection  onQDataChanged={onQDataChanged} dataMap={dataMap} qgenericFieldInfo={qGenericFieldInfo}/>
						<CloneSection  onQDataChanged={onQDataChanged} dataMap={dataMap} qgenericFieldInfo={qGenericFieldInfo}/>
					</Form> }
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
