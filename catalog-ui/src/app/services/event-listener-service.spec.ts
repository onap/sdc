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

/**
 * Characterization test for `EventListenerService` (SDC-4829 Phase 10-12 safety net).
 *
 * This is the cross-framework event bus used throughout catalog-ui (already an @Injectable,
 * bridged both ways). It has NO spec today despite being in the coverage scope. Pins the
 * observer registry contract that the whole app depends on:
 *  - register + notify invokes callbacks with the notify args;
 *  - duplicate callbacks (by .toString()) are de-duplicated on register;
 *  - unregister with a single observer removes the whole event key;
 *  - unregister with multiple observers removes only the matching callback;
 *  - notify for an unknown event is a no-op.
 */
import {EventListenerService} from './event-listener-service';

describe('EventListenerService (characterization)', () => {
    let bus: EventListenerService;

    beforeEach(() => {
        bus = new EventListenerService();
    });

    it('invokes a registered callback with the notify arguments', () => {
        const cb = jest.fn();
        bus.registerObserverCallback('EVT', cb);
        bus.notifyObservers('EVT', 'a', 2);
        expect(cb).toHaveBeenCalledWith('a', 2);
    });

    it('invokes multiple distinct callbacks for the same event', () => {
        // NOTE: the de-dup compares callback.toString(), and jest.fn() mocks all stringify
        // identically, so distinct mocks would collide. Use real functions with different
        // bodies (hence different .toString()) and track calls via a side effect.
        const calls: string[] = [];
        const cb1 = () => { calls.push('one'); };
        const cb2 = () => { calls.push('two'); };
        bus.registerObserverCallback('EVT', cb1);
        bus.registerObserverCallback('EVT', cb2);
        bus.notifyObservers('EVT');
        expect(calls).toEqual(['one', 'two']);
    });

    it('de-duplicates callbacks with identical source (registers once)', () => {
        const make = () => () => 'same-body';
        const cbA = make();
        const cbB = make();
        bus.registerObserverCallback('EVT', cbA);
        bus.registerObserverCallback('EVT', cbB); // same .toString() -> not added
        expect(bus.observerCallbacks.getValue('EVT').length).toBe(1);
    });

    it('removes the event key entirely when its only observer is unregistered', () => {
        const cb = jest.fn();
        bus.registerObserverCallback('EVT', cb);
        bus.unRegisterObserver('EVT', cb);
        expect(bus.observerCallbacks.containsKey('EVT')).toBe(false);
    });

    it('removes only the matching callback when several are registered', () => {
        // Distinct-bodied real functions again (see the multi-callback test above for why).
        const calls: string[] = [];
        const cb1 = () => { calls.push('one'); };
        const cb2 = () => { calls.push('two'); };
        bus.registerObserverCallback('EVT', cb1);
        bus.registerObserverCallback('EVT', cb2);
        bus.unRegisterObserver('EVT', cb1);
        bus.notifyObservers('EVT');
        expect(calls).toEqual(['two']);
    });

    it('is a no-op when notifying an event with no observers', () => {
        expect(() => bus.notifyObservers('UNKNOWN', 'x')).not.toThrow();
    });
});
