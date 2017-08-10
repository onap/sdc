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
import React, {Component} from 'react';
import Button from 'sdc-ui/lib/react/Button.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import FormControl from 'react-bootstrap/lib/FormControl.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SelectInput from 'nfvo-components/input/SelectInput.jsx';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import {fileTypes} from './HeatSetupConstants.js';
import {tabsMapping} from '../SoftwareProductAttachmentsConstants.js';
import {sortable} from 'react-sortable';

class ListItem extends Component {

	render() {
		return (
			<li {...this.props}>{this.props.children}</li>
		);
	}
}


const SortableListItem = sortable(ListItem);

class SortableModuleFileList extends Component {

	state = {
		draggingIndex: null,
		data: this.props.modules
	};


	componentWillReceiveProps(nextProps) {
		this.setState({data: nextProps.modules});
	}

	render() {

		let {unassigned, onModuleRename, onModuleDelete, onModuleAdd, onBaseAdd, onModuleFileTypeChange, isBaseExist, isReadOnlyMode} = this.props;
		const childProps = module => ({
			module,
			onModuleRename,
			onModuleDelete,
			onModuleFileTypeChange: (value, type) => onModuleFileTypeChange({module, value, type}),
			files: unassigned
		});
		let listItems = this.state.data.map(function (item, i) {
			return (
				<SortableListItem
					key={i}
					updateState={data => this.setState(data)}
					items={this.state.data}
					draggingIndex={this.state.draggingIndex}
					sortId={i}
					outline='list'><ModuleFile {...childProps(item)} isReadOnlyMode={this.props.isReadOnlyMode} /></SortableListItem>
			);
		}, this);

		return (
			<div className={`modules-list-wrapper ${(listItems.length > 0) ? 'modules-list-wrapper-divider' : ''}`}>
				<div className='modules-list-header'>
					{!isBaseExist && <div><Button btnType='link' onClick={onBaseAdd} disabled={isReadOnlyMode || unassigned.length === 0}>{i18n('Add Base')}</Button></div>}
					<div><Button btnType='link' onClick={onModuleAdd} disabled={isReadOnlyMode || unassigned.length === 0}>{i18n('Add Module')}</Button></div>
				</div>
				{(listItems.length > 0) && <ul>{listItems}</ul>}
			</div>
		);
	}
}

const tooltip = (name) => <Tooltip id='tooltip-bottom'>{name}</Tooltip>;
const UnassignedFileList = (props) => {
	return (
		<div>
			<div className='modules-list-header'/>
				<div className='unassigned-files'>
			<div className='unassigned-files-title'>{i18n('UNASSIGNED FILES')}</div>
			<div className='unassigned-files-list'>{props.children}</div>
			</div>
		</div>
	);
};

const EmptyListContent = props => {
	let {onClick, heatDataExist, isReadOnlyMode} = props;
	let displayText = heatDataExist ? 'All Files Are Assigned' : '';
	return (
		<div className='go-to-validation-button-wrapper'>
			<div className='all-files-assigned'>{i18n(displayText)}</div>
			{heatDataExist && <SVGIcon disabled={isReadOnlyMode} name='angleRight' onClick={onClick}
				data-test-id='go-to-validation' label={i18n('Proceed To Validation')} labelPosition='left' color='primary'/>}
		</div>
	);
};
const UnassignedFile = (props) => (
	<OverlayTrigger placement='bottom' overlay={tooltip(props.name)} delayShow={1000}>
		<li data-test-id='unassigned-files' className='unassigned-files-list-item'>{props.name}</li>
	</OverlayTrigger>
);

const AddOrDeleteVolumeFiles = ({add = true, onAdd, onDelete, isReadOnlyMode}) => {
	const displayText = add ? 'Add Volume Files' : 'Delete Volume Files';
	const action = add ? onAdd : onDelete;
	return (
		<Button disabled={isReadOnlyMode} onClick={action} btnType='link' className='add-or-delete-volumes' iconName={add ? 'plus' : 'close'}>{i18n(displayText)}</Button>
	);
};

const SelectWithFileType = ({type, selected, files, onChange}) => {

	let filteredFiledAccordingToType = files.filter(file => file.label.search(type.regex) > -1);
	if (selected) {
		filteredFiledAccordingToType = filteredFiledAccordingToType.concat({label: selected, value: selected});
	}

	return (
		<SelectInput
			data-test-id={`${type.label}-list`}
			label={type.label}
			value={selected}
			onChange={value => value !== selected && onChange(value, type.label)}
			disabled={filteredFiledAccordingToType.length === 0}
			placeholder={filteredFiledAccordingToType.length === 0 ? '' : undefined}
			clearable={true}
			options={filteredFiledAccordingToType} />
	);
};

class NameEditInput extends Component {
	componentDidMount() {
		this.input.focus();
	}

	render() {
		return (
			<FormControl {...this.props} className='name-edit' inputRef={input => this.input = input}/>
		);
	}
}

class ModuleFile extends Component {
	constructor(props) {
		super(props);
		this.state = {
			isInNameEdit: false,
			displayVolumes: Boolean(props.module.vol || props.module.volEnv)
		};
	}

	handleSubmit(event, name) {
		if (event.keyCode === 13) {
			this.handleModuleRename(event, name);
		}
	}

	componentWillReceiveProps(nextProps) {
		this.setState({displayVolumes: Boolean(nextProps.module.vol || nextProps.module.volEnv)});
	}

	handleModuleRename(event, name) {
		this.setState({isInNameEdit: false});
		this.props.onModuleRename(name, event.target.value);
	}

	deleteVolumeFiles() {
		const { onModuleFileTypeChange} = this.props;
		onModuleFileTypeChange(null, fileTypes.VOL.label);
		onModuleFileTypeChange(null, fileTypes.VOL_ENV.label);
		this.setState({displayVolumes: false});
	}

	renderNameAccordingToEditState() {
		const {module: {name}} = this.props;
		if (this.state.isInNameEdit) {
			return (<NameEditInput defaultValue={name} onBlur={evt => this.handleModuleRename(evt, name)} onKeyDown={evt => this.handleSubmit(evt, name)}/>);
		}
		return (<span className='filename-text'>{name}</span>);
	}

	render() {
		const {module: {name, isBase, yaml, env, vol, volEnv}, onModuleDelete, files, onModuleFileTypeChange, isReadOnlyMode} = this.props;
		const {displayVolumes} = this.state;
		const moduleType = isBase ? 'BASE' : 'MODULE';
		return (
			<div className='modules-list-item' data-test-id='module-item'>
				<div className='modules-list-item-controllers'>
					<div className='modules-list-item-filename'>
						<SVGIcon name={isBase ? 'base' : 'module'} color='primary' iconClassName='heat-setup-module-icon' />
						<span className='module-title-by-type'>{`${moduleType}: `}</span>
						<div className={`text-and-icon ${this.state.isInNameEdit ? 'in-edit' : ''}`}>
							{this.renderNameAccordingToEditState()}
							{!this.state.isInNameEdit && <SVGIcon
								name='pencil'
								onClick={() => this.setState({isInNameEdit: true})}
								data-test-id={isBase ? 'base-name' : 'module-name'}/>}
						</div>
					</div>
					<SVGIcon name='trashO' onClick={() => onModuleDelete(name)} data-test-id='module-delete'/>
				</div>
				<div className='modules-list-item-selectors'>
					<SelectWithFileType
						type={fileTypes.YAML}
						files={files}
						selected={yaml}
						onChange={onModuleFileTypeChange}/>
					<SelectWithFileType
						type={fileTypes.ENV}
						files={files}
						selected={env}
						onChange={onModuleFileTypeChange}/>
					{displayVolumes  && <SelectWithFileType
						type={fileTypes.VOL}
						files={files}
						selected={vol}
						onChange={onModuleFileTypeChange}/>}
					{displayVolumes && <SelectWithFileType
						type={fileTypes.VOL_ENV}
						files={files}
						selected={volEnv}
						onChange={onModuleFileTypeChange}/>}
					<AddOrDeleteVolumeFiles isReadOnlyMode={isReadOnlyMode} onAdd={() => this.setState({displayVolumes: true})} onDelete={() => this.deleteVolumeFiles()} add={!displayVolumes}/>
				</div>
			</div>
		);
	}
}

class ArtifactOrNestedFileList extends Component {

	render() {
		let {type, title, selected, options, onSelectChanged, onAddAllUnassigned, isReadOnlyMode, headerClassName} = this.props;
		return (
			<div className={`artifact-files ${type === 'nested' ? 'nested' : ''} ${headerClassName} `}>
				<div className='artifact-files-header'>
					<span>
						{type === 'artifact' && (<SVGIcon color='primary' name='artifacts' iconClassName='heat-setup-module-icon' />)}
						{`${title}`}
					</span>
					{type === 'artifact' && <Button disabled={isReadOnlyMode} btnType='link' className='add-all-unassigned' onClick={onAddAllUnassigned}>{i18n('Add All Unassigned Files')}</Button>}
				</div>
				{type === 'nested' ? (
					<ul className='nested-list'>{selected.map(nested =>
						<li key={nested} className='nested-list-item'>{nested}</li>
					)}</ul>) :
					(<SelectInput
						options={options}
						onMultiSelectChanged={onSelectChanged || (() => {
						})}
						value={selected}
						clearable={false}
						placeholder={i18n('Add Artifact')}
						multi/>)
				}
			</div>
		);
	}
}

const buildLabelValueObject = str => (typeof str === 'string' ? {value: str, label: str} : str);

class SoftwareProductHeatSetupView extends Component {

	processAndValidateHeat(heatData, heatDataCache){
		let {onProcessAndValidate, changeAttachmentsTab, version} = this.props;
		onProcessAndValidate({heatData, heatDataCache, version}).then(
			() => changeAttachmentsTab(tabsMapping.VALIDATION)
		);
	}

	render() {
		let {modules, heatSetupCache, isReadOnlyMode, heatDataExist, unassigned, artifacts, nested, onArtifactListChange, onAddAllUnassigned} = this.props;

		const formattedUnassigned = unassigned.map(buildLabelValueObject);
		const formattedArtifacts = artifacts.map(buildLabelValueObject);
		return (
			<div className={`heat-setup-view ${isReadOnlyMode ? 'disabled' : ''}`}>
				<div className='heat-setup-view-modules-and-artifacts'>
					<SortableModuleFileList
						{...this.props}
						isReadOnlyMode={this.props.isReadOnlyMode}
						artifacts={formattedArtifacts}
						unassigned={formattedUnassigned}/>
					<ArtifactOrNestedFileList
						type={'artifact'}
						title={i18n('ARTIFACTS')}
						options={formattedUnassigned}
						selected={formattedArtifacts}
						onSelectChanged={onArtifactListChange}
						isReadOnlyMode={this.props.isReadOnlyMode}
						headerClassName={(modules && modules.length > 0) ? 'with-list-items' : ''}
						onAddAllUnassigned={onAddAllUnassigned}/>
					<ArtifactOrNestedFileList
						type={'nested'}
						title={i18n('NESTED HEAT FILES')}
						options={[]}
						isReadOnlyMode={this.props.isReadOnlyMode}
						selected={nested}/>
				</div>
				<UnassignedFileList>
					{
						formattedUnassigned.length > 0 ?
						(<ul>{formattedUnassigned.map(file => <UnassignedFile key={file.label} name={file.label}/>)}</ul>)
						:
						(<EmptyListContent
							heatDataExist={heatDataExist}
							isReadOnlyMode={this.props.isReadOnlyMode}
							onClick={() => this.processAndValidateHeat({modules, unassigned, artifacts, nested}, heatSetupCache)}/>)
					}
				</UnassignedFileList>
			</div>
		);
	}

}

export default SoftwareProductHeatSetupView;
