import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Input from 'nfvo-components/input/validation/Input.jsx';
import Form from 'nfvo-components/input/validation/Form.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import SelectInput from 'nfvo-components/input/SelectInput.jsx';
import SelectActionTable from 'nfvo-components/table/SelectActionTable.jsx';
import SelectActionTableRow from 'nfvo-components/table/SelectActionTableRow.jsx';
import SelectActionTableCell from 'nfvo-components/table/SelectActionTableCell.jsx';
import Validator from 'nfvo-utils/Validator.js';

export default class SoftwareProductDeploymentEditorView extends React.Component {
	render() {
		let {data, isEdit, onClose, onDataChanged, isReadOnlyMode, selectedFeatureGroupsList, componentsList, computesList, genericFieldInfo} = this.props;
		let {model, description, featureGroupId, componentComputeAssociations = []} = data;
		let featureGroupsExist = selectedFeatureGroupsList.length > 0;
		return (
			<div>
				{genericFieldInfo && <Form
					ref='validationForm'
					hasButtons={true}
					labledButtons={true}
					isReadOnlyMode={isReadOnlyMode}
					onSubmit={ () => this.submit() }
					submitButtonText={isEdit ? i18n('Save') : i18n('Create')}
					onReset={ () => onClose() }
					onValidateForm={() => this.validate() }
					isValid={this.props.isFormValid}
					formReady={this.props.formReady}
					className='vsp-deployment-editor'>
					<GridSection hasLastColSet>
						<GridItem colSpan={1}>
							<Input
								onChange={model => onDataChanged({model}, {model: model => this.validateName(model)})}
								label={i18n('Model')}
								value={model}
								data-test-id='deployment-model'
								isValid={genericFieldInfo.model.isValid}
								errorText={genericFieldInfo.model.errorText}
								isRequired={true}
								type='text'/>
						</GridItem>
						<GridItem colSpan={3} lastColInRow>
							<Input
								onChange={description => onDataChanged({description})}
								label={i18n('Description')}
								value={description}
								data-test-id='deployment-description'
								isValid={genericFieldInfo.description.isValid}
								errorText={genericFieldInfo.description.errorText}
								type='text'/>
						</GridItem>
					</GridSection>
					<GridSection className={`deployment-feature-groups-section${!featureGroupsExist ? ' no-feature-groups' : ''}`} title={i18n('License Details')} hasLastColSet>
						<GridItem colSpan={1}>
							<SelectInput
								data-test-id='deployment-feature-groups'
								label={i18n('Feature Group')}
								value={featureGroupId}
								onChange={featureGroup => onDataChanged({featureGroupId: featureGroup ? featureGroup.value : null})}
								type='select'
								clearable={true}
								disabled={isReadOnlyMode || !featureGroupsExist}
								className='field-section'
								options={selectedFeatureGroupsList}/>
						</GridItem>
					</GridSection>
					{!featureGroupsExist && <GridSection className='deployment-feature-group-warning-section'>
						<GridItem colSpan={3}>
							<span>{i18n('Please assign Feature Groups in VSP General')}</span>
						</GridItem>
					</GridSection>}
					<GridSection title={i18n('Assign VFCs and Compute Flavors')} className='vfc-table' hasLastColSet>
						<GridItem colSpan={4} lastColInRow>
							<SelectActionTable
								columns={['Virtual Function Components', 'Compute Flavors']}
								numOfIcons={0}>
								{componentComputeAssociations.map( (association, index) =>
									<SelectActionTableRow key={association.componentId}>
										<SelectActionTableCell
											options={
												componentsList
												.map(component => ({value: component.id, label: component.displayName}) )
											}
											selected={association.componentId}
											onChange={componentId => {
												let newAssociations = [...componentComputeAssociations];
												newAssociations[index] = {...newAssociations[index], componentId};
												onDataChanged({componentComputeAssociations: newAssociations});
											}}
											disabled={true}/>
										<SelectActionTableCell
											options={
												computesList
												.filter(compute => compute.componentId === association.componentId)
												.map(compute => ({value: compute.computeFlavorId, label: compute.name}) )
											}
											selected={association.computeFlavorId}
											onChange={computeFlavorId => {
												let newAssociations = [...componentComputeAssociations];
												newAssociations[index] = {...newAssociations[index], computeFlavorId};
												onDataChanged({componentComputeAssociations: newAssociations});
											}}
											disabled={isReadOnlyMode}/>
									</SelectActionTableRow>
								)}
							</SelectActionTable>
						</GridItem>
					</GridSection>
				</Form>}
			</div>
		);
	}

	validateName(value) {
		const {data: {id = ''}, DFNames} = this.props;
		const isExists = Validator.isItemNameAlreadyExistsInList({itemId: id, itemName: value, list: DFNames});

		return !isExists ?  {isValid: true, errorText: ''} :
			{isValid: false, errorText: i18n('Deployment flavor by the name \'' + value + '\' already exists. Deployment flavor name must be unique')};
	}

	submit(){
		let {isEdit, onCreate, onEdit, onClose, data} = this.props;
		if (isEdit) {
			onEdit(data);
		} else {
			onCreate(data);
		}
		onClose();
	}

	validate() {
		this.props.onValidateForm();
	}
}
