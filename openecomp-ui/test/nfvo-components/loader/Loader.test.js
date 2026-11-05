/*
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React from 'react';
import { render, act, cleanup } from '@testing-library/react';
import { Loader } from 'nfvo-components/loader/Loader.jsx';

describe('Onboarding loader', () => {
    beforeEach(() => jest.useFakeTimers());
    afterEach(() => {
        cleanup();
        jest.useRealTimers();
    });
    const advance = ms => act(() => jest.advanceTimersByTime(ms));

    it('blocks clicks immediately but shows feedback only after 300 ms', () => {
        const { container } = render(<Loader isLoading />);
        expect(
            container.querySelector('.onboarding-loader-pending')
        ).not.toBeNull();
        advance(299);
        expect(container.querySelector('.tlv-loader')).toBeNull();
        advance(1);
        expect(container.querySelector('.tlv-loader')).not.toBeNull();
        expect(
            container.querySelector('.onboarding-loader-pending')
        ).toBeNull();
    });

    it('never flashes the spinner for a fast request', () => {
        const { container, rerender } = render(<Loader isLoading />);
        advance(150);
        rerender(<Loader isLoading={false} />);
        advance(500);
        expect(
            container.querySelector('.onboarding-loader-backdrop')
        ).toBeNull();
        expect(container.querySelector('.tlv-loader')).toBeNull();
    });

    it('keeps the same animated element across chained requests', () => {
        const { container, rerender } = render(<Loader isLoading />);
        advance(300);
        const spinner = container.querySelector('.tlv-loader');
        rerender(<Loader isLoading={false} />);
        advance(50);
        rerender(<Loader isLoading />);
        advance(200);
        expect(container.querySelector('.tlv-loader')).toBe(spinner);
        rerender(<Loader isLoading={false} />);
        advance(100);
        expect(container.querySelector('.tlv-loader')).toBeNull();
    });

    it('measures the display delay across the complete request chain', () => {
        const { container, rerender } = render(<Loader isLoading />);
        advance(200);
        rerender(<Loader isLoading={false} />);
        advance(10);
        rerender(<Loader isLoading />);
        advance(90);
        expect(container.querySelector('.tlv-loader')).not.toBeNull();
    });

    it('does not show feedback after a request finishes just before the delay', () => {
        const { container, rerender } = render(<Loader isLoading />);
        advance(290);
        rerender(<Loader isLoading={false} />);
        advance(20);
        expect(container.querySelector('.tlv-loader')).toBeNull();
        advance(100);
        expect(
            container.querySelector('.onboarding-loader-backdrop')
        ).toBeNull();
    });

    it('schedules no timer while idle', () => {
        render(<Loader isLoading={false} />);
        expect(jest.getTimerCount()).toBe(0);
    });

    it('cancels pending timers on unmount', () => {
        const { unmount } = render(<Loader isLoading />);
        unmount();
        expect(jest.getTimerCount()).toBe(0);
    });
});
