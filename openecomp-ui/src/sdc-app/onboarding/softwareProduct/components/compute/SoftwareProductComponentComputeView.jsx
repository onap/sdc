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
import Form from 'nfvo-components/input/validation/Form.jsx';
import VmSizing from './computeComponents/VmSizing.jsx';
import NumberOfVms from './computeComponents/NumberOfVms.jsx';
import GuestOs from './computeComponents/GuestOs.jsx';
import Validator from 'nfvo-utils/Validator.js';

class SoftwareProductComponentComputeView extends React.Component {

	static propTypes = {
		dataMap: React.PropTypes.object,
		qgenericFieldInfo: React.PropTypes.object,
		isReadOnlyMode: React.PropTypes.bool,
		onQDataChanged: React.PropTypes.func.isRequired,
		qValidateData: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired
	};

	render() {
		let {qdata, dataMap, qgenericFieldInfo, isReadOnlyMode, onQDataChanged, qValidateData, onSubmit} = this.props;

		return (
			<div className='vsp-component-questionnaire-view'>
				{ qgenericFieldInfo && <Form
					ref={ (form) => { this.form = form; }}
					formReady={null}
					isValid={true}
					hasButtons={false}
					onSubmit={() => onSubmit({qdata})}
					className='component-questionnaire-validation-form'
					isReadOnlyMode={isReadOnlyMode} >
					<VmSizing onQDataChanged={onQDataChanged} dataMap={dataMap} qgenericFieldInfo={qgenericFieldInfo} />
					<NumberOfVms onQDataChanged={onQDataChanged} dataMap={dataMap}
						 qgenericFieldInfo={qgenericFieldInfo} qValidateData={qValidateData}
						 customValidations={{'compute/numOfVMs/maximum' : this.validateMax, 'compute/numOfVMs/minimum': this.validateMin}} />
					<GuestOs onQDataChanged={onQDataChanged} dataMap={dataMap} qgenericFieldInfo={qgenericFieldInfo} />
				</Form> }
			</div>
		);
	}

	save(){
		return this.form.handleFormSubmit(new Event('dummy'));
	}

	validateMin(value, state) {
		let maxVal = state.dataMap['compute/numOfVMs/maximum'];
		return Validator.validateItem(value,maxVal,'maximum');
	}

	validateMax(value, state) {
		let minVal = state.dataMap['compute/numOfVMs/minimum'];
		return Validator.validateItem(value,minVal,'minimum');
	}
}

export default SoftwareProductComponentComputeView;
