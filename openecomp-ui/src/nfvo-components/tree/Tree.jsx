import React, {Component} from 'react';
import PropTypes from 'prop-types';
import {select} from 'd3-selection';
import {tree, stratify} from 'd3-hierarchy';


function diagonal(d) {

	const offset = 50;
	return 'M' + d.y + ',' + d.x
		+ 'C' + (d.parent.y + offset) + ',' + d.x
		+ ' ' + (d.parent.y + offset) + ',' + d.parent.x
		+ ' ' + d.parent.y + ',' + d.parent.x;
}

const nodeRadius = 8;
const verticalSpaceBetweenNodes = 70;
const NARROW_HORIZONTAL_SPACES = 47;
const WIDE_HORIZONTAL_SPACES = 65;

const stratifyFn = stratify().id(d => d.id).parentId(d => d.parent);

class Tree extends Component {

	// state = {
	// 	startingCoordinates: null,
	// 	isDown: false
	// }

	static propTypes = {
		name: PropTypes.string,
		width: PropTypes.number,
		allowScaleWidth: PropTypes.bool,
		nodes: PropTypes.arrayOf(PropTypes.shape({
			id: PropTypes.string,
			name: PropTypes.string,
			parent: PropTypes.string
		})),
		selectedNodeId: PropTypes.string,
		onNodeClick: PropTypes.func,
		onRenderedBeyondWidth: PropTypes.func
	};

	static defaultProps = {
		width: 500,
		allowScaleWidth : true,
		name: 'default-name'
	};

	render() {
		let {width, name, scrollable = false} = this.props;
		return (
			<div
				className={`tree-view ${name}-container ${scrollable ? 'scrollable' : ''}`}>
				<svg width={width} className={name}></svg>
			</div>
		);
	}

	componentDidMount() {
		this.renderTree();
	}

	// handleMouseMove(e) {
	// 	if (!this.state.isDown) {
	// 		return;
	// 	}
	// 	const container = select(`.tree-view.${this.props.name}-container`);
	// 	let coordinates = this.getCoordinates(e);
	// 	container.property('scrollLeft' , container.property('scrollLeft') + coordinates.x - this.state.startingCoordinates.x);
	// 	container.property('scrollTop' , container.property('scrollTop') + coordinates.y - this.state.startingCoordinates.y);
	// }

	// handleMouseDown(e) {
	// 	let startingCoordinates = this.getCoordinates(e);
	// 	this.setState({
	// 		startingCoordinates,
	// 		isDown: true
	// 	});
	// }

	// handleMouseUp() {
	// 	this.setState({
	// 		startingCorrdinates: null,
	// 		isDown: false
	// 	});
	// }

	// getCoordinates(e) {
	// 	var bounds = e.target.getBoundingClientRect();
	// 	var x = e.clientX - bounds.left;
	// 	var y = e.clientY - bounds.top;
	// 	return {x, y};
	// }

	componentDidUpdate(prevProps) {
		if (this.props.nodes.length !== prevProps.nodes.length ||
			this.props.selectedNodeId !== prevProps.selectedNodeId) {
			console.log('update');
			this.renderTree();
		}
	}

	renderTree() {
		let {width, nodes, name, allowScaleWidth, selectedNodeId, onRenderedBeyondWidth, toWiden} = this.props;
		if (nodes.length > 0) {

			let horizontalSpaceBetweenLeaves = toWiden ? WIDE_HORIZONTAL_SPACES : NARROW_HORIZONTAL_SPACES;
			const treeFn = tree().nodeSize([horizontalSpaceBetweenLeaves, verticalSpaceBetweenNodes]);//.size([width - 50, height - 50])
			let root = stratifyFn(nodes).sort((a, b) => a.data.name.localeCompare(b.data.name));
			let svgHeight = verticalSpaceBetweenNodes * root.height + nodeRadius * 6;

			treeFn(root);

			let nodesXValue = root.descendants().map(node => node.x);
			let maxX = Math.max(...nodesXValue);
			let minX = Math.min(...nodesXValue);

			let svgTempWidth = (maxX - minX) / 30 * (horizontalSpaceBetweenLeaves);
			let svgWidth = svgTempWidth < width ? (width - 5) : svgTempWidth;
			const svgEL = select(`svg.${name}`);
			const container = select(`.tree-view.${name}-container`);
			svgEL.html('');
			svgEL.attr('height', svgHeight);
			let canvasWidth = width;
			if (svgTempWidth > width) {
				if (allowScaleWidth) {
					canvasWidth = svgTempWidth;
				}
				// we seems to have a margin of 25px that we can still see with text
				if (((svgTempWidth - 25) > width) && onRenderedBeyondWidth !== undefined) {
					onRenderedBeyondWidth();
				}
			};
			svgEL.attr('width', canvasWidth);
			let rootGroup = svgEL.append('g').attr('transform', `translate(${svgWidth / 2 + nodeRadius},${nodeRadius * 4}) rotate(90)`);

			// handle link
			rootGroup.selectAll('.link')
				.data(root.descendants().slice(1))
				.enter().append('path')
				.attr('class', 'link')
				.attr('d', diagonal);

			let node = rootGroup.selectAll('.node')
				.data(root.descendants())
				.enter().append('g')
				.attr('class', node => `node ${node.children ? ' has-children' : ' leaf'} ${node.id === selectedNodeId ? 'selectedNode' : ''} ${this.props.onNodeClick ? 'clickable' : ''}`)
				.attr('transform', node => 'translate(' + node.y + ',' + node.x + ')')
				.on('click', node => this.onNodeClick(node));

			node.append('circle').attr('r', nodeRadius).attr('class', 'outer-circle');
			node.append('circle').attr('r', nodeRadius - 3).attr('class', 'inner-circle');

			node.append('text')
				.attr('y', nodeRadius / 4 + 1)
				.attr('x', - nodeRadius * 1.8)
				.text(node => node.data.name)
				.attr('transform', 'rotate(-90)');

			let selectedNode = selectedNodeId ? root.descendants().find(node => node.id === selectedNodeId) : null;
			if (selectedNode) {

				container.property('scrollLeft', (svgWidth / 4) + (svgWidth / 4 - 100) - (selectedNode.x / 30 * horizontalSpaceBetweenLeaves));
				container.property('scrollTop', (selectedNode.y / 100 * verticalSpaceBetweenNodes));

			} else {
				container.property('scrollLeft', (svgWidth / 4) + (svgWidth / 4 - 100));
			}
		}
	}

	onNodeClick(node) {
		if (this.props.onNodeClick) {
			this.props.onNodeClick(node.data);
		}
	}

}

export default Tree;
