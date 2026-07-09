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
 * Characterization test for `LoaderService` (SDC-4829 Phase 10-12 safety net).
 *
 * Thin wrapper over EventListenerService: showLoader/hideLoader notify observers of the
 * per-type SHOW_LOADER_EVENT / HIDE_LOADER_EVENT names, forwarding any extra args. Pins the
 * exact event-name composition (base event + loaderType suffix) that loader directives listen on.
 */
import {LoaderService} from './loader-service';
import {EVENTS} from '../utils/constants';

describe('LoaderService (characterization)', () => {
    let notifySpy: jest.Mock;
    let service: LoaderService;

    beforeEach(() => {
        notifySpy = jest.fn();
        service = new LoaderService({notifyObservers: notifySpy} as any);
    });

    it('notifies the type-suffixed SHOW_LOADER_EVENT with forwarded args', () => {
        service.showLoader('global', {size: 'large'});
        expect(notifySpy).toHaveBeenCalledWith(EVENTS.SHOW_LOADER_EVENT + 'global', {size: 'large'});
    });

    it('notifies the type-suffixed HIDE_LOADER_EVENT', () => {
        service.hideLoader('global');
        expect(notifySpy).toHaveBeenCalledWith(EVENTS.HIDE_LOADER_EVENT + 'global');
    });
});
