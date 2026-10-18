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
import { act, render, fireEvent } from '@testing-library/react';
import Term, { GlossaryDescriptions } from 'nfvo-components/term/Term.jsx';
import { Button } from 'onap-ui-react';

describe('Term', () => {
    it('leaves the child text alone, adds no tab stop and describes it', () => {
        const { container } = render(
            <Term term="VSP">
                <span>VSP</span>
            </Term>
        );
        const span = container.querySelector('span');
        expect(span.textContent).toBe('VSP');
        expect(span.hasAttribute('tabindex')).toBe(false);
        expect(span.getAttribute('aria-describedby')).toBe(
            'sdc-onboarding-glossary-VSP'
        );
    });

    it('keeps an existing aria-describedby', () => {
        const { container } = render(
            <Term term="VLM">
                <span aria-describedby="x">VLM</span>
            </Term>
        );
        expect(
            container.querySelector('span').getAttribute('aria-describedby')
        ).toBe('x sdc-onboarding-glossary-VLM');
    });

    it('renders unknown terms untouched', () => {
        const { container } = render(
            <Term term="XYZ">
                <span>XYZ</span>
            </Term>
        );
        expect(
            container.querySelector('span').hasAttribute('aria-describedby')
        ).toBe(false);
    });

    it('shows the tooltip on focus', () => {
        jest.useFakeTimers();
        const { container, baseElement } = render(
            <Term term="VFC">
                <button>Select VFC</button>
            </Term>
        );
        fireEvent.focus(container.querySelector('button'));
        act(() => jest.advanceTimersByTime(300));
        jest.useRealTimers();
        expect(
            baseElement.querySelector('[role="tooltip"]').textContent
        ).toContain('Virtual Function Component (VFC):');
    });

    it('renders one hidden description per term with prefixed ids', () => {
        const { container } = render(<GlossaryDescriptions />);
        const ids = Array.from(container.querySelectorAll('[id]')).map(
            e => e.id
        );
        expect(ids.length).toBe(7);
        expect(
            ids.every(id => id.indexOf('sdc-onboarding-glossary-') === 0)
        ).toBe(true);
        expect(container.firstChild.hidden).toBe(true);
    });

    it('keeps the existing and the glossary description while the tooltip is shown', () => {
        jest.useFakeTimers();
        const { container } = render(
            <Term term="VLM">
                <button aria-describedby="x">VLM</button>
            </Term>
        );
        fireEvent.focus(container.querySelector('button'));
        act(() => jest.advanceTimersByTime(300));
        jest.useRealTimers();
        expect(
            container.querySelector('button').getAttribute('aria-describedby')
        ).toBe('x sdc-onboarding-glossary-VLM');
    });

    it('hides the tooltip on Escape', () => {
        jest.useFakeTimers();
        const { container, baseElement } = render(
            <Term term="VSP">
                <button>Create new VSP</button>
            </Term>
        );
        fireEvent.focus(container.querySelector('button'));
        act(() => jest.advanceTimersByTime(300));
        expect(baseElement.querySelector('[role="tooltip"]')).not.toBeNull();
        fireEvent.keyDown(document, { key: 'Escape' });
        act(() => jest.advanceTimersByTime(1000));
        jest.useRealTimers();
        expect(baseElement.querySelector('[role="tooltip"]')).toBeNull();
    });

    it('shows the tooltip for a function-component child such as Button', () => {
        const { getByRole, baseElement } = render(
            <Term term="VSP">
                <Button btnType="link">Create new VSP</Button>
            </Term>
        );
        fireEvent.focus(getByRole('button', { name: 'Create new VSP' }));
        expect(
            baseElement.querySelector('[role="tooltip"]').textContent
        ).toContain('Vendor Software Product (VSP):');
    });
});
