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
import HeatSetupView  from '../onboarding/softwareProduct/attachments/setup/HeatSetupView.jsx';
import UploadScreenActionHelper from './UploadScreenActionHelper.js';
import {mapStateToProps, mapActionsToProps} from '../onboarding/softwareProduct/attachments/setup/HeatSetup.js';

const mapActionsToPropsExt = (dispatch) => {
	return {
		...mapActionsToProps(dispatch,{}),
		onProcessAndValidate: (heatData, heatDataCache) => UploadScreenActionHelper.processAndValidateHeat(dispatch, heatData, heatDataCache)
	};
};

export default connect(mapStateToProps, mapActionsToPropsExt, null, {withRef: true})(HeatSetupView);
