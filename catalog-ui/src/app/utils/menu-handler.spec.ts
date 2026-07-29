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
 * Specs for `MenuHandler.generateBreadcrumbsModelFromComponents`, the breadcrumb-label builder
 * behind the workspace top bar.
 *
 * It labels each item with `ResourceNamePipe.getDisplayName(component.name)`. In create mode the
 * selected component has no name yet, and an unguarded helper threw there
 * (`Cannot read properties of undefined (reading 'split')`), aborting
 * `WorkspaceContainerComponent.initBreadcrumbs()` before it assigned `breadcrumbsModel` — so the
 * whole breadcrumb bar was missing from the create form.
 */
import {MenuHandler} from './menu-handler';
import {Component} from '../models';

describe('MenuHandler', () => {
    let handler: MenuHandler;

    const component = (over: any): Component => Object.assign({
        uniqueId: 'uid',
        uuid: 'uuid',
        invariantUUID: 'inv',
        componentType: 'SERVICE',
        getComponentSubType: () => 'SERVICE'
    }, over) as Component;

    beforeEach(() => {
        handler = new MenuHandler(
            {} as any, {} as any, {} as any, {} as any,
            {go: jest.fn()} as any,
            {when: jest.fn()} as any);
    });

    describe('generateBreadcrumbsModelFromComponents', () => {
        it('labels an item with the component subtype and its short display name', () => {
            const selected = component({name: 'org.openecomp.resource.nfv.VnfMain'});

            const group = handler.generateBreadcrumbsModelFromComponents([selected], selected);

            expect(group.menuItems.length).toBe(1);
            expect(group.menuItems[0].text).toBe('SERVICE: VnfMain');
        });

        it('builds a breadcrumb for a component the list does not contain', () => {
            const listed = component({name: 'Other', uuid: 'other-uuid', invariantUUID: 'other-inv'});
            const selected = component({name: 'MyService'});

            const group = handler.generateBreadcrumbsModelFromComponents([listed], selected);

            expect(group.selectedIndex).toBe(0);
            expect(group.menuItems[0].text).toBe('SERVICE: MyService');
        });

        // Create mode for a user who follows nothing: the list is empty but truthy, so the
        // "component not found" branch runs and labels the not-yet-named component. An unguarded
        // display-name helper threw here, and the throw took the whole breadcrumb bar with it
        // (WorkspaceContainerComponent.initBreadcrumbs never reached its breadcrumbsModel
        // assignment). The label itself does not matter — the container unshifts a
        // "Create new <type>" item in front of it and selects that.
        it('does not throw for a nameless component when the list is empty', () => {
            const selected = component({name: undefined, uuid: undefined, invariantUUID: undefined});

            expect(() => handler.generateBreadcrumbsModelFromComponents([], selected)).not.toThrow();

            const group = handler.generateBreadcrumbsModelFromComponents([], selected);
            expect(group.menuItems.length).toBe(1);
            expect(group.selectedIndex).toBe(0);
        });

        it('returns an empty group when there are no components', () => {
            const group = handler.generateBreadcrumbsModelFromComponents(undefined, component({name: 'X'}));

            expect(group.menuItems.length).toBe(0);
        });
    });
});
