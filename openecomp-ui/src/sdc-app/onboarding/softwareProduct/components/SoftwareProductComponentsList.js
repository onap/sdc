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
import {connect} from 'react-redux';
import i18n from 'nfvo-utils/i18n/i18n.js';

import SoftwareProductComponentsListView from './SoftwareProductComponentsListView.jsx';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import SoftwareProductComponentsActionHelper from '../components/SoftwareProductComponentsActionHelper.js';
import {actionTypes as globalModalActions} from 'nfvo-components/modal/GlobalModalConstants.js';

const generateMessage = (name) => {
	return i18n(`Are you sure you want to delete ${name}?`);
};


const mapActionToProps = (dispatch) => {
	return {
		onComponentSelect: ({id: softwareProductId, componentId, version}) => {
			OnboardingActionHelper.navigateToSoftwareProductComponentGeneralAndUpdateLeftPanel(dispatch, {softwareProductId, componentId, version });
		},
		onAddComponent: (softwareProductId) => SoftwareProductActionHelper.addComponent(dispatch, {softwareProductId, modalClassName: 'create-vfc-modal'}),
		onDeleteComponent: (component, softwareProductId, version) => dispatch({
			type: globalModalActions.GLOBAL_MODAL_WARNING,
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

export default connect(null, mapActionToProps, null, {withRef: true})(SoftwareProductComponentsListView);
