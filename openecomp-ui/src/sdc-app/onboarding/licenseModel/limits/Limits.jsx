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
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import LimitEditor from './LimitEditor.js';
import {NEW_LIMIT_TEMP_ID, selectValues} from './LimitEditorConstants.js';

const LimitItem = ({isReadOnlyMode, limit, onDelete, onSelect}) => {
	const {name, description, value, metric, aggregationFunction = '', time = ''} = limit;
	const timeLabel = time ? `per ${time}` : '';
	let metricOption = selectValues.METRIC.find(item => item.enum === metric);
	metricOption = metricOption ? metricOption.title : metric;

	return (
		<ListEditorItemView
			onDelete={onDelete}
			onSelect={onSelect}
			isReadOnlyMode={isReadOnlyMode}>
			<div className='list-editor-item-view-field limit-name'>
				<div className='text name'>{name}</div>
			</div>

			<div className='list-editor-item-view-field limit-description'>
				<div className='text description'>{description}</div>
			</div>

			<div className='list-editor-item-view-field limit-metric-details'>
				<div className='text description'>{`${metricOption} ${value} ${aggregationFunction} ${timeLabel}`}</div>
			</div>
		</ListEditorItemView>
	);
};

class Limits extends React.Component {


	state = {
		localFilter: ''
	};

	render() {
		const {isReadOnlyMode = false, limitEditor, limitsList = [], onCloseLimitEditor, selectedLimit} = this.props;
		let limitsNames = {};
		for (let i = 0; i < limitsList.length; i++) {
			limitsNames[limitsList[i].name.toLowerCase()] = limitsList[i].id;
		}
		return (
			<div className='license-model-limits-view'>
				<ListEditorView isReadOnlyMode={isReadOnlyMode}>
					{this.props.selectedLimit === NEW_LIMIT_TEMP_ID && limitEditor.data &&
						<LimitEditor limitsNames={limitsNames} onCancel={onCloseLimitEditor} onSubmit={ () => this.submit()} isReadOnlyMode={isReadOnlyMode}/>
					}
					{limitsList.length === 0 && !limitEditor.data && <div className='no-limits-text'>{i18n('There are no limits')}</div>}
					{limitsList.map(limit =>
					<div key={limit.id}  className='limit-item-wrapper'>
						<LimitItem
							onDelete={() => this.deleteLimit(limit)}
							onSelect={selectedLimit ? undefined : () => this.props.onSelectLimit(limit)}
							clickable={!selectedLimit}
							isReadOnlyMode={isReadOnlyMode}
							limit={limit}/>
						{limit.id === selectedLimit &&  limitEditor.data &&
							<LimitEditor
								limitsNames={limitsNames}
								onCancel={onCloseLimitEditor}
								onSubmit={ () => this.submit()}
								isReadOnlyMode={isReadOnlyMode} />
						}
					</div> )}
				</ListEditorView>

			</div>
		);
	}

	submit() {
		let {onSubmit, onCloseLimitEditor, parent, limitEditor, licenseModelId, version, limitType} = this.props;
		onSubmit({type: limitType, ...limitEditor.data}, parent, licenseModelId, version).then(() => onCloseLimitEditor());
	}

	deleteLimit(limit) {
		let {onDelete, parent, licenseModelId, version, onCloseLimitEditor, selectedLimit} = this.props;
		onDelete({limit, parent, licenseModelId, version, onCloseLimitEditor, selectedLimit});
	}

	filterList() {
		let {limitsList = []} = this.props;
		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return limitsList.filter(({name = '', description = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter);
			});
		}
		else {
			return limitsList;
		}
	}
}

export default Limits;
