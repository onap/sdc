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
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import SVGIcon from 'nfvo-components/icon/SVGIcon.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';

function ActivityLogSortableCellHeader({isHeader, data, isDes, onSort}) {
	if (isHeader) {
		return (
			<span className='date-header' onClick={onSort}>
				<span>{data}</span>
				<span className={`header-sort-arrow ${isDes ? 'up' : 'down'}`}></span>
			</span>
		);
	}
	return (
		<span className='date-cell'>
			<span>{i18n.dateNormal(data, {
				year: 'numeric', month: 'numeric', day: 'numeric'
			})}</span>
			<span>{i18n.dateNormal(data, {
				hour: 'numeric', minute: 'numeric',
				hour12: true
			})}</span>
		</span>
	);
}

function ActivityLogStatus({status, isHeader}) {
	if (isHeader) {
		return <span>{status}</span>;
	}
	let {message, success} = status;
	return (
		<span>
			<span className={`status-icon ${success}`}>{`${success ? i18n('Success') : i18n('Failure')}`}</span>
			{success && <SVGIcon name='check-circle'/>}
			{!success && <OverlayTrigger placement='bottom' overlay={<Tooltip className='activity-log-message-tooltip' id={'activity-log-message-tooltip'}>
				<div className='message-block'>{message}</div>
			</Tooltip>}>
				<span className='message-further-info-icon'>{'?'}</span>
			</OverlayTrigger>}
		</span>
	);
}

export function ActivityListItem({activity, isHeader, isDes, onSort}) {
	let {type, timestamp, comment, user, status} = activity;
	return (
		<li className={`activity-list-item ${isHeader ? 'header' : ''}`} data-test-id='activity-list-item'>
			<div className='table-cell activity-date' data-test-id='activity-date'><ActivityLogSortableCellHeader isHeader={isHeader} data={timestamp} isDes={isDes} onSort={onSort}/></div>
			<div className='table-cell activity-action' data-test-id='activity-action'>{type}</div>
			<div className='table-cell activity-comment' title={comment} data-test-id='activity-comment'><span>{comment}</span></div>
			<div className='table-cell activity-username' data-test-id='activity-username'>{user}</div>
			<div className='table-cell activity-status' data-test-id='activity-status'><ActivityLogStatus isHeader={isHeader} status={status}/></div>
		</li>
	);
}

class ActivityLogView extends Component {

	state = {
		localFilter: '',
		sortDescending: true
	};

	render() {
		return (
			<div className='activity-log-view'>
				<ListEditorView
					title={i18n('Activity Log')}
					filterValue={this.state.localFilter}
					onFilter={filter => this.setState({localFilter: filter})}>
					<ActivityListItem
						isHeader={true}
						activity={{timestamp: 'Date', type: 'Action', comment: 'Comment', user: 'Username', status: 'Status'}}
						isDes={this.state.sortDescending}
						onSort={() => this.setState({sortDescending: !this.state.sortDescending})}/>
					{this.sortActivities(this.filterActivities(), this.state.sortDescending).map(activity => <ActivityListItem key={activity.id} activity={activity}/>)}
				</ListEditorView>
			</div>
		);
	}

	filterActivities() {
		let {activities} = this.props;
		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return activities.filter(({user = '', comment = '', type = ''}) => escape(user).match(filter) || escape(comment).match(filter) || escape(type).match(filter));
		}
		else {
			return activities;
		}
	}

	sortActivities(activities) {
		if (this.state.sortDescending) {
			return activities.sort((a, b) => a.timestamp - b.timestamp);
		}
		else {
			return activities.reverse();
		}
	}

}

export default ActivityLogView;
