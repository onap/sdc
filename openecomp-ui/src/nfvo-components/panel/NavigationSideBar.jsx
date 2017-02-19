import React from 'react';
import classnames from 'classnames';
import Collapse from 'react-bootstrap/lib/Collapse.js';

class NavigationSideBar extends React.Component {

	static PropTypes = {
		activeItemId: React.PropTypes.string.isRequired,
		onSelect: React.PropTypes.func,
		onToggle: React.PropTypes.func,
		groups: React.PropTypes.array
	};

	render() {
		let {groups, activeItemId} = this.props;

		return (
			<div className='navigation-side-content'>
				{groups.map(group => (
					<div className='navigation-group'  key={group.id}>
						<div className='group-name'>{group.name}</div>
						<div className='navigation-group-items'>
							{
								group.items && group.items.map(item => this.renderGroupItem(item, activeItemId))
							}
						</div>
					</div>
				))}
			</div>
		);
	}

	renderGroupItem(item, activeItemId) {
		let isGroup = item.items && item.items.length > 0;
		return (
			<div className={classnames('navigation-group-item', {'selected-item': item.id === activeItemId})}>
				<div
					key={item.id}
					className={classnames('navigation-group-item-name', {
						'selected': item.id === activeItemId,
						'disabled': item.disabled,
						'bold-name': item.expanded,
						'hidden': item.hidden
					})}
					onClick={(event) => this.handleItemClicked(event, item)}>
					{item.name}
				</div>
				{isGroup &&
					<Collapse in={item.expanded}>
						<div>
							{item.items.map(item => this.renderGroupItem(item, activeItemId))}
						</div>
					</Collapse>
				}
			</div>
		);
	}

	handleItemClicked(event, item) {
		event.stopPropagation();
		if(this.props.onToggle) {
			this.props.onToggle(this.props.groups, item.id);
		}
		if(item.onSelect) {
			item.onSelect();
		}
		if(this.props.onSelect) {
			this.props.onSelect(item);
		}
	}
}

export default NavigationSideBar;
