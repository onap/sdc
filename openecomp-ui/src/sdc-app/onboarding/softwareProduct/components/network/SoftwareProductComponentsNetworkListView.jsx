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
import Form from 'nfvo-components/input/validation/Form.jsx';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import ListEditorItemViewField from 'nfvo-components/listEditor/ListEditorItemViewField.jsx';
import Input from'nfvo-components/input/validation/Input.jsx';
import Modal from 'nfvo-components/modal/Modal.jsx';

import SoftwareProductComponentsNICEditor from './SoftwareProductComponentsNICEditor.js';

class SoftwareProductComponentsNetworkView extends React.Component {

	state = {
		localFilter: ''
	};

	render() {
		let {dataMap, qgenericFieldInfo, onQDataChanged, isModalInEditMode, isDisplayModal, softwareProductId, componentId, isReadOnlyMode} = this.props;

		return(
			<div className='vsp-components-network'>
				<div className='network-data'>
					<div>
{ qgenericFieldInfo && <Form
	formReady={null}
	isValid={true}
	onSubmit={() => this.save()}
	isReadOnlyMode={isReadOnlyMode}
	hasButtons={false}>
							<h3 className='section-title'>{i18n('Network Capacity')}</h3>
							<div className='rows-section'>
								<div className='row-flex-components'>
									<div className='single-col'>
										<Input
											data-test-id='protocolWithHighestTrafficProfileAcrossAllNICs'
											label={i18n('Protocol with Highest Traffic Profile across all NICs')}
											type='select'
											groupClassName='bootstrap-input-options'
											className='input-options-select'
											isValid={qgenericFieldInfo['network/networkCapacity/protocolWithHighestTrafficProfileAcrossAllNICs'].isValid}
											errorText={qgenericFieldInfo['network/networkCapacity/protocolWithHighestTrafficProfileAcrossAllNICs'].errorText}
											value={dataMap['network/networkCapacity/protocolWithHighestTrafficProfileAcrossAllNICs']}
											onChange={(e) => {
												const selectedIndex = e.target.selectedIndex;
												const val = e.target.options[selectedIndex].value;
												onQDataChanged({'network/networkCapacity/protocolWithHighestTrafficProfileAcrossAllNICs' : val});}
											}>
											<option key='placeholder' value=''>{i18n('Select...')}</option>
											{ qgenericFieldInfo['network/networkCapacity/protocolWithHighestTrafficProfileAcrossAllNICs'].enum.map(proto =>
												<option value={proto.enum} key={proto.enum}>{proto.title}</option>) }
										</Input>
									</div>
									<div className='single-col add-line-break'>
										<Input
											data-test-id='networkTransactionsPerSecond'
											label={i18n('Network Transactions per Second')}
											type='number'
											onChange={(ntps) => onQDataChanged({'network/networkCapacity/networkTransactionsPerSecond' : ntps})}
											isValid={qgenericFieldInfo['network/networkCapacity/networkTransactionsPerSecond'].isValid}
											errorText={qgenericFieldInfo['network/networkCapacity/networkTransactionsPerSecond'].errorText}
											value={dataMap['network/networkCapacity/networkTransactionsPerSecond']} />
									</div>
									<div className='empty-two-col' />
								</div>
							</div>

						</Form> }
					</div>
					{this.renderNicList()}
				</div>
				<Modal show={isDisplayModal} bsSize='large' animation={true} className='network-nic-modal'>
					<Modal.Header>
						<Modal.Title>{isModalInEditMode ? i18n('Edit NIC') : i18n('Create New NIC')}</Modal.Title>
					</Modal.Header>
					<Modal.Body>
						{
							<SoftwareProductComponentsNICEditor
								softwareProductId={softwareProductId}
								componentId={componentId}
								isReadOnlyMode={isReadOnlyMode}/>
						}
					</Modal.Body>
				</Modal>
			</div>
		);
	}

	renderNicList() {
		const {localFilter} = this.state;
		let {isReadOnlyMode} = this.props;
		return (
			<ListEditorView
				title={i18n('Interfaces')}
				filterValue={localFilter}
				placeholder={i18n('Filter NICs by Name')}
				isReadOnlyMode={isReadOnlyMode}
				onFilter={value => this.setState({localFilter: value})}
				twoColumns>
				{this.filterList().map(nic => this.renderNicListItem(nic, isReadOnlyMode))}
			</ListEditorView>
		);
	}

	renderNicListItem(nic, isReadOnlyMode) {
		let {id, name, description, networkName = ''} = nic;
		let {onEditNicClick, version} =  this.props;
		return (
			<ListEditorItemView
				key={id}
				isReadOnlyMode={isReadOnlyMode}
				onSelect={() => onEditNicClick(nic, version)}>

				<ListEditorItemViewField>
					<div className='name'>{name}</div>
				</ListEditorItemViewField>
				<ListEditorItemViewField>
					<div className='details'>
						<div className='title'>{i18n('Purpose of NIC')}</div>
						<div className='description'>{description}</div>
					</div>
					<div className='details'>
						<div className='title'>{i18n('Network')}</div>
						<div className='artifact-name'>{networkName}</div>
					</div>
				</ListEditorItemViewField>

			</ListEditorItemView>
		);
	}

	filterList() {
		let {nicList} = this.props;
		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return nicList.filter(({name = '', description = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter);
			});
		}
		else {
			return nicList;
		}
	}

	save() {
		let {onSubmit, qdata, version} = this.props;
		return onSubmit({qdata, version});
	}
}

export default SoftwareProductComponentsNetworkView;
