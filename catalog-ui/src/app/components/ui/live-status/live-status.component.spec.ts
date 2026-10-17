/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

import {SimpleChange} from '@angular/core';
import {LiveStatusComponent, resultCount} from './live-status.component';

describe('LiveStatusComponent', () => {
    let component: LiveStatusComponent;
    let region: HTMLElement;
    let zone: any;

    function set(message: string): void {
        component.message = message;
        component.ngOnChanges({message: new SimpleChange(null, message, false)} as any);
    }

    beforeEach(() => {
        jest.useFakeTimers();
        zone = {runOutsideAngular: jest.fn((fn: Function) => fn())};
        component = new LiveStatusComponent(zone);
        region = document.createElement('div');
        component.region = {nativeElement: region} as any;
    });

    afterEach(() => jest.useRealTimers());

    it('renders a status region that is always present', () => {
        // tslint:disable-next-line:no-var-requires
        const template = (LiveStatusComponent as any).__annotations__[0].template;
        expect(template).toContain('role="status"');
        expect(template).toContain('class="sr-only"');
        expect(template).not.toContain('*ngIf');
    });

    it('announces only the settled message after the delay', () => {
        component.delay = 1000;
        set('3 results');
        jest.advanceTimersByTime(500);
        set('1 result');
        jest.advanceTimersByTime(999);
        expect(region.textContent).toBe('');
        jest.advanceTimersByTime(1);
        expect(region.textContent).toBe('1 result');
        expect(zone.runOutsideAngular).toHaveBeenCalled();
    });

    it('keeps the first known message silent when announceInitial is false', () => {
        component.announceInitial = false;
        set(null);
        set('342 results');
        jest.runAllTimers();
        expect(region.textContent).toBe('');
        set('1 result');
        jest.runAllTimers();
        expect(region.textContent).toBe('1 result');
    });

    it('changes the text when the same message repeats so it is read again', () => {
        set('1 result');
        jest.runAllTimers();
        set('1 result');
        jest.runAllTimers();
        expect(region.textContent).toBe('1 result\u00a0');
    });

    it('drops a pending message when destroyed', () => {
        component.delay = 1000;
        set('2 results');
        component.ngOnDestroy();
        jest.runAllTimers();
        expect(region.textContent).toBe('');
    });

    it('words the count', () => {
        expect([0, 1, 12].map(resultCount)).toEqual(['No results', '1 result', '12 results']);
    });
});
