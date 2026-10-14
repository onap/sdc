/*
 * Copyright (C) 2026 Deutsche Telekom AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import { render, fireEvent, within } from '@testing-library/react';
import ActivityLogView from 'sdc-app/common/activity-log/ActivityLogView.jsx';

const older = {
    id: 'a1',
    timestamp: new Date(2026, 0, 1).getTime(),
    type: 'Create',
    comment: 'created',
    user: { id: 'u1', name: 'Alice' },
    status: { success: true, message: '' }
};
const newer = {
    id: 'a2',
    timestamp: new Date(2026, 0, 2).getTime(),
    type: 'Commit',
    comment: 'changed',
    user: { id: 'u2', name: 'Bob' },
    status: { success: false, message: 'error' }
};

const renderLog = () =>
    render(<ActivityLogView activities={[newer, older]} />);

const bodyComments = table =>
    within(table)
        .getAllByRole('row')
        .slice(1)
        .map(row => row.querySelector('.activity-comment').textContent);

describe('Activity log table', () => {
    it('renders a named table with column headers and one row per activity', () => {
        const { getByRole } = renderLog();
        const table = getByRole('table', { name: 'Activity Log' });
        expect(table.tagName).toBe('TABLE');
        const headers = within(table).getAllByRole('columnheader');
        expect(headers.map(h => h.getAttribute('scope'))).toEqual([
            'col',
            'col',
            'col',
            'col',
            'col'
        ]);
        expect(within(table).getAllByRole('row').length).toBe(3);
        expect(table.querySelectorAll('li').length).toBe(0);
    });

    it('sorts by date from a button whose header reports the order', () => {
        const { getByRole } = renderLog();
        const table = getByRole('table', { name: 'Activity Log' });
        const sort = within(table).getByRole('button', { name: 'Date' });
        expect(sort.getAttribute('type')).toBe('button');
        const dateHeader = sort.closest('th');
        expect(dateHeader.getAttribute('aria-sort')).toBe('ascending');
        expect(bodyComments(table)).toEqual(['created', 'changed']);

        fireEvent.click(sort);
        expect(dateHeader.getAttribute('aria-sort')).toBe('descending');
        expect(bodyComments(table)).toEqual(['changed', 'created']);

        fireEvent.click(sort);
        expect(dateHeader.getAttribute('aria-sort')).toBe('ascending');
        expect(bodyComments(table)).toEqual(['created', 'changed']);
    });

    it('does not mark the other columns as sorted', () => {
        const { getByRole } = renderLog();
        const headers = within(
            getByRole('table', { name: 'Activity Log' })
        ).getAllByRole('columnheader');
        expect(headers.filter(h => h.hasAttribute('aria-sort')).length).toBe(
            1
        );
    });
});
