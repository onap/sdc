import React from 'react';
import FontAwesome from 'react-fontawesome';
import classNames from 'classnames';
import Collapse from 'react-bootstrap/lib/Collapse.js';

import i18n from 'nfvo-utils/i18n/i18n.js';
import {nodeTypes, mouseActions} from './AttachmentsConstants';

const typeToIcon = Object.freeze({
	heat: 'building-o',
	volume: 'database',
	network: 'cloud',
	artifact: 'gear',
	env: 'server',
	other: 'cube'
});

const leftPanelWidth = 250;

class SoftwareProductAttachmentsView extends React.Component {

	static propTypes = {
		attachmentsTree: React.PropTypes.object.isRequired
	};
	state = {
		treeWidth: '400',
	};

	render() {
		let {attachmentsTree, errorList = []} = this.props;

		let {treeWidth} = this.state;
		return (
			<div className='software-product-attachments'>
				<div className='software-product-view'>
					<div className='software-product-landing-view-right-side'>
						<div className='software-product-attachments-main'>
							<div className='software-product-attachments-tree' style={{'width' : treeWidth + 'px'}}>
								<div className='tree-wrapper'>
									{
										attachmentsTree && attachmentsTree.children && attachmentsTree.children.map((child, ind) => this.renderNode(child, [ind]))
									}
								</div>
							</div>
							<div className='software-product-attachments-separator' onMouseDown={e => this.onChangeTreeWidth(e)} />
							<div className='software-product-attachments-error-list'>
								{errorList.length ? this.renderErrorList(errorList) : <div className='no-errors'>{attachmentsTree.children ?
									i18n('VALIDATION SUCCESS') : i18n('THERE IS NO HEAT DATA TO PRESENT') }</div>}
							</div>
						</div>
					</div>
				</div>
			</div>
		);
	}



	renderNode(node, path) {
		let isFolder = node.children && node.children.length > 0;
		let {onSelectNode} = this.props;
		return (
			<div key={node.name} className='tree-block-inside'>
				{
					<div onDoubleClick={() => this.props.toggleExpanded(path)} className={this.getTreeRowClassName(node.name)}>
						{
							isFolder &&
							<div onClick={() => this.props.toggleExpanded(path)} className={classNames('tree-node-expander', {'tree-node-expander-collapsed': !node.expanded})}>
								<FontAwesome name='caret-down'/>
							</div>
						}
						{

							<span className='tree-node-icon'>
								<FontAwesome name={typeToIcon[node.type]}/>
							</span>
						}
						{

							<span onClick={() => onSelectNode(node.name)} className={this.getTreeTextClassName(node)}>
							{node.name}
							</span>
						}
					</div>
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

	createErrorList(errorList, node, parent) {
		if (node.errors) {
			node.errors.forEach(error => errorList.push({
				error,
				name: node.name,
				parentName: parent.name,
				type: node.type
			}));
		}
		if (node.children && node.children.length) {
			node.children.map((child) => this.createErrorList(errorList, child, node));
		}
	}

	renderErrorList(errors) {
		let prevError = {};
		let {selectedNode} = this.props;
		return errors.map(error => {
			let isSameNodeError = error.name === prevError.name && error.parentName === prevError.parentName;
			prevError = error;

			return (
				<div
					key={error.name + error.errorMessage + error.parentName}

					onClick={() => this.selectNode(error.name)}
					className={classNames('error-item', {'clicked': selectedNode === error.name, 'shifted': !isSameNodeError})}>
					<span className={classNames('error-item-file-type', {'strong': !isSameNodeError})}>
					{
						error.hasParent ?
							i18n('{type}  {name} in {parentName}: ', {
								type: nodeTypes[error.type],
								name: error.name,
								parentName: error.parentName
							}) :
							i18n('{type}  {name}: ', {
								type: nodeTypes[error.type],
								name: error.name
							})
					}
					</span>
					<span className={`error-item-file-type ${error.errorLevel}`}> {error.errorMessage} </span>
				</div>
			);
		});
	}

	selectNode(currentSelectedNode) {
		let {onUnselectNode, onSelectNode, selectedNode} = this.props;
		if (currentSelectedNode !== selectedNode) {
			onSelectNode(currentSelectedNode);
		}else{
			onUnselectNode();
		}

	}

	getTreeRowClassName(name) {
		let {hoveredNode, selectedNode} = this.props;
		return classNames({
			'tree-node-row': true,
			'tree-node-selected': name === hoveredNode,
			'tree-node-clicked': name === selectedNode
		});
	}

	getTreeTextClassName(node) {
		let {selectedNode} = this.props;
		return classNames({
			'tree-element-text': true,
			'error-status': node.errors,
			'error-status-selected': node.name === selectedNode
		});
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

export default SoftwareProductAttachmentsView;
