import React, {Component} from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Input from 'nfvo-components/input/validation/ValidationInput.jsx';
import Form from 'nfvo-components/input/validation/ValidationForm.jsx';

class FlowsEditorModalView extends Component {

	render() {
		let {onCancel, onDataChanged, currentFlow} = this.props;
		let {artifactName, description} = currentFlow;
		return (
			<Form onSubmit={() => this.onSaveClicked()} onReset={onCancel}>
				<Input
					type='text'
					name='name'
					label={i18n('Name')}
					validations={{required: true}}
					value={artifactName}
					onChange={artifactName => onDataChanged({artifactName})}/>
				<Input
					type='textarea'
					name='description'
					label={i18n('Description')}
					validations={{required: true}}
					value={description}
					onChange={description => onDataChanged({description})}/>
			</Form>
		);
	}

	onSaveClicked() {
		let {currentFlow, onSubmit} = this.props;
		if (onSubmit) {
			onSubmit(currentFlow);
		}
	}

}

export default FlowsEditorModalView;
