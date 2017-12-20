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
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

import Form from 'nfvo-components/input/validation/Form.jsx';
import Input from'nfvo-components/input/validation/Input.jsx';

import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';

const prefix = 'highAvailabilityAndLoadBalancing/';

const pointers = [
	{
		key: 'failureLoadDistribution',
		description: 'How is load distributed across live vms in the event of a vm/host failure? please describe'
	},
	{
		key: 'nkModelImplementation',
		description: 'Does each VM implement the N+K model for redundancy and failure protection? Please describe.'
	},
	{
		key: 'architectureChoice',
		description: 'What architecture is being implemented: ACTIVE-ACTIVE and/or ACTIVE-PASSIVE. ',
		added: 'Will the arrangement be 1-1 or N-M? Please describe.'
	},
	{key: 'slaRequirements', description: 'Specify application SLA requirements on Cloud platform.'},
	{
		key: 'horizontalScaling',
		description: 'Is horizontal scaling the preferred solution for HA and resiliency? Please describe.'
	},
	{
		key: 'loadDistributionMechanism',
		description: 'Can load be distributed across VMs? If so, are special mechanisms needed to re-balance data across VMs?',
		added: 'Please describe.'
	}
];
//TODO check for buttons

const TextAreaItem = ({item, toggle, expanded, genericFieldInfo, dataMap, onQDataChanged}) => (
		<GridItem colSpan={3} key={item.key} >
			<div className={expanded ? 'title' : 'title add-padding'}
				 data-test-id={`btn-${item.key}`}
				 onClick={() => toggle(item.key)}>
					<SVGIcon name={expanded ? 'chevronUp' : 'chevronDown'}/>
					<span className='title-text'>{i18n(item.description)}</span>
					{item.added && <div className='new-line'>{i18n(item.added)}</div>}
			</div>
			<div className={expanded ? 'collapse in' : 'collapse'}>
				<div>
					<div>
						<Input
							data-test-id={`input-${item.key}`}
							type='textarea'
							isValid={genericFieldInfo[`${prefix}${item.key}`].isValid}
							errorText={genericFieldInfo[`${prefix}${item.key}`].errorText}
							value={dataMap[`${prefix}${item.key}`]}
							onChange={(val) => onQDataChanged({[`${prefix}${item.key}`] : val})} />
					</div>
				</div>
			</div>
		</GridItem>
);

class SoftwareProductComponentLoadBalancingView extends React.Component {
	static propTypes = {
		componentId: PropTypes.string.isRequired,
		softwareProductId: PropTypes.string.isRequired,
		qdata: PropTypes.object,
		qschema: PropTypes.object,
		currentSoftwareProduct: PropTypes.object
	};

	state = {
		expanded: {}
	};

	render() {
		let {dataMap, genericFieldInfo, onQDataChanged, isReadOnlyMode} = this.props;
		return (
			<div className='vsp-components-load-balancing'>
				<div className='halb-data'>
					{ genericFieldInfo && <Form
						formReady={null}
						isValid={true}
						onSubmit={() => this.save()}
						isReadOnlyMode={isReadOnlyMode}
						hasButtons={false}>
						<GridSection title={i18n('High Availability & Load Balancing')}>
							<GridItem colSpan={1}>
								<Input
									data-test-id='input-is-component-mandatory'
									label={i18n('Is Component Mandatory')}
									type='select'
									className='input-options-select'
									groupClassName='bootstrap-input-options'
									isValid={genericFieldInfo[`${prefix}isComponentMandatory`].isValid}
									errorText={genericFieldInfo[`${prefix}isComponentMandatory`].errorText}
									value={dataMap[`${prefix}isComponentMandatory`]}
									onChange={(e) => {
										const selectedIndex = e.target.selectedIndex;
										const val = e.target.options[selectedIndex].value;
										onQDataChanged({[`${prefix}isComponentMandatory`] : val});}
									}>
									 <option key='placeholder' value=''>{i18n('Select...')}</option>
									{ genericFieldInfo[`${prefix}isComponentMandatory`].enum.map(isMan => <option value={isMan.enum} key={isMan.enum}>{isMan.title}</option>) }
								</Input>
							</GridItem>
							<GridItem colSpan={3}/>
							<GridItem colSpan={1}>
								<Input
									data-test-id='input-high-availability-mode'
									label={i18n('High Availability Mode')}
									type='select'
									className='input-options-select'
									groupClassName='bootstrap-input-options'
									isValid={genericFieldInfo[`${prefix}highAvailabilityMode`].isValid}
									errorText={genericFieldInfo[`${prefix}highAvailabilityMode`].errorText}
									value={dataMap[`${prefix}highAvailabilityMode`]}
									onChange={(e) => {
										const selectedIndex = e.target.selectedIndex;
										const val = e.target.options[selectedIndex].value;
										onQDataChanged({[`${prefix}highAvailabilityMode`] : val});}
									}>
									<option key='placeholder' value=''>{i18n('Select...')}</option>
									{genericFieldInfo[`${prefix}highAvailabilityMode`].enum.map(hmode => <option value={hmode.enum} key={hmode.enum}>{hmode.title}</option>)}
								</Input>
							</GridItem>
							<GridItem colSpan={3}/>
						</GridSection>
						<GridSection>
						{pointers.map(pointer => <TextAreaItem onQDataChanged={onQDataChanged}
							   genericFieldInfo={genericFieldInfo} dataMap={dataMap} item={pointer} key={pointer.key + 'pKey'}
							   expanded={this.state.expanded[pointer.key]} toggle={(name)=>{this.toggle(name);}} />)}
						</GridSection>
					</Form> }
				</div>
			</div>
		);
	}

	toggle(name) {
		let st = this.state.expanded[name] ? true : false;
		let newState = {...this.state};
		newState.expanded[name] = !st;
		this.setState(newState);
	}

	save() {
		let {onSubmit, qdata} = this.props;
		return onSubmit({qdata});
	}
}

export default SoftwareProductComponentLoadBalancingView;
