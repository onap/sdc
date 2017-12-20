import {connect} from 'react-redux';
import i18n from 'nfvo-utils/i18n/i18n.js';

import SoftwareProductComponentsActionHelper from '../components/SoftwareProductComponentsActionHelper.js';
import {onboardingMethod as onboardingMethodTypes} from '../SoftwareProductConstants.js';
import ConfirmationModalConstants from 'nfvo-components/modal/GlobalModalConstants.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import {screenTypes} from 'sdc-app/onboarding/OnboardingConstants.js';
import SoftwareProductComponentsView from './SoftwareProductComponentsListView.jsx';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';

const generateMessage = (name) => {
	return i18n('Are you sure you want to delete {name}?', {name: name});
};

const mapStateToProps = ({softwareProduct, currentScreen: {props: {version}}}) => {
	let {softwareProductEditor: {data: currentSoftwareProduct = {}}, softwareProductComponents} = softwareProduct;
	let {componentsList} = softwareProductComponents;
	let {onboardingMethod = onboardingMethodTypes.HEAT} = currentSoftwareProduct;
	return {
		currentSoftwareProduct,
		componentsList,
		isManual: onboardingMethod === onboardingMethodTypes.MANUAL,
		version
	};
};

const mapActionToProps = (dispatch) => {
	return {
		onComponentSelect: ({id: softwareProductId, componentId, version}) =>
			ScreensHelper.loadScreen(dispatch, {
				screen: screenTypes.SOFTWARE_PRODUCT_COMPONENT_DEFAULT_GENERAL, screenType: screenTypes.SOFTWARE_PRODUCT,
				props: {softwareProductId, version, componentId}
			}),
		onAddComponent: (softwareProductId, version) => SoftwareProductActionHelper.addComponent(dispatch, {softwareProductId, version, modalClassName: 'create-vfc-modal'}),
		onDeleteComponent: (component, softwareProductId, version) => dispatch({
			type: ConfirmationModalConstants.GLOBAL_MODAL_WARNING,
			data:{
				msg: generateMessage(component.displayName),
				onConfirmed: ()=>SoftwareProductComponentsActionHelper.deleteComponent(dispatch, {
					softwareProductId,
					componentId: component.id,
					version
				})
			}
		})
	};
};

export default connect(mapStateToProps, mapActionToProps, null, {withRef: true})(SoftwareProductComponentsView);
