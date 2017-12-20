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
import {relationTypesOptions, NEW_RULE_TEMP_ID} from './SoftwareProductDependenciesConstants.js';


const TableActionRow = ({onAction, actionIcon, showAction, dependency, sourceOptions, targetOptions, onDataChanged}) => {
	return (
		<SelectActionTableRow
			key={dependency.id}
			onAction={onAction}
			overlayMsg={i18n('There is a loop between selections')}
			hasError={dependency.hasCycle}
			hasErrorIndication
			showAction={showAction}
			actionIcon={actionIcon}>
			<SelectActionTableCell
				options={sourceOptions}
				selected={dependency.sourceId}
				placeholder={i18n('Select VFC...')}
				clearable={false}
				onChange={newVal =>  {
					dependency.sourceId = newVal;
					onDataChanged(dependency);
				}} />
			<SelectActionTableCell options={relationTypesOptions} selected={dependency.relationType} clearable={false}/>
			<SelectActionTableCell
				placeholder={i18n('Select VFC...')}
				options={targetOptions}
				selected={dependency.targetId}
				clearable={false}
				onChange={newVal =>  {
					dependency.targetId = newVal;
					onDataChanged(dependency);
				}} />
		</SelectActionTableRow>
	);
};


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
		let {componentsOptions, softwareProductDependencies, onDataChanged, onAddDependency, onDeleteDependency, isReadOnlyMode} = this.props;
		let sourceToTargetMapping = {};
		softwareProductDependencies.map(dependency => {
			let isInMap = sourceToTargetMapping.hasOwnProperty(dependency.sourceId);
			if (dependency.targetId) {
				sourceToTargetMapping[dependency.sourceId] = isInMap ? [...sourceToTargetMapping[dependency.sourceId], dependency.targetId] : [dependency.targetId];
			}
		});
		let depList = softwareProductDependencies.filter(dependency => dependency.id !== NEW_RULE_TEMP_ID);
		let newDependency = softwareProductDependencies.find(dependency => dependency.id === NEW_RULE_TEMP_ID);
		return (
			<div className='software-product-dependencies'>
				<div className='page-title'>{i18n('Dependencies')}</div>
				<SelectActionTable
					columns={[i18n('Source'), i18n('Relation Type'), i18n('Target')]}
					numOfIcons={2}
					isReadOnlyMode={isReadOnlyMode}>
					{!isReadOnlyMode && <TableActionRow
						key={newDependency.id}
						actionIcon='plusCircle'
						onAction={() => onAddDependency(newDependency)}
						dependency={newDependency}
						componentsOptions={componentsOptions}
						sourceToTargetMapping={sourceToTargetMapping}
						onDataChanged={onDataChanged}
						sourceOptions={this.filterSources({componentsOptions, sourceToTargetMapping, selectedSourceId: newDependency.sourceId, selectedTargetId: newDependency.targetId})}
						targetOptions={this.filterTargets({componentsOptions, sourceToTargetMapping, selectedSourceId: newDependency.sourceId, selectedTargetId: newDependency.targetId})}
						showAction={newDependency.targetId !== null && newDependency.relationType !== null && newDependency.sourceId !== null}/> }
					{depList.map(dependency => (
						<TableActionRow
							key={dependency.id}
							actionIcon='trashO'
							onAction={() => onDeleteDependency(dependency)}
							dependency={dependency}
							componentsOptions={componentsOptions}
							sourceToTargetMapping={sourceToTargetMapping}
							sourceOptions={this.filterSources({componentsOptions, sourceToTargetMapping, selectedSourceId: dependency.sourceId, selectedTargetId: dependency.targetId})}
							targetOptions={this.filterTargets({componentsOptions, sourceToTargetMapping, selectedSourceId: dependency.sourceId, selectedTargetId: dependency.targetId})}
							onDataChanged={onDataChanged}
							showAction={true}/>
					))}
				</SelectActionTable>
			</div>
		);
	}

}
