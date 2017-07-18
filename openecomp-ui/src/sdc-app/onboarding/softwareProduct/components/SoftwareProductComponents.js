import {connect} from 'react-redux';
import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';

import SoftwareProductComponentsList from './SoftwareProductComponentsList.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import SoftwareProductComponentsActionHelper from '../components/SoftwareProductComponentsActionHelper.js';
import {onboardingMethod} from '../SoftwareProductConstants.js';
import ConfirmationModalConstants from 'nfvo-components/modal/GlobalModalConstants.js';

const generateMessage = (name) => {
	return i18n(`Are you sure you want to delete ${name}?`);
};

const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductEditor: {data: currentSoftwareProduct}, softwareProductComponents} = softwareProduct;
	let {componentsList} = softwareProductComponents;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(currentSoftwareProduct);

	return {
		currentSoftwareProduct,
		isReadOnlyMode,
		componentsList,
		isManual: currentSoftwareProduct.onboardingMethod === onboardingMethod.MANUAL

	};
};

class SoftwareProductComponentsView extends React.Component {
	render() {
		let {currentSoftwareProduct, isReadOnlyMode, componentsList, isManual, onDeleteComponent} = this.props;
		return (
			<SoftwareProductComponentsList
				isReadOnlyMode={isReadOnlyMode}
				componentsList={componentsList}
				onDeleteComponent={onDeleteComponent}
				isManual={isManual}
				currentSoftwareProduct={currentSoftwareProduct}/>);
	}

}

const mapActionToProps = (dispatch) => {
	return {
		onComponentSelect: ({id: softwareProductId, componentId, version}) => {
			OnboardingActionHelper.navigateToSoftwareProductComponentGeneralAndUpdateLeftPanel(dispatch, {softwareProductId, componentId, version });
		},
		onAddComponent: (softwareProductId) => SoftwareProductComponentsActionHelper.addComponent(dispatch, {softwareProductId}),
		onDeleteComponent: (component, softwareProductId, version) => dispatch({
			type: ConfirmationModalConstants.GLOBAL_MODAL_WARNING,
			data:{
				msg: generateMessage(component.displayName),
				onConfirmed: ()=>SoftwareProductComponentsActionHelper.deleteComponent(dispatch,
					{
						softwareProductId,
						componentId: component.id,
						version
					})
			}
		})
	};
};

export default connect(mapStateToProps, mapActionToProps, null, {withRef: true})(SoftwareProductComponentsView);
