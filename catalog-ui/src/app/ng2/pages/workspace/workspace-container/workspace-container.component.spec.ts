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

// `automated-upgrade.service.ts` — a transitive import of the component under test — does
// `import Dictionary = _.Dictionary`, which TypeScript emits as a RUNTIME read of the global `_`
// that webpack's script-loader supplies in the browser but Jest does not. The global must be
// seeded BEFORE the component module is evaluated, which an ES import (hoisted above all
// statements) cannot guarantee — hence require().
import {ChangeDetectionStrategy} from '@angular/core';

(window as any)._ = require('lodash');
// tslint:disable-next-line:no-var-requires
const WorkspaceContainerComponent = require('./workspace-container.component').WorkspaceContainerComponent;

// Direct construction rather than TestBed.createComponent: this is the house idiom for the
// workspace components (see general-tab/__tests__/general-tab.component.spec.ts), and it avoids
// compiling a template that hosts <router-outlet> and <top-nav> for tests that only exercise
// pure methods.
function createComponent(overrides: any = {}) {
    const sdcMenu: any = {roles: {}, LifeCycleStatuses: {}, DistributionStatuses: {}, component_workspace_menu_option: {}};
    const componentFactory: any = {createComponent: (c: any) => c};
    const menuHandler: any = {generateBreadcrumbsModelFromComponents: jest.fn()};
    const changeLifecycleStateHandler: any = {changeLifecycleState: jest.fn()};
    const progressService: any = {initCreateComponentProgress: jest.fn(), deleteProgressValue: jest.fn()};
    const cacheService: any = {get: jest.fn(), set: jest.fn(), remove: jest.fn()};
    const eventListenerService: any = {registerObserverCallback: jest.fn(), unRegisterObserver: jest.fn(), notifyObservers: jest.fn()};
    const homeService: any = {getAllComponents: jest.fn()};
    const catalogService: any = {getCatalog: jest.fn()};
    const componentServiceNg2: any = {getDependencies: jest.fn()};
    const automatedUpgradeService: any = {openAutomatedUpgradeModal: jest.fn()};
    const eventBusService: any = {notify: jest.fn()};
    const modalServiceSdcUI: any = {openInfoModal: jest.fn()};
    const pluginsService: any = {isPluginDisplayedInContext: jest.fn(() => false)};
    const workspaceNg1BridgeService: any = {updateIsViewOnly: jest.fn()};
    const workspaceService: any = overrides.workspaceService || {component: undefined, containerActions: undefined};
    const translateService: any = {translate: (k: string) => k};
    const navigationService: any = overrides.navigationService || {
        getParam: jest.fn(),
        getParams: jest.fn(() => ({})),
        getCurrentStateName: jest.fn(() => 'workspace.general'),
        onNavigationSuccess: jest.fn(() => jest.fn()),
        navigate: jest.fn()
    };
    const notificationsService: any = {push: jest.fn()};
    const cdr: any = {detectChanges: jest.fn()};

    const comp = new WorkspaceContainerComponent(
        cacheService, eventListenerService, homeService, catalogService, componentServiceNg2,
        automatedUpgradeService, eventBusService, modalServiceSdcUI, pluginsService,
        workspaceNg1BridgeService, workspaceService, translateService, navigationService,
        notificationsService, cdr, sdcMenu, componentFactory, menuHandler,
        changeLifecycleStateHandler, progressService
    );
    return {comp, workspaceService, navigationService, cdr, eventListenerService};
}

describe('menu state matching', () => {
    let component: any;

    beforeEach(() => {
        component = createComponent().comp;
    });

    it('matches a dotted menu.js state', () => {
        expect((component as any).isMenuState({state: 'workspace.general'} as any, 'general')).toBe(true);
    });
    it('matches a bare DataType menu.js state', () => {
        expect((component as any).isMenuState({state: 'general'} as any, 'general')).toBe(true);
    });
    it('does not match a different tab', () => {
        expect((component as any).isMenuState({state: 'workspace.properties'} as any, 'general')).toBe(false);
    });
    it('disables only non-general tabs in create mode', () => {
        const items = [{state: 'workspace.general'}, {state: 'workspace.properties'}] as any[];
        (component as any).applyCreateModeDisabling(items, true);
        expect(items.map((i) => i.isDisabled)).toEqual([false, true]);
    });

    it('tolerates a menu item with no state', () => {
        expect((component as any).isMenuState({} as any, 'general')).toBe(false);
        expect((component as any).isMenuState(undefined, 'general')).toBe(false);
    });

    it('disables nothing outside create mode except a category-disabled item', () => {
        const items = [{state: 'workspace.general'}, {state: 'workspace.properties'}, {state: 'workspace.deployment_artifacts', disabledCategory: true}] as any[];
        (component as any).applyCreateModeDisabling(items, false);
        expect(items.map((i) => i.isDisabled)).toEqual([false, false, true]);
    });

    it('disables the dotted Deployment tab when a VF has no modules', () => {
        const {comp} = createComponent();
        (comp as any).component = {modules: [], isResource: () => true};
        const items = [{state: 'workspace.general'}, {state: 'workspace.deployment'}] as any[];
        (comp as any).applyCreateModeDisabling(items, false);
        expect(items.map((i) => i.isDisabled)).toEqual([false, true]);
    });
});

describe('isGeneralView', () => {
    it('is true on the General tab and false elsewhere', () => {
        const navigationService: any = {
            getParam: jest.fn(),
            getParams: jest.fn(() => ({})),
            getCurrentStateName: jest.fn(() => 'workspace.general'),
            onNavigationSuccess: jest.fn(() => jest.fn()),
            navigate: jest.fn()
        };
        const {comp} = createComponent({navigationService});
        expect(comp.isGeneralView()).toBe(true);
        navigationService.getCurrentStateName = jest.fn(() => 'workspace.properties');
        expect(comp.isGeneralView()).toBe(false);
    });
});

describe('disableMenuItems / enableMenuItems', () => {
    it('leaves the General tab enabled for both the dotted and the bare form', () => {
        const {comp} = createComponent();
        (comp as any).leftBarTabs = {
            menuItems: [{state: 'workspace.general'}, {state: 'general'}, {state: 'workspace.properties'}, {state: 'properties'}]
        };
        (comp as any).disableMenuItems();
        expect((comp as any).leftBarTabs.menuItems.map((i: any) => i.isDisabled)).toEqual([false, false, true, true]);
        (comp as any).enableMenuItems();
        expect((comp as any).leftBarTabs.menuItems.map((i: any) => i.isDisabled)).toEqual([false, false, false, false]);
    });
});

describe('updateSelectedMenuItem', () => {
    it('selects the dotted menu item for a dotted state name', () => {
        const {comp} = createComponent();
        (comp as any).leftBarTabs = {menuItems: [{state: 'workspace.general'}, {state: 'workspace.properties'}]};
        (comp as any).updateSelectedMenuItem('workspace.properties');
        expect((comp as any).leftBarTabs.selectedIndex).toBe(1);
    });

    it('selects the bare DataType menu item for a dotted state name', () => {
        const {comp} = createComponent();
        (comp as any).leftBarTabs = {menuItems: [{state: 'general'}, {state: 'properties'}]};
        (comp as any).updateSelectedMenuItem('workspace.properties');
        expect((comp as any).leftBarTabs.selectedIndex).toBe(1);
    });

    it('clears the selection on the full-bleed composition tab', () => {
        const {comp} = createComponent();
        (comp as any).leftBarTabs = {menuItems: [{state: 'workspace.general'}]};
        (comp as any).isComposition = true;
        (comp as any).updateSelectedMenuItem('workspace.composition');
        expect((comp as any).leftBarTabs.selectedIndex).toBe(-1);
    });
});

describe('ngOnInit component feed', () => {
    it('reads the component from WorkspaceService and exposes the container actions', () => {
        const workspaceService: any = {component: undefined, containerActions: undefined};
        const {comp} = createComponent({workspaceService});
        comp.ngOnInit();
        expect(workspaceService.containerActions).toBe(comp);
        expect(comp.component).toBeUndefined();
    });
});

// Since the router swap this shell is a <router-outlet> CHILD view rather than a downgradeComponent
// whose host view was attached to ApplicationRef. tick() detectChanges()es each ROOT directly and so
// bypasses the OnPush ChecksEnabled gate, but a child is reached via callViewAction, which enforces
// it — and an unchecked OnPush parent skips its ENTIRE subtree. Every workspace tab is now a
// descendant of this component, so OnPush here starves all of them: a tab renders its empty initial
// state and never re-renders when its own XHRs resolve, because a resolving request marks no view.
// That took out the properties table and the interface list (13 Selenium methods). Default strategy
// restores the pre-swap semantics for the whole subtree.
describe('change detection strategy', () => {
    it('is Default, not OnPush', () => {
        // Reading the decorator metadata rather than rendering: asserting this through a TestBed
        // would mean compiling the shell's <router-outlet>/<top-nav> template. The @Component
        // decorator always materialises `changeDetection`, so compare against the enum.
        const annotations = (WorkspaceContainerComponent as any).__annotations__ || [];
        expect(annotations.length).toBeGreaterThan(0);
        expect(annotations[0].changeDetection).toBe(ChangeDetectionStrategy.Default);
        expect(annotations[0].changeDetection).not.toBe(ChangeDetectionStrategy.OnPush);
    });
});
