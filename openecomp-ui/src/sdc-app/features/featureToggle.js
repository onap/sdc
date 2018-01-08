/*!
 * Copyright © 2016-2017 European Support Limited
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


 /**
 * Feature toggling decorator
 * 	usage: 
 * 
 * @featureToggle('FeatureName')
 * class Example extends React.Component {
 * 		render() {
 * 			return (<div>test feature</div>);
 * 		}
 * }
 * 
 *  OR 
 * 
 * const TestFeature = () => (<div>test feature</div>)
 * export default featureToggle('FeatureName')(TestFeature)
 * 
 */

import React from 'react';
import PropTypes from 'prop-types';
import {connect} from 'react-redux';

export const FeatureComponent = ({features = [], featureName, InnerComponent}) => {	
	return !!features.find(el => el.name === featureName && el.active) ? <InnerComponent/> : null;
};

FeatureComponent.propTypes = {
	features: PropTypes.array,
	featureName: PropTypes.string.isRequired
};


export default function featureToggle(featureName) {
	return (InnerComponent) => {		
		return connect(({features}) => {return {features, featureName, InnerComponent};})(FeatureComponent);
	};
}

