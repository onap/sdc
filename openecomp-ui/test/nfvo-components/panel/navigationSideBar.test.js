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
import { render, fireEvent } from '@testing-library/react';
import NavigationSideBar from 'nfvo-components/panel/NavigationSideBar.jsx';

const groups = [
    {
        id: 'g',
        name: 'VSP',
        items: [
            { id: 'OVERVIEW', name: 'Overview' },
            {
                id: 'COMPONENTS',
                name: 'Components',
                expanded: false,
                items: [{ id: 'C1', name: 'c1' }]
            },
            { id: 'LOCKED', name: 'Locked', disabled: true }
        ]
    }
];

const renderSideBar = (onSelect = jest.fn()) => {
    const { container } = render(
        <NavigationSideBar
            groups={groups}
            activeItemId="OVERVIEW"
            onSelect={onSelect}
        />
    );
    const link = id =>
        container.querySelector(`[data-test-id="navbar-group-item-${id}"]`);
    return { link, onSelect };
};

describe('NavigationSideBar - links', () => {
    it('renders the selected link as a button with the exact Selenium class', () => {
        const { link } = renderSideBar();
        const overview = link('OVERVIEW');
        expect(overview.tagName).toBe('BUTTON');
        expect(overview.getAttribute('type')).toBe('button');
        expect(overview.getAttribute('class')).toBe(
            'navigation-group-item-name selected'
        );
    });

    it('marks only the selected link as the current page', () => {
        const { link } = renderSideBar();
        expect(link('OVERVIEW').getAttribute('aria-current')).toBe('page');
        expect(link('COMPONENTS').hasAttribute('aria-current')).toBe(false);
    });

    it('reports whether a group is expanded', () => {
        const { link } = renderSideBar();
        expect(link('COMPONENTS').getAttribute('aria-expanded')).toBe(
            'false'
        );
        expect(link('OVERVIEW').hasAttribute('aria-expanded')).toBe(false);
    });

    it('selects a link on click', () => {
        const { link, onSelect } = renderSideBar();
        fireEvent.click(link('COMPONENTS'));
        expect(onSelect).toHaveBeenCalledTimes(1);
        expect(onSelect).toHaveBeenCalledWith(
            expect.objectContaining({ id: 'COMPONENTS' })
        );
    });

    it('disables a disabled link natively', () => {
        const { link, onSelect } = renderSideBar();
        const locked = link('LOCKED');
        expect(locked.disabled).toBe(true);
        fireEvent.click(locked);
        expect(onSelect).not.toHaveBeenCalled();
    });
});
