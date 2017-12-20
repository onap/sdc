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
import PropTypes from 'prop-types';
import classNames from 'classnames';
import Collapse from 'react-bootstrap/lib/Collapse.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {mouseActions, errorLevels, nodeFilters} from './HeatValidationConstants.js';

const leftPanelWidth = 250;
const typeToIcon = Object.freeze({
	heat: 'nestedHeat',
	volume: 'base',
	network: 'network',
	artifact: 'artifacts',
	env: 'env',
	other: 'other'
});


class HeatValidationView extends Component {

	static propTypes = {
		attachmentsTree: PropTypes.object.isRequired,
		errorList: PropTypes.array.isRequired,
		currentErrors: PropTypes.array.isRequired,
		currentWarnings: PropTypes.array.isRequired,
		onSelectNode: PropTypes.func.isRequired,
		onDeselectNode: PropTypes.func.isRequired,
		toggleExpanded: PropTypes.func.isRequired,
		selectedNode: PropTypes.string
	};

	render() {
		return (<div className='vsp-attachments-heat-validation' data-test-id='heat-validation-editor'>
			<HeatFileTree errorList={this.props.errorList} attachmentsTree={this.props.attachmentsTree}
				onSelectNode={this.props.onSelectNode} toggleExpanded={this.props.toggleExpanded}
				selectedNode={this.props.selectedNode} onDeselectNode={this.props.onDeselectNode} />
			<HeatMessageBoard errors={this.props.currentErrors} warnings={this.props.currentWarnings} selectedNode={this.props.selectedNode} />
		</div> );
	}
}

function HeatFileTreeRow(props) {
	let {node, path, toggleExpanded, selectedNode, selectNode} = props;
	let isFolder = node.children && node.children.length > 0;
	return (
		<div onDoubleClick={() => toggleExpanded(path)} className={classNames({
			'tree-node-row': true,
			'tree-node-clicked': node.name === props.selectedNode
		})} data-test-id='validation-tree-node'>
			<div className='name-section'>
				{
					isFolder &&
						<div onClick={() => toggleExpanded(path)}
							className='tree-node-expander'>
							<SVGIcon name={!node.expanded ? 'chevronUp' : 'chevronDown'} data-test-id='validation-tree-block-toggle'/>
						</div>
				}
				{

					<span className='tree-node-icon'>
						<SVGIcon name={typeToIcon[node.type]} color={selectedNode === node.name ? 'primary' : 'secondary'}/>
					</span>
				}
				{

					<span  className='tree-node-name' onClick={() => selectNode(node.name)} data-test-id='validation-tree-node-name'>
						{node.name ? node.name : 'UNKNOWN'}
					</span>
				}
			</div>
			<ErrorsAndWarningsCount errorList={node.errors} onClick={() => selectNode(node.name)} />
		</div>);
}

function HeatFileTreeHeader(props) {
	let hasErrors = props.errorList.filter(error => error.level === errorLevels.ERROR).length > 0;
	return (
		<div onClick={() => props.selectNode(nodeFilters.ALL)} className={classNames({'attachments-tree-header': true,
			'header-selected' : props.selectedNode === nodeFilters.ALL})} data-test-id='validation-tree-header'>
			<div className='tree-header-title' >
				{/*<SVGIcon name='zip' color={props.selectedNode === nodeFilters.ALL ? 'primary' : ''}  iconClassName='header-icon' />*/}
				<span className={classNames({'tree-header-title-text' : true,
					'tree-header-title-selected' : props.selectedNode === nodeFilters.ALL})}>{i18n('{title} {hasErrors}', {title: props.headerTitle, hasErrors: hasErrors ? '(Draft)' : ''})}</span>
			</div>
			<ErrorsAndWarningsCount errorList={props.errorList} size='large' />
		</div>);
}

class HeatFileTree extends React.Component  {
	static propTypes = {
		attachmentsTree: PropTypes.object.isRequired,
		errorList: PropTypes.array.isRequired,
		onSelectNode: PropTypes.func.isRequired,
		onDeselectNode: PropTypes.func.isRequired,
		toggleExpanded: PropTypes.func.isRequired,
		selectedNode: PropTypes.string
	};
	state = {
		treeWidth: '400'
	};
	render() {
		let {attachmentsTree} = this.props;
		return (
			<div className='validation-tree-section' style={{'width' : this.state.treeWidth + 'px'}}>
				<div className='vsp-attachments-heat-validation-tree'>
					<div className='tree-wrapper'>
						{attachmentsTree && attachmentsTree.children && attachmentsTree.children.map((child, ind) => this.renderNode(child, [ind]))}
					</div>
				</div>
				<div onMouseDown={(e) => this.onChangeTreeWidth(e)}
					 className='vsp-attachments-heat-validation-separator' data-test-id='validation-tree-separator'></div>
			</div>);
	}
	renderNode(node, path) {
		let rand = Math.random() * (3000 - 1) + 1;
		let isFolder = node.children && node.children.length > 0;
		let {selectedNode} = this.props;
		return (
			<div key={node.name + rand} className={classNames({'tree-block-inside' : !node.header})}>
				{
					node.header ?
					<HeatFileTreeHeader headerTitle={node.name} selectedNode={selectedNode} errorList={this.props.errorList} selectNode={(nodeName) => this.selectNode(nodeName)}  /> :
					<HeatFileTreeRow toggleExpanded={this.props.toggleExpanded} node={node} path={path} selectedNode={selectedNode} selectNode={() => this.selectNode(node.name)} />
				}
				{
					isFolder &&
					<Collapse in={node.expanded}>
						<div className='tree-node-children'>
							{
								node.children.map((child, ind) => this.renderNode(child, [...path, ind]))
							}
						</div>
					</Collapse>
				}
			</div>
		);
	}





	selectNode(currentSelectedNode) {
		let {onDeselectNode, onSelectNode, selectedNode} = this.props;
		if (currentSelectedNode !== selectedNode) {
			onSelectNode(currentSelectedNode);
		} else {
			onDeselectNode();
		}



	}

	onChangeTreeWidth(e) {
		if (e.button === mouseActions.MOUSE_BUTTON_CLICK) {
			let onMouseMove = (e) => {
				this.setState({treeWidth: e.clientX - leftPanelWidth});
			};
			let onMouseUp = () => {
				document.removeEventListener('mousemove', onMouseMove);
				document.removeEventListener('mouseup', onMouseUp);
			};
			document.addEventListener('mousemove', onMouseMove);
			document.addEventListener('mouseup', onMouseUp);
		}
	}
}

class HeatMessageBoard extends Component {
	static propTypes = {
		currentErrors: PropTypes.array,
		currentWarnings: PropTypes.array,
		selectedNode: PropTypes.string
	};
	render() {
		let {errors, warnings} = this.props;
		let allItems = [...errors, ...warnings];
		return (
			<div className='message-board-section'>
				{ allItems.map(error => this.renderError(error)) }
			</div>
		);
	}
	renderError(error) {
		let rand = Math.random() * (3000 - 1) + 1;
		return (
			<div
				key={error.name + error.errorMessage + error.parentName + rand}
				className='error-item' data-test-id='validation-error'>
				{error.level === errorLevels.WARNING ?
					<SVGIcon name='exclamationTriangleLine' iconClassName='large' color='warning' /> : <SVGIcon name='error' iconClassName='large' color='negative' /> }
				<span className='error-item-file-type'>
				{
					(this.props.selectedNode === nodeFilters.ALL) ?
						<span>
							<span className='error-file-name'>
								{error.name}
							</span>
							<span>
								{error.errorMessage}
							</span>
						</span> :
						error.errorMessage
				}
				</span>
			</div>
		);
	}
}
class ErrorsAndWarningsCount extends Component {
	static propTypes = {
		errorList: PropTypes.array,
		size: PropTypes.string
	};
	render() {
		let errors = this.getErrorsAndWarningsCount(this.props.errorList);
		if (!errors) {
			return null;
		}
		let {size} = this.props;
		return (<div className='counters'>
			{(errors.errorCount > 0) && <div className='counter'>
				<SVGIcon name='error' color='negative' iconClassName={size}/>
				<div className={'error-text ' + (size ? size : '')} data-test-id='validation-error-count'>{errors.errorCount}</div>
			</div>}
			{(errors.warningCount > 0) && <div className='counter'>
				<SVGIcon name='exclamationTriangleLine' iconClassName={size} color='warning'/>
				<div className={'warning-text ' + (size ? size : '')} data-test-id='validation-warning-count'>{errors.warningCount}</div>
			</div>}
		</div>);
	}
	getErrorsAndWarningsCount(errorList) {
		let errorCount = 0, warningCount = 0;
		if (errorList && errorList.length > 0) {
			for (let i = 0; i < errorList.length; i++) {
				if (errorList[i].level === errorLevels.ERROR) {
					errorCount++;
				} else if (errorList[i].level === errorLevels.WARNING) {
					warningCount++;
				}
			}
		}
		if (errorCount === 0 && warningCount === 0) {
			return null;
		}
		return {errorCount, warningCount};
	}
}
export default HeatValidationView;
