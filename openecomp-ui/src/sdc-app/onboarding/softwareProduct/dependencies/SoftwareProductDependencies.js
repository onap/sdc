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
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import SoftwareProductDependenciesActionHelper from './SoftwareProductDependenciesActionHelper.js';

export const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductEditor: {data: currentSoftwareProduct = {}}, softwareProductDependencies, softwareProductComponents: {componentsList}} = softwareProduct;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(currentSoftwareProduct);
	return {
		isReadOnlyMode,
		softwareProductDependencies: softwareProductDependencies.length ? softwareProductDependencies : [{sourceId: '', targetId: '', relationType: 'dependsOn', id: 'fake'}],
		componentsOptions: componentsList.map(component => ({value: component.id, label: component.name}))
	};
};

const mapActionsToProps = (dispatch, {softwareProductId, version}) => {
	return {
		onDataChanged: dependenciesList => SoftwareProductDependenciesActionHelper.updateDependencyList(dispatch, {dependenciesList}),
		onAddDependency: () => SoftwareProductDependenciesActionHelper.addDependency(dispatch),
		onSubmit: (dependenciesList) => SoftwareProductDependenciesActionHelper.saveDependencies(dispatch, {softwareProductId, version, dependenciesList})
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductDependenciesView);
