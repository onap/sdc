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
import ExpandableInput from 'nfvo-components/input/ExpandableInput.jsx';

class Harness extends React.Component {
    state = { value: this.props.initialValue || '' };
    render() {
        return (
            <ExpandableInput
                iconType="search"
                value={this.state.value}
                onChange={value => this.setState({ value })}
            />
        );
    }
}

const byTestId = (container, id) =>
    container.querySelector(`[data-test-id="${id}"]`);

describe('ExpandableInput', () => {
    it('opens from a named native search button and focuses the input', () => {
        const { container, getByRole } = render(<Harness />);
        const opener = getByRole('button', { name: 'Search' });
        expect(opener.getAttribute('type')).toBe('button');
        expect(opener.getAttribute('data-test-id')).toBe(
            'expandable-input-closed'
        );
        fireEvent.click(opener);
        const input = byTestId(container, 'expandable-input-opened');
        expect(input).toBeTruthy();
        expect(document.activeElement).toBe(input);
        expect(input.getAttribute('aria-label')).toBe('Search');
    });

    it('clears the value with a named native button and keeps focus in the input', () => {
        const { container, getByRole } = render(
            <Harness initialValue="abc" />
        );
        const clear = getByRole('button', { name: 'Clear search' });
        expect(clear.getAttribute('data-test-id')).toBe(
            'expandable-input-close-btn'
        );
        fireEvent.click(clear);
        const input = byTestId(container, 'expandable-input-opened');
        expect(input.value).toBe('');
        expect(document.activeElement).toBe(input);
    });

    it('hides the decorative search icon of the open input', () => {
        const { container, getByRole } = render(<Harness />);
        fireEvent.click(getByRole('button', { name: 'Search' }));
        const opened = container.querySelector(
            '.expandable-input-wrapper.opened'
        );
        const icon = opened.querySelector('.svg-icon-wrapper');
        expect(icon.getAttribute('aria-hidden')).toBe('true');
        expect(opened.querySelectorAll('button').length).toBe(0);
    });

    it('collapses on Escape when empty and returns focus to the opener', () => {
        const { container, getByRole } = render(<Harness />);
        fireEvent.click(getByRole('button', { name: 'Search' }));
        const input = byTestId(container, 'expandable-input-opened');
        fireEvent.keyDown(input, { key: 'Escape' });
        expect(byTestId(container, 'expandable-input-opened')).toBeNull();
        const opener = byTestId(container, 'expandable-input-closed');
        expect(document.activeElement).toBe(opener);
    });

    it('clears a non-empty value on Escape and stays open', () => {
        const { container } = render(<Harness initialValue="abc" />);
        const input = byTestId(container, 'expandable-input-opened');
        input.focus();
        fireEvent.keyDown(input, { key: 'Escape' });
        const after = byTestId(container, 'expandable-input-opened');
        expect(after).toBeTruthy();
        expect(after.value).toBe('');
        expect(document.activeElement).toBe(after);
    });
});
