/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2026 Deutsche Telekom AG.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {AttributesOutputsComponent} from './attributes-outputs.page.component';
import {EventListenerService} from 'app/services/event-listener.service';
import {EVENTS, WorkspaceMode} from 'app/utils/constants';

describe('AttributesOutputsComponent certify', () => {
    const build = () => {
        const componentModeService = {getComponentMode: jest.fn(() => WorkspaceMode.EDIT)};
        const eventListenerService = new EventListenerService();
        const deps: any[] = new Array(14).fill({});
        deps[6] = {component: {uniqueId: 'vf-1', model: null}};
        deps[8] = componentModeService;
        deps[9] = eventListenerService;
        const comp = new (AttributesOutputsComponent as any)(...deps) as AttributesOutputsComponent;
        return {comp, componentModeService, eventListenerService};
    };

    it('turns read-only when the component is certified while the tab is open', () => {
        const {comp, componentModeService, eventListenerService} = build();
        expect(comp.isReadonly).toBe(false);
        componentModeService.getComponentMode.mockReturnValue(WorkspaceMode.VIEW);
        eventListenerService.notifyObservers(EVENTS.ON_CERTIFY);
        expect(comp.isReadonly).toBe(true);
    });

    it('stops listening for certify on destroy', () => {
        const {comp, eventListenerService} = build();
        comp.ngOnDestroy();
        expect(eventListenerService.observerCallbacks.containsKey(EVENTS.ON_CERTIFY)).toBe(false);
    });
});
