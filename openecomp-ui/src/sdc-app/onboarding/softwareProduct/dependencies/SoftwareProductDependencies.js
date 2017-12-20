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

import SoftwareProductDependenciesView from './SoftwareProductDependenciesView.jsx';
import SoftwareProductDependenciesActionHelper from './SoftwareProductDependenciesActionHelper.js';

export const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductDependencies, softwareProductComponents: {componentsList}} = softwareProduct;
	return {
		softwareProductDependencies: softwareProductDependencies,
		componentsOptions: componentsList.map(component => ({value: component.id, label: component.displayName}))
	};
};

const mapActionsToProps = (dispatch, {softwareProductId, version}) => {
	return {
		onDataChanged: (item) => SoftwareProductDependenciesActionHelper.updateDependency(dispatch, {softwareProductId, version, item}),
		onDeleteDependency: (item) => SoftwareProductDependenciesActionHelper.removeDependency(dispatch, {softwareProductId, version, item}),
		onAddDependency: (item) => SoftwareProductDependenciesActionHelper.createDependency(dispatch, {softwareProductId, version, item})
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductDependenciesView);
