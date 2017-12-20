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
import PropTypes from 'prop-types';
import classnames from 'classnames';
import Collapse from 'react-bootstrap/lib/Collapse.js';

class NavigationSideBar extends React.Component {
	static PropTypes = {
		activeItemId: PropTypes.string.isRequired,
		onSelect: PropTypes.func,
		onToggle: PropTypes.func,
		groups: PropTypes.array
	};

	constructor(props) {
		super(props);
		this.state = {
			activeItemId: null
		};
		this.handleItemClicked = this.handleItemClicked.bind(this);
	}

	render() {
		let {groups, activeItemId} = this.props;

		return (
			<div className='navigation-side-content'>
				{groups.map(group => (
					<NavigationMenu menu={group} activeItemId={activeItemId} onNavigationItemClick={this.handleItemClicked} key={'menu_' + group.id} />
				))}
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

class NavigationMenu extends React.Component {
	static PropTypes = {
		activeItemId: PropTypes.string.isRequired,
		onNavigationItemClick: PropTypes.func,
		menu: PropTypes.array
	};

	render() {
		const {menu, activeItemId, onNavigationItemClick} = this.props;
		return (
			<div className='navigation-group'  key={menu.id}>
				<NavigationMenuHeader title={menu.name} />
				<NavigationMenuItems items={menu.items} activeItemId={activeItemId} onNavigationItemClick={onNavigationItemClick} />
			</div>);
	}
}

function NavigationMenuHeader(props) {
	return <div className='group-name' data-test-id='navbar-group-name'>{props.title}</div>;
}

function getItemDataTestId(itemId) {
	return itemId.split('|')[0];
}
function NavigationMenuItems(props) {
	const {items, activeItemId, onNavigationItemClick} = props;
	return (
		<div className='navigation-group-items'>
			{
				items && items.map(item => (<NavigationMenuItem key={'menuItem_' + item.id} item={item} activeItemId={activeItemId} onNavigationItemClick={onNavigationItemClick} />))
			}
		</div>
	);
}

function NavigationMenuItem(props) {
	const {onNavigationItemClick, item, activeItemId} = props;
	const isGroup = item.items && item.items.length > 0;
	return (
		<div className={classnames('navigation-group-item', {'selected-item': item.id === activeItemId})} key={'item_' + item.id}>
			<NavigationLink item={item} activeItemId={activeItemId} onClick={onNavigationItemClick} />
			{isGroup && <Collapse in={item.expanded} data-test-id={'navigation-group-' + getItemDataTestId(item.id)}>
				<div>
						{item.items.map(subItem => (<NavigationMenuItem key={'menuItem_' + subItem.id} item={subItem} onNavigationItemClick={onNavigationItemClick} activeItemId={activeItemId}  />)) }
				</div>
			</Collapse>
			}
		</div>
	);
}

function NavigationLink(props) {
	const {item, activeItemId, onClick} = props;
	// todo should this be button
	return (
		<div
			key={'navAction_' + item.id}
			className={classnames('navigation-group-item-name', {
				'selected': item.id === activeItemId,
				'disabled': item.disabled,
				'bold-name': item.expanded,
				'hidden': item.hidden
			})}
			onClick={(event) => onClick(event, item)}
			data-test-id={'navbar-group-item-' + getItemDataTestId(item.id)}>
			{item.name}
		</div>
	);
}

export default NavigationSideBar;
