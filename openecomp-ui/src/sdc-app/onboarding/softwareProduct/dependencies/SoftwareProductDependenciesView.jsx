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

import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';

import SelectActionTable from 'nfvo-components/table/SelectActionTable.jsx';
import SelectActionTableRow from 'nfvo-components/table/SelectActionTableRow.jsx';
import SelectActionTableCell from 'nfvo-components/table/SelectActionTableCell.jsx';
import {relationTypesOptions} from './SoftwareProductDependenciesConstants.js';

export default class SoftwareProductDependenciesView extends React.Component {
	filterTargets({componentsOptions, sourceToTargetMapping, selectedSourceId, selectedTargetId}) {
		let isInMap = sourceToTargetMapping.hasOwnProperty(selectedSourceId);
		return componentsOptions.filter(component => {
			if (component.value === selectedTargetId) {
				return true;
			} else {
				return component.value !== selectedSourceId && (isInMap ? sourceToTargetMapping[selectedSourceId].indexOf(component.value) < 0 : true);
			}
		});
	}

	filterSources({componentsOptions, sourceToTargetMapping, selectedSourceId, selectedTargetId}) {
		return componentsOptions.filter(component => {
			if (component.value === selectedSourceId) {
				return true;
			} else {
				let isInMap = sourceToTargetMapping.hasOwnProperty(component.value);
				return component.value !== selectedTargetId && (isInMap ? sourceToTargetMapping[component.value].indexOf(selectedTargetId) < 0 : true);
			}
		});
	}

	render() {
		let {componentsOptions, softwareProductDependencies, onDataChanged, onAddDependency, isReadOnlyMode} = this.props;
		let canAdd = softwareProductDependencies.length < componentsOptions.length * (componentsOptions.length - 1);
		let sourceToTargetMapping = {};
		softwareProductDependencies.map(dependency => {
			let isInMap = sourceToTargetMapping.hasOwnProperty(dependency.sourceId);
			if (dependency.targetId) {
				sourceToTargetMapping[dependency.sourceId] = isInMap ? [...sourceToTargetMapping[dependency.sourceId], dependency.targetId] : [dependency.targetId];
			}
		});
		return (
			<div className='software-product-dependencies'>
				<div className='software-product-dependencies-title'>{i18n('Dependencies')}</div>
				<SelectActionTable
					columns={['Source', 'Relation Type', 'Target']}
					isReadOnlyMode={isReadOnlyMode}
					onAdd={canAdd ? onAddDependency : undefined}
					onAddItem={i18n('Add Rule')}>					
					{softwareProductDependencies.map(dependency => (
						<SelectActionTableRow
							key={dependency.id}
							onDelete={() => onDataChanged(softwareProductDependencies.filter(currentDependency => currentDependency.id !== dependency.id))}
							overlayMsg={i18n('There is a loop between selections')}
							hasError={dependency.hasCycle}>
							<SelectActionTableCell
								options={this.filterSources({componentsOptions, sourceToTargetMapping, selectedSourceId: dependency.sourceId, selectedTargetId: dependency.targetId})}
								selected={dependency.sourceId}
								placeholder={i18n('Select VFC...')}
								onChange={newSourceId => onDataChanged(softwareProductDependencies.map(currentDependency =>
									({...currentDependency, sourceId: currentDependency.id === dependency.id ? newSourceId : currentDependency.sourceId})
								))} />
							<SelectActionTableCell options={relationTypesOptions} selected={dependency.relationType} clearable={false}/>
							<SelectActionTableCell
								placeholder={i18n('Select VFC...')}
								options={this.filterTargets({componentsOptions, sourceToTargetMapping, selectedSourceId: dependency.sourceId, selectedTargetId: dependency.targetId})}
								selected={dependency.targetId}
								onChange={newTargetId => onDataChanged(softwareProductDependencies.map(currentDependency =>
									({...currentDependency, targetId: currentDependency.id === dependency.id ? newTargetId : currentDependency.targetId})
								))} />
						</SelectActionTableRow>
					))}
				</SelectActionTable>
			</div>
		);
	}

	save() {
		let {onSubmit, softwareProductDependencies} = this.props;
		return onSubmit(softwareProductDependencies);
	}
}
