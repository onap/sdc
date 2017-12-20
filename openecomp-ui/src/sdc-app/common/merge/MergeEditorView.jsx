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
import union from 'lodash/union.js';
import Button from 'sdc-ui/lib/react/Button.js';
// import Checkbox from 'sdc-ui/lib/react/Checkbox.js';
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import Radio from 'sdc-ui/lib/react/Radio.js';
import equal from 'deep-equal';
import {ResolutionTypes} from './MergeEditorConstants.js';

class ConflictCategory extends React.Component  {
	state = {
		resolution: ResolutionTypes.YOURS
	};

	getTitle(conflictType, conflictName) {
		if (typeof conflictName === 'undefined' || conflictType === conflictName) {
			return i18n(conflictType);
		} else {
			return `${i18n(conflictType)}: ${conflictName}`;
		}
	}

	render() {
		let {collapseExpand, conflict: {id: conflictId, type, name}, isCollapsed, item: {id: itemId, version}, onResolveConflict} = this.props;
		let {resolution} = this.state;
		const iconClass = isCollapsed ? 'merge-chevron' : 'merge-chevron right';

		return (
			<div key={'conflictCategory_' + conflictId}  >
				<GridSection className='conflict-section'>
					<GridItem >
						<div className='collapsible-section' onClick={collapseExpand}>
							<SVGIcon name={isCollapsed ? 'chevronDown' : 'chevronUp'} iconClassName={iconClass} />
							<div className='conflict-title'>{this.getTitle(type, name)}</div>
						</div>
					</GridItem>
					<GridItem className='yours'>
						<Radio name={'radio_' + conflictId} checked={resolution === ResolutionTypes.YOURS} value='yours'
							   onChange={() => this.setState({resolution: ResolutionTypes.YOURS})} data-test-id={'radio_' + conflictId + '_yours'} />
					</GridItem>
					<GridItem className='theirs'>
						<Radio name={'radio_' + conflictId} checked={resolution === ResolutionTypes.THEIRS} value='theirs'
							   onChange={() => this.setState({resolution: ResolutionTypes.THEIRS})} data-test-id={'radio_' + conflictId + '_theirs'} /></GridItem>
					<GridItem className='resolve'>
						<Button className='conflict-resolve-btn' btnType='outline' color='gray'
							onClick={() => onResolveConflict({conflictId, resolution, itemId, version})}>
							{i18n('Resolve')}
						</Button>
					</GridItem>
				</GridSection>
				<div>
					{isCollapsed && this.props.children}
				</div>
			</div>
		);
	}

};

class TextCompare extends React.Component  {
	render() {
		// let rand = Math.random() * (3000 - 1) + 1;
		let {yours, theirs, field, type, isObjName, conflictsOnly} = this.props;
		let typeYours = typeof yours;
		let typeTheirs = typeof theirs;

		let parsedType = `${type}/${field}`.replace(/\/[0-9]+/g,'/index');
		let level = type.split('/').length;

		if (typeYours === 'boolean' || typeTheirs === 'boolean') {
			yours = yours ? i18n('Yes') : i18n('No');
			theirs = theirs ? i18n('Yes') : i18n('No');
		}


		/*if ((typeYours !== 'string' && typeYours !== 'undefined') || (typeTheirs !== 'string' && typeTheirs !== 'undefined')) {
			return (<div className='merge-editor-text-field field-error'>{field} cannot be parsed for display</div>);
		}*/
		let isDiff = yours !== theirs;
		if (!isObjName &&
			((!isDiff && conflictsOnly) ||
			 (yours === '' && theirs === '') ||
			 (typeYours === 'undefined' && typeTheirs === 'undefined')
			)
		) {
			return null;
		}

		return (
			<GridSection className={isDiff ? 'merge-editor-text-field diff' : 'merge-editor-text-field'}>
				<GridItem className='field-col grid-col-title' stretch>
					<div className={`field ${isDiff ? 'diff' : ''} field-name level-${level} ${isObjName ? 'field-object-name' : ''}`}>
						{i18n(parsedType)}
					</div>
				</GridItem>
				<GridItem className='field-col grid-col-yours' stretch>
					<div className={`field field-yours ${!yours ? 'empty-field' : ''}`} >{yours || (isObjName ? '' : '━━')}</div>
				</GridItem>
				<GridItem className='field-col grid-col-theirs' stretch>
					<div className={`field field-theirs ${!theirs ? 'empty-field' : ''}`}>{theirs || (isObjName ? '' : '━━')}</div>
				</GridItem>
				<GridItem stretch/>
			</GridSection>
		);
	}
};

class MergeEditorView extends React.Component {
	state = {
		collapsingSections: {},
		conflictsOnly: false
	};

	render() {
		let {conflicts, item, conflictFiles, onResolveConflict, currentScreen, resolution} = this.props;

		return (
		<div className='merge-editor'>
			{conflictFiles && this.renderConflictTableTitles()}
			<div className='merge-editor-body'>
				{conflictFiles && conflictFiles.sort((a, b) => a.type > b.type).map(file => (
				<ConflictCategory key={'conflict_' + file.id} conflict={file} item={item} isCollapsed={this.state.collapsingSections[file.id]}
					collapseExpand={()=>{this.updateCollapseState(file.id);}}
					onResolveConflict={cDetails => onResolveConflict({...cDetails, currentScreen})}>
					{(conflicts && conflicts[file.id]) &&
						this.getUnion(conflicts[file.id].yours, conflicts[file.id].theirs).map(field => {
							return this.renderField(field, file, conflicts[file.id].yours[field], conflicts[file.id].theirs[field], resolution);
						})}
				</ConflictCategory>))}
			</div>
		</div>);
	}

	renderConflictTableTitles()
	{
		return (<GridSection className='conflict-titles-section'>
			<GridItem>
				{i18n('Page')}
			</GridItem>
			<GridItem className='yours'>
				{i18n('Local (Me)')}
			</GridItem>
			<GridItem className='theirs'>
				{i18n('Last Committed')}
			</GridItem>
			<GridItem className='resolve'>
				<Input
					label={i18n('Show Conflicts Only')}
					type='checkbox'
					value={this.state.conflictsOnly}
					onChange={e => this.setState({conflictsOnly: e}) } />
			</GridItem>
		</GridSection>);
	}
	// <Checkbox
	// 	label={i18n('Show Conflicts Only')}
	// 	value={this.state.conflictsOnly}
	// 	checked={this.state.conflictsOnly}
	// 	onChange={checked => this.setState({conflictsOnly: checked})} />

	renderObjects(yours, theirs, fileType, field, id, resolution) {
		if (equal(yours, theirs)) {
			return;
		}
		let {conflictsOnly} = this.state;
		return (
			<div key={`obj_${fileType}/${field}_${id}`}>
				<TextCompare field={field} type={fileType} conflictsOnly={conflictsOnly} yours='' theirs='' isObjName resolution={resolution} />
				<div className='field-objects'>
					<div>
						{this.getUnion(yours, theirs).map(key =>
							this.renderField(
								key,
								{type: `${fileType}/${field}`, id},
								yours && yours[key],
								theirs && theirs[key]
							)
						)}
					</div>
				</div>
			</div>
		);
	}

	renderList(yours = [], theirs = [], type, field, id, resolution) {
		let theirsList = theirs.join(', ');
		let yoursList = yours.join(', ');
		let {conflictsOnly} = this.state;
		return (<TextCompare key={'text_' + id + '_' + field}
			 field={field} type={type} yours={yoursList} theirs={theirsList} conflictsOnly={conflictsOnly} resolution={resolution} />);
	}

	renderField(field, file, yours, theirs, resolution) {
		if (yours) {
			if (Array.isArray(yours)) {
				return this.renderList(yours, theirs, file.type, field, file.id, resolution);
			}
			else if (typeof yours === 'object') {
				return this.renderObjects(yours, theirs, file.type, field, file.id, resolution);
			}
		} else if (theirs) {
			if (Array.isArray(theirs)) {
				return this.renderList(yours, theirs, file.type, field, file.id, resolution);
			}
			else if (typeof theirs === 'object') {
				return this.renderObjects(yours, theirs, file.type, field, file.id, resolution);
			}
		}
		let {conflictsOnly} = this.state;
		return (<TextCompare key={'text_' + file.id + '_' + field} resolution={resolution}
			 field={field} type={file.type} yours={yours} theirs={theirs} conflictsOnly={conflictsOnly} />);
	}

	getUnion(yours = {},theirs = {}) {
		let yoursKeys = Object.keys(yours);
		let theirsKeys = Object.keys(theirs);
		let myUn = union(yoursKeys, theirsKeys);
		return myUn;//.sort((a, b) => a > b);
	}

	updateCollapseState(conflictId) {
		const {fetchConflict, item: {id: itemId, version}, /*conflicts*/} = this.props;
		let isCollapsed = this.state.collapsingSections[conflictId];
		// if (!isCollapsed && !(conflicts && conflictId in conflicts)) {
		if (!isCollapsed) {
			fetchConflict({cid: conflictId, itemId, version});
		}
		this.setState({
			collapsingSections: {
				...this.state.collapsingSections,
				[conflictId]: !isCollapsed
			}
		});
	}
}

export default MergeEditorView;
