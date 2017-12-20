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
import Acceptable from './nicEditorComponents/Acceptable.jsx';
import FlowLength from './nicEditorComponents/FlowLength.jsx';
import OutFlowTraffic from './nicEditorComponents/OutFlowTraffic.jsx';
import InFlowTraffic from './nicEditorComponents/InFlowTraffic.jsx';
import Sizing from './nicEditorComponents/Sizing.jsx';
import Network from './nicEditorComponents/Network.jsx';
import IpConfig from './nicEditorComponents/IpConfig.jsx';
import Protocols from './nicEditorComponents/Protocols.jsx';
import NameAndPurpose from './nicEditorComponents/NameAndPurpose.jsx';

class SoftwareProductComponentsNetworkEditorView extends React.Component {

	render() {
		let {onCancel, onValidateForm, isReadOnlyMode, isFormValid, formReady, data = {}, qgenericFieldInfo,
			dataMap, onDataChanged, protocols, onQDataChanged, isManual, genericFieldInfo} = this.props;
		let {name, description, networkName, networkType, networkDescription} = data;
		let netWorkValues = [{
			enum: networkName,
			title: networkName
		}];
		return (
			<div>
		{qgenericFieldInfo && <Form
			ref={(form) => { this.form = form; }}
			hasButtons={true}
			onSubmit={ () => this.submit() }
			onReset={ () => onCancel() }
			labledButtons={true}
			isReadOnlyMode={isReadOnlyMode}
			isValid={isFormValid}
			formReady={formReady}
			onValidateForm={() => onValidateForm() }
			className='vsp-components-network-editor'>
				<div className='editor-data'>
					<NameAndPurpose isManual={isManual}  name={name} description={description} onDataChanged={onDataChanged} isReadOnlyMode={isReadOnlyMode} genericFieldInfo={genericFieldInfo} />
					<Protocols protocols={protocols} qgenericFieldInfo={qgenericFieldInfo} dataMap={dataMap} onQDataChanged={onQDataChanged} />
					<IpConfig dataMap={dataMap} onQDataChanged={onQDataChanged} />
					<Network networkDescription={networkDescription} onDataChanged={onDataChanged} networkValues={netWorkValues}  isReadOnlyMode={isReadOnlyMode} networkType={networkType}  />
					<Sizing qgenericFieldInfo={qgenericFieldInfo} dataMap={dataMap} onQDataChanged={onQDataChanged} />
					<InFlowTraffic qgenericFieldInfo={qgenericFieldInfo} dataMap={dataMap} onQDataChanged={onQDataChanged} />
					<OutFlowTraffic qgenericFieldInfo={qgenericFieldInfo} dataMap={dataMap} onQDataChanged={onQDataChanged} />
					<FlowLength qgenericFieldInfo={qgenericFieldInfo} dataMap={dataMap} onQDataChanged={onQDataChanged} />
					<Acceptable qgenericFieldInfo={qgenericFieldInfo} dataMap={dataMap} onQDataChanged={onQDataChanged} />
				</div>
			</Form> }
			</div>
		);
	}

	submit() {
		let {data, qdata, onSubmit} = this.props;
		onSubmit({data, qdata});
	}
}

export default SoftwareProductComponentsNetworkEditorView;
