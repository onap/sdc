import React from 'react';
import FontAwesome from 'react-fontawesome';
import i18n from 'nfvo-utils/i18n/i18n.js';

import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';
import ValidationInput from'nfvo-components/input/validation/ValidationInput.jsx';

const prefix = '/highAvailabilityAndLoadBalancing/';

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

class SoftwareProductComponentLoadBalancingView extends React.Component {
	static propTypes = {
		componentId: React.PropTypes.string.isRequired,
		softwareProductId: React.PropTypes.string.isRequired,
		qdata: React.PropTypes.object,
		qschema: React.PropTypes.object,
		currentSoftwareProduct: React.PropTypes.object
	};

	state = {
		expanded: {}
	};

	renderTextAreaItem(item) {
		return (
			<div className='question'>
				<div className={this.state.expanded[item.key] ? 'title' : 'title add-padding'}
					 onClick={() => this.toggle(item.key)}>
					<FontAwesome name={this.state.expanded[item.key] ? 'chevron-up' : 'chevron-down'}/>
					{i18n(item.description)}
					{item.added && <div className='new-line'>{i18n(item.added)}</div>}
				</div>
				<div className={this.state.expanded[item.key] ? 'collapse in' : 'collapse'}>
					<div className='row'>
						<div className='col-md-9'>
							<ValidationInput
								type='textarea'
								pointer={`${prefix}${item.key}`}
								maxLength='1000' />
						</div>
					</div>
				</div>
			</div>
		);
	}

	render() {
		let {qdata, qschema, onQDataChanged, isReadOnlyMode} = this.props;
		return (
			<div className='vsp-components-load-balancing'>
				<div className='halb-data'>
					<div className='load-balancing-page-title'>{i18n('High Availability & Load Balancing')}</div>
					<ValidationForm
						onDataChanged={onQDataChanged}
						data={qdata} schema={qschema}
						isReadOnlyMode={isReadOnlyMode}
						hasButtons={false}>
						{pointers.map(pointer => this.renderTextAreaItem(pointer))}
					</ValidationForm>
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
