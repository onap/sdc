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
import Form from 'nfvo-components/input/validation/Form.jsx';
import NumberOfVms from './computeComponents/NumberOfVms.jsx';
import GuestOs from './computeComponents/GuestOs.jsx';
import ComputeFlavors from './computeComponents/ComputeFlavors.js';
import Validator from 'nfvo-utils/Validator.js';

class SoftwareProductComponentComputeView extends React.Component {

	static propTypes = {
		dataMap: PropTypes.object,
		qgenericFieldInfo: PropTypes.object,
		isReadOnlyMode: PropTypes.bool,
		isManual: PropTypes.bool,
		onQDataChanged: PropTypes.func.isRequired,
		qValidateData: PropTypes.func.isRequired,
		onSubmit: PropTypes.func.isRequired
	};

	render() {
		let {softwareProductId, componentId, version, qdata, dataMap, qgenericFieldInfo, isReadOnlyMode, onQDataChanged, qValidateData,
			onSubmit, computeFlavorsList, isManual} = this.props;

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
					<NumberOfVms onQDataChanged={onQDataChanged} dataMap={dataMap}
						 qgenericFieldInfo={qgenericFieldInfo} qValidateData={qValidateData}
						 customValidations={{'compute/numOfVMs/maximum' : this.validateMax, 'compute/numOfVMs/minimum': this.validateMin}} />
					<GuestOs onQDataChanged={onQDataChanged} dataMap={dataMap} qgenericFieldInfo={qgenericFieldInfo} />
					<ComputeFlavors computeFlavorsList={computeFlavorsList} softwareProductId={softwareProductId} componentId={componentId}
						version={version} isReadOnlyMode={isReadOnlyMode} isManual={isManual}/>
				</Form> }
			</div>
		);
	}

	save(){
		return this.form.handleFormSubmit(new Event('dummy'));
	}

	validateMin(value, state) {
		let maxVal = state.dataMap['compute/numOfVMs/maximum'];
		// we are allowed to have an empty maxval, that will allow all minvals.
		// if we do not have a minval than there is no point to check it either.
		if (value === undefined || maxVal === undefined) {
			return { isValid: true, errorText: '' };
		} else {
			return Validator.validateItem(value, maxVal,'maximum');
		}
	}

	validateMax(value, state) {
		let minVal = state.dataMap['compute/numOfVMs/minimum'];
		if (minVal === undefined ) {
			// having no minimum is the same as 0, maximum value doesn't need to be checked
			// against it.
			return { isValid: true, errorText: '' };
		} else {
			return Validator.validateItem(value,minVal,'minimum');
		}
	}
}

export default SoftwareProductComponentComputeView;
