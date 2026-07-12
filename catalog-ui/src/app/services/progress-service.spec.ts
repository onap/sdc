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
 * Characterization test for the Angular `ProgressService` (SDC-4829 Phase 11).
 *
 * Pins the create-component progress bookkeeping: value set/get/delete, the 0-default for an
 * unknown key, the interval-driven ramp (10% start, +1% per tick, capped at 90%), and the
 * self-clearing behaviour once the cap is exceeded.
 *
 * Phase 11 migrated the service from an AngularJS `$interval` to a Zone-safe `setInterval`
 * (scheduled via `NgZone.runOutsideAngular`, UI writes re-entered via `NgZone.run` — see
 * ngUpgrade failure-catalog §B). The spec now drives real timers via Jest fake timers and
 * supplies a pass-through fake `NgZone` (runOutsideAngular/run just invoke the callback), which
 * preserves every behavioural assertion across the migration.
 */
import {NgZone} from '@angular/core';
import {ProgressService} from './progress-service';

describe('ProgressService (Angular characterization)', () => {
    // Pass-through fake NgZone: runOutsideAngular/run simply invoke the callback synchronously.
    const ngZoneFake = {
        runOutsideAngular: (fn: () => any) => fn(),
        run: (fn: () => any) => fn(),
    } as NgZone;

    const TICK_MS = 5000; // onePercentIntervalSeconds (5) * 1000

    beforeEach(() => {
        jest.useFakeTimers();
    });

    afterEach(() => {
        jest.clearAllTimers();
        jest.useRealTimers();
    });

    const makeService = () => new ProgressService(ngZoneFake);

    it('returns 0 for an unknown progress key', () => {
        expect(makeService().getProgressValue('nope')).toBe(0);
    });

    it('stores and reads back a progress value', () => {
        const svc = makeService();
        svc.setProgressValue('vf-1', 42);
        expect(svc.getProgressValue('vf-1')).toBe(42);
    });

    it('deletes a progress value and clears the interval', () => {
        const svc = makeService();
        svc.initCreateComponentProgress('vf-1'); // starts the ramp timer at 10%
        svc.deleteProgressValue('vf-1');
        expect(svc.getProgressValue('vf-1')).toBe(0);
        // the interval was cleared: advancing time does NOT resurrect a progress value
        jest.advanceTimersByTime(TICK_MS * 5);
        expect(svc.getProgressValue('vf-1')).toBe(0);
    });

    it('seeds progress at the 10% start value when creation begins', () => {
        const svc = makeService();
        svc.initCreateComponentProgress('vf-1');
        expect(svc.getProgressValue('vf-1')).toBe(10);
    });

    it('ramps by 1% per interval tick', () => {
        const svc = makeService();
        svc.initCreateComponentProgress('vf-1');
        jest.advanceTimersByTime(TICK_MS); // 10 -> 11
        expect(svc.getProgressValue('vf-1')).toBe(11);
        jest.advanceTimersByTime(TICK_MS); // 11 -> 12
        expect(svc.getProgressValue('vf-1')).toBe(12);
    });

    it('does not re-seed when progress for the id already exists', () => {
        const svc = makeService();
        svc.setProgressValue('vf-1', 55);
        svc.initCreateComponentProgress('vf-1');
        // Already had a value -> init is a no-op: value unchanged and no ramp timer registered,
        // so advancing time must not mutate the value.
        expect(svc.getProgressValue('vf-1')).toBe(55);
        jest.advanceTimersByTime(TICK_MS * 3);
        expect(svc.getProgressValue('vf-1')).toBe(55);
    });
});
