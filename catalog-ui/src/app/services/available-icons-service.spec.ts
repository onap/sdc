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
 * Characterization test for the AngularJS `AvailableIconsService` (SDC-4829 Phase 10-12).
 *
 * The service returns a fixed icon-name list per component type. The lists themselves live in
 * the icons-modal migration scope (Phase 8 CR3 / 9); this spec pins the contract that the icon
 * picker depends on: SERVICE and RESOURCE each return their known set, and any other type
 * returns undefined (no default branch in the switch).
 */
import {AvailableIconsService} from './available-icons-service';
import {ComponentType} from '../utils/constants';

describe('AvailableIconsService (AngularJS characterization)', () => {
    let service: AvailableIconsService;

    beforeEach(() => {
        service = new AvailableIconsService();
    });

    it('returns the service icon set for SERVICE', () => {
        const icons = service.getIcons(ComponentType.SERVICE);
        expect(icons).toEqual(['call_controll', 'mobility', 'network_l_1-3', 'network_l_4']);
    });

    it('returns the resource icon set for RESOURCE (starting with router/database)', () => {
        const icons = service.getIcons(ComponentType.RESOURCE);
        expect(icons[0]).toBe('router');
        expect(icons).toContain('firewall');
        expect(icons.length).toBeGreaterThan(20);
    });

    it('returns undefined for an unknown component type (no default branch)', () => {
        expect(service.getIcons('PRODUCT')).toBeUndefined();
    });
});
