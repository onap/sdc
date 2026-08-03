/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import {TestBed} from '@angular/core/testing';

import {TypeWorkspaceService} from './type-workspace.service';
import {MenuItem, MenuItemGroup} from '../../../utils/menu-handler';

describe('TypeWorkspaceService', () => {
    let service: TypeWorkspaceService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [TypeWorkspaceService]
        });
        service = TestBed.get(TypeWorkspaceService);
    });

    it('shares leftBarTabs between the menu and the shell', () => {
        const group = new MenuItemGroup(0, [new MenuItem('General', null, 'general', 'goToState')], false);
        service.leftBarTabs = group;
        expect(service.leftBarTabs.menuItems[0].text).toBe('General');
    });

    it('shares the imported file between the shell and the general tab', () => {
        service.importFile = {name: 'dt.yaml'};
        expect(service.importFile.name).toBe('dt.yaml');
    });
});
