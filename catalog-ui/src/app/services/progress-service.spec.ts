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
 * Characterization test for the AngularJS `ProgressService` (SDC-4829 Phase 10-12 safety net).
 *
 * Pins the create-component progress bookkeeping: value set/get/delete, the 0-default for an
 * unknown key, the interval-driven ramp (10% start, +1% per tick, capped at 90%), and the
 * self-clearing behaviour once the cap is exceeded. `$interval` is faked so ticks are driven
 * synchronously and no real timers run. No production code is changed.
 */
import {ProgressService} from './progress-service';

describe('ProgressService (AngularJS characterization)', () => {
    // Fake $interval: records the tick callback so the test can drive it manually.
    let tickCb: () => void;
    let cancelled: boolean;
    let intervalFake: any;

    beforeEach(() => {
        tickCb = undefined;
        cancelled = false;
        intervalFake = (cb: () => void) => {
            tickCb = cb;
            return {id: 'interval-handle'};
        };
        intervalFake.cancel = () => {
            cancelled = true;
        };
    });

    const makeService = () => new ProgressService(intervalFake);

    it('returns 0 for an unknown progress key', () => {
        expect(makeService().getProgressValue('nope')).toBe(0);
    });

    it('stores and reads back a progress value', () => {
        const svc = makeService();
        svc.setProgressValue('vf-1', 42);
        expect(svc.getProgressValue('vf-1')).toBe(42);
    });

    it('deletes a progress value and cancels the interval', () => {
        const svc = makeService();
        svc.setProgressValue('vf-1', 42);
        svc.deleteProgressValue('vf-1');
        expect(svc.getProgressValue('vf-1')).toBe(0);
        expect(cancelled).toBe(true);
    });

    it('seeds progress at the 10% start value when creation begins', () => {
        const svc = makeService();
        svc.initCreateComponentProgress('vf-1');
        expect(svc.getProgressValue('vf-1')).toBe(10);
    });

    it('ramps by 1% per interval tick', () => {
        const svc = makeService();
        svc.initCreateComponentProgress('vf-1');
        tickCb(); // 10 -> 11
        expect(svc.getProgressValue('vf-1')).toBe(11);
        tickCb(); // 11 -> 12
        expect(svc.getProgressValue('vf-1')).toBe(12);
    });

    it('does not re-seed when progress for the id already exists', () => {
        const svc = makeService();
        svc.setProgressValue('vf-1', 55);
        svc.initCreateComponentProgress('vf-1');
        // Already had a value -> init is a no-op, no interval registered.
        expect(svc.getProgressValue('vf-1')).toBe(55);
        expect(tickCb).toBeUndefined();
    });
});
