/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG
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
import React, { Component } from 'react';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import { SVGIcon } from 'onap-ui-react';
import i18n from 'nfvo-utils/i18n/i18n.js';

function ActivityLogSortableCellHeader({
    isHeader,
    data,
    isAscending,
    onSort
}) {
    if (isHeader) {
        return (
            <button type="button" className="date-header" onClick={onSort}>
                <span>{data}</span>
                <span
                    aria-hidden="true"
                    className={`header-sort-arrow ${
                        isAscending ? 'up' : 'down'
                    }`}
                />
            </button>
        );
    }
    return (
        <span className="date-cell">
            <span>
                {i18n.dateNormal(data, {
                    year: 'numeric',
                    month: 'numeric',
                    day: 'numeric'
                })}
            </span>
            <span>
                {i18n.dateNormal(data, {
                    hour: 'numeric',
                    minute: 'numeric',
                    hour12: true
                })}
            </span>
        </span>
    );
}

function ActivityLogStatus({ status, isHeader }) {
    if (isHeader) {
        return <span>{status}</span>;
    }
    let { message, success } = status;
    return (
        <span>
            <span className={`status-icon ${success}`}>{`${
                success ? i18n('Success') : i18n('Failure')
            }`}</span>
            {success && <SVGIcon name="checkCircle" color="positive" />}
            {!success && (
                <OverlayTrigger
                    placement="bottom"
                    overlay={
                        <Tooltip
                            className="activity-log-message-tooltip"
                            id={'activity-log-message-tooltip'}>
                            <div className="message-block">{message}</div>
                        </Tooltip>
                    }>
                    <span className="message-further-info-icon">{'?'}</span>
                </OverlayTrigger>
            )}
        </span>
    );
}

export function ActivityListItem({ activity, isHeader, isAscending, onSort }) {
    let { type, timestamp, comment, user, status } = activity;
    const Cell = isHeader ? 'th' : 'td';
    const scope = isHeader ? 'col' : undefined;
    return (
        <tr
            className={`activity-list-item ${isHeader ? 'header' : ''}`}
            data-test-id="activity-list-item">
            <Cell
                scope={scope}
                aria-sort={
                    isHeader
                        ? isAscending ? 'ascending' : 'descending'
                        : undefined
                }
                className="table-cell activity-date"
                data-test-id="activity-date">
                <ActivityLogSortableCellHeader
                    isHeader={isHeader}
                    data={timestamp}
                    isAscending={isAscending}
                    onSort={onSort}
                />
            </Cell>
            <Cell
                scope={scope}
                className="table-cell activity-action"
                data-test-id="activity-action">
                {i18n(type)}
            </Cell>
            <Cell
                scope={scope}
                className="table-cell activity-comment"
                title={isHeader ? '' : comment}
                data-test-id="activity-comment">
                <span>{i18n(comment)}</span>
            </Cell>
            <Cell
                scope={scope}
                className="table-cell activity-username"
                data-test-id="activity-username">
                {isHeader
                    ? i18n(activity.user)
                    : `${i18n(user.name)} (${user.id})`}
            </Cell>
            <Cell
                scope={scope}
                className="table-cell activity-status"
                data-test-id="activity-status">
                <ActivityLogStatus isHeader={isHeader} status={status} />
            </Cell>
        </tr>
    );
}

class ActivityLogView extends Component {
    state = {
        localFilter: '',
        sortAscending: true
    };

    render() {
        return (
            <div className="activity-log-view">
                <ListEditorView
                    title={i18n('Activity Log')}
                    filterValue={this.state.localFilter}
                    onFilter={filter => this.setState({ localFilter: filter })}>
                    {/* An array, because ListEditorView shows the filter only for children with a length */}
                    {[
                        <table
                            key="activity-log-table"
                            className="activity-log-table"
                            aria-label={i18n('Activity Log')}>
                            <thead>
                                <ActivityListItem
                                    activity={{
                                        timestamp: 'Date',
                                        type: 'Action',
                                        comment: 'Comment',
                                        user: 'Username',
                                        status: 'Status'
                                    }}
                                    isAscending={this.state.sortAscending}
                                    onSort={() =>
                                        this.setState({
                                            sortAscending: !this.state
                                                .sortAscending
                                        })
                                    }
                                    isHeader
                                />
                            </thead>
                            <tbody>
                                {this.sortActivities(
                                    this.filterActivities(),
                                    this.state.sortAscending
                                ).map(activity => (
                                    <ActivityListItem
                                        key={activity.id}
                                        activity={activity}
                                    />
                                ))}
                            </tbody>
                        </table>
                    ]}
                </ListEditorView>
            </div>
        );
    }

    filterActivities() {
        let { activities } = this.props;
        let { localFilter } = this.state;
        if (localFilter.trim()) {
            const filter = new RegExp(escape(localFilter), 'i');
            return activities.filter(
                ({ user = { id: '', name: '' }, comment = '', type = '' }) =>
                    escape(user.id).match(filter) ||
                    escape(user.name).match(filter) ||
                    escape(comment).match(filter) ||
                    escape(type).match(filter)
            );
        } else {
            return activities;
        }
    }

    sortActivities(activities, ascending) {
        const sorted = [...activities].sort(
            (a, b) => a.timestamp - b.timestamp
        );
        return ascending ? sorted : sorted.reverse();
    }
}

export default ActivityLogView;
