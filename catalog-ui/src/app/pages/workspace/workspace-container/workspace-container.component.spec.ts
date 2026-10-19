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
    const componentService: any = {getDependencies: jest.fn()};
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
    const routeMetadataService: any = overrides.routeMetadataService || {setPageTitle: jest.fn()};

    const comp = new WorkspaceContainerComponent(
        cacheService, eventListenerService, homeService, catalogService, componentService,
        automatedUpgradeService, eventBusService, modalServiceSdcUI, pluginsService,
        workspaceNg1BridgeService, workspaceService, translateService, navigationService,
        notificationsService, cdr, sdcMenu, componentFactory, menuHandler,
        changeLifecycleStateHandler, progressService, routeMetadataService
    );
    return {comp, workspaceService, navigationService, cdr, eventListenerService, changeLifecycleStateHandler, workspaceNg1BridgeService};
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

    // The breadcrumb renders menuItems[selectedIndex].text, so it needs the composition item to
    // have a name. The menu item's state carries a panel-tab suffix the URL may not, which the
    // AngularJS shell absorbed by comparing only the first two segments.
    ['workspace.composition', 'workspace.composition.details', 'workspace.composition.properties'].forEach((state) => {
        it(`selects the composition menu item on ${state}`, () => {
            const {comp} = createComponent();
            (comp as any).leftBarTabs = {menuItems: [{state: 'workspace.general'}, {state: 'workspace.composition.details'}]};
            (comp as any).isComposition = true;
            (comp as any).updateSelectedMenuItem(state);
            expect((comp as any).leftBarTabs.selectedIndex).toBe(1);
        });
    });

    it('selects the deployment menu item', () => {
        const {comp} = createComponent();
        (comp as any).leftBarTabs = {menuItems: [{state: 'workspace.general'}, {state: 'workspace.deployment'}]};
        (comp as any).isDeployment = true;
        (comp as any).updateSelectedMenuItem('workspace.deployment');
        expect((comp as any).leftBarTabs.selectedIndex).toBe(1);
    });

    it('selects the plugin menu item matching the current path', () => {
        const {comp, navigationService} = createComponent();
        navigationService.getParam = jest.fn((key: string) => key === 'path' ? 'pluginB' : undefined);
        (comp as any).leftBarTabs = {menuItems: [
            {state: 'workspace.general'},
            {state: 'plugins', params: {path: 'pluginA'}},
            {state: 'plugins', params: {path: 'pluginB'}}
        ]};
        (comp as any).isPlugins = true;
        (comp as any).updateSelectedMenuItem('workspace.plugins');
        expect((comp as any).leftBarTabs.selectedIndex).toBe(2);
    });
});

// The three flags drive the *ngIf on the 110px tab-title header, and workspace.less subtracts
// @tab_title for exactly the same three states. `isPlugins` was declared and assigned NOWHERE (the
// AngularJS controller set it at workspace-view-model.ts:760; the port kept its two neighbours and
// dropped it), so plugin tabs rendered a header the old shell suppressed. Asserted per state because the composition check is an indexOf — its state name
// carries a panel-tab suffix — while the other two are exact comparisons.
describe('updateFullBleedFlags', () => {
    const flagsFor = (stateName: string) => {
        const navigationService: any = {
            getParam: jest.fn(), getParams: jest.fn(() => ({})),
            getCurrentStateName: jest.fn(() => stateName),
            onNavigationSuccess: jest.fn(() => jest.fn()), navigate: jest.fn()
        };
        const {comp} = createComponent({navigationService});
        (comp as any).updateFullBleedFlags();
        return {
            isComposition: (comp as any).isComposition,
            isDeployment: (comp as any).isDeployment,
            isPlugins: (comp as any).isPlugins
        };
    };

    it('flags the plugins state, which nothing set before', () => {
        expect(flagsFor('workspace.plugins')).toEqual(
            {isComposition: false, isDeployment: false, isPlugins: true});
    });

    it('flags composition including its panel-tab suffix', () => {
        expect(flagsFor('workspace.composition')).toEqual(
            {isComposition: true, isDeployment: false, isPlugins: false});
        expect(flagsFor('workspace.composition.details')).toEqual(
            {isComposition: true, isDeployment: false, isPlugins: false});
    });

    it('flags deployment but not the similarly-named deployment_artifacts tab', () => {
        expect(flagsFor('workspace.deployment')).toEqual(
            {isComposition: false, isDeployment: true, isPlugins: false});
        // A titled tab whose state name is a PREFIX-match of the full-bleed one: an indexOf here
        // would suppress its heading and hand it 110px of dead space.
        expect(flagsFor('workspace.deployment_artifacts')).toEqual(
            {isComposition: false, isDeployment: false, isPlugins: false});
    });

    it('leaves every flag clear on an ordinary titled tab', () => {
        expect(flagsFor('workspace.general')).toEqual(
            {isComposition: false, isDeployment: false, isPlugins: false});
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

describe('certify', () => {
    function certifyFromEditMode() {
        const navigationService: any = {
            getParam: jest.fn((k: string) => k === 'id' ? 'uid-1' : undefined),
            getCurrentStateName: jest.fn(() => 'workspace.deployment'),
            onNavigationSuccess: jest.fn(() => jest.fn()),
            navigate: jest.fn()
        };
        const ctx = createComponent({navigationService});
        const {comp} = ctx;
        comp.user = {userId: 'cs0008'};
        comp.role = 'DESIGNER';
        comp.component = {
            lifecycleState: 'NOT_CERTIFIED_CHECKOUT', lastUpdaterUserId: 'cs0008',
            isService: () => false, isResource: () => true
        };
        comp.mode = 'edit';
        comp.changeLifecycleStateButtons = {certify: {text: 'Certify', url: 'lifecycleState/certify'}};
        jest.spyOn(comp as any, 'handleCertification').mockImplementation(() => undefined);
        jest.spyOn(comp as any, 'verifyIfDependenciesExist').mockImplementation(() => undefined);
        comp.handleChangeLifecycleState('certify');
        const onSuccess = ctx.changeLifecycleStateHandler.changeLifecycleState.mock.calls[0][3];
        onSuccess({lifecycleState: 'CERTIFIED'}, 'lifecycleState/certify');
        return ctx;
    }

    it('drops the workspace to VIEW mode without waiting for the dependency check', () => {
        const {comp, workspaceNg1BridgeService} = certifyFromEditMode();
        expect(comp.isViewMode()).toBe(true);
        expect(workspaceNg1BridgeService.updateIsViewOnly).toHaveBeenLastCalledWith(true);
    });

    it('tells the mounted tabs the component was certified', () => {
        const {eventListenerService} = certifyFromEditMode();
        expect(eventListenerService.notifyObservers).toHaveBeenCalledWith('onCertify');
    });
});

describe('onNavigationSuccess', () => {
    function shellShowing(uniqueId: string, resolvedId: string) {
        const workspaceService: any = {component: {uniqueId: resolvedId}};
        const {comp, cdr} = createComponent({workspaceService});
        comp.component = {uniqueId};
        const initWorkspace = jest.spyOn(comp as any, 'initWorkspace').mockImplementation(() => undefined);
        const updateSelectedMenuItem = jest.spyOn(comp as any, 'updateSelectedMenuItem').mockImplementation(() => undefined);
        jest.spyOn(comp as any, 'updatePageTitle').mockImplementation(() => undefined);
        (comp as any).onNavigationSuccess();
        return {initWorkspace, updateSelectedMenuItem, cdr};
    }

    it('re-initialises the reused shell when the navigation resolved another component', () => {
        const {initWorkspace, cdr} = shellShowing('uid-0.2', 'uid-0.1');
        expect(initWorkspace).toHaveBeenCalledTimes(1);
        expect(cdr.detectChanges).toHaveBeenCalled();
    });

    it('only follows the tab when the component is unchanged', () => {
        const {initWorkspace, updateSelectedMenuItem} = shellShowing('uid-0.2', 'uid-0.2');
        expect(initWorkspace).not.toHaveBeenCalled();
        expect(updateSelectedMenuItem).toHaveBeenCalledWith('workspace.general');
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

describe('main landmark', () => {
    // tslint:disable-next-line:no-var-requires
    const template: string = require('./workspace-container.component.html');
    const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');

    // axe's landmark-banner-is-top-level only exempts a <header> inside a NATIVE <main>; a
    // role="main" div is itself a landmark, so every projected expand-collapse <header> would fail.
    it('is a native <main> carrying the skip-link target', () => {
        const main = doc.getElementById('main-content');
        expect(main.tagName.toLowerCase()).toBe('main');
        expect(main.className).toBe('w-sdc-main-container');
        expect(main.hasAttribute('role')).toBe(false);
        expect(doc.querySelectorAll('[role="main"], main').length).toBe(1);
    });
});

describe('page title', () => {
    it('names the tab first, then the asset', () => {
        const {comp} = createComponent();
        const routeMetadataService = (comp as any).routeMetadataService;
        (comp as any).leftBarTabs = {menuItems: [{text: 'General'}, {text: 'Composition'}], selectedIndex: 1};
        (comp as any).menuComponentTitle = 'vFW';
        (comp as any).updatePageTitle();
        expect(routeMetadataService.setPageTitle).toHaveBeenCalledWith('Composition', 'vFW');
    });

    it('uses the create heading while the new asset has no name', () => {
        const navigationService: any = {
            getParam: jest.fn((key: string) => key === 'type' ? 'service' : undefined),
            getParams: jest.fn(() => ({})),
            getCurrentStateName: jest.fn(() => 'workspace.general'),
            onNavigationSuccess: jest.fn(() => jest.fn()),
            navigate: jest.fn()
        };
        const {comp} = createComponent({navigationService});
        (comp as any).leftBarTabs = {menuItems: [{text: 'General'}], selectedIndex: 0};
        (comp as any).menuComponentTitle = '';
        (comp as any).updatePageTitle();
        expect((comp as any).routeMetadataService.setPageTitle).toHaveBeenCalledWith('General', 'Create new service');
    });
});

describe('reading order and current location', () => {
    // tslint:disable-next-line:no-var-requires
    const template: string = require('./workspace-container.component.html');
    const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');

    it('renders the top navigation before main', () => {
        const topNav = doc.querySelector('top-nav');
        const main = doc.querySelector('main');
        // tslint:disable-next-line:no-bitwise
        expect(topNav.compareDocumentPosition(main) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    });

    it('marks the selected left-menu button as the current page', () => {
        const button = doc.querySelector('nav[aria-label="Workspace"] button');
        expect(button.getAttribute('[attr.aria-current]')).toBe("isSelected(menuItem) ? 'page' : null");
    });

    it('gives the visible tab title and the deployment tab a focusable tab heading', () => {
        const headings = Array.from(doc.querySelectorAll('h2[data-tab-heading]'));
        expect(headings.length).toBe(2);
        expect(headings[1].getAttribute('class')).toBe('sr-only');
        expect(headings[1].getAttribute('*ngIf')).toBe('isDeployment');
    });
});

describe('getLatestVersion', () => {
    // allVersions maps version number -> component unique id, in whatever order the BE's map
    // serialised it, so neither the key nor key order is usable as a navigation target.
    it('navigates to the unique id of the highest version', () => {
        const {comp, navigationService} = createComponent();
        (comp as any).eventBusService.notify = jest.fn(() => ({subscribe: (cb: () => void) => cb()}));
        (comp as any).component = {
            version: '1.0',
            componentType: 'RESOURCE',
            isLatestVersion: () => false,
            allVersions: {'2.0': 'uid-2.0', '1.10': 'uid-1.10', '1.0': 'uid-1.0', '1.9': 'uid-1.9'},
            getAllVersionsAsSortedArray: () => [
                {versionNumber: '1.0', versionId: 'uid-1.0'},
                {versionNumber: '1.9', versionId: 'uid-1.9'},
                {versionNumber: '1.10', versionId: 'uid-1.10'},
                {versionNumber: '2.0', versionId: 'uid-2.0'}
            ]
        };
        (comp as any).initVersionObject();

        comp.getLatestVersion();

        expect(navigationService.navigate).toHaveBeenCalledWith('workspace.general',
            expect.objectContaining({id: 'uid-2.0', type: 'resource'}));
    });
});

describe('readOnlyNotice', () => {
    const notice = (mode: string, role: string, component: any) => {
        const {comp} = createComponent();
        comp.translateService = {translate: (key: string, args: any) => `${key}|${args.name}`};
        comp.mode = mode;
        comp.role = role;
        comp.component = {isArchived: false, isLatestVersion: () => true, lastUpdaterFullName: 'Jane Doe', ...component};
        return comp.readOnlyNotice();
    };

    it('is empty while the workspace is editable or being created', () => {
        expect(notice('edit', 'DESIGNER', {lifecycleState: 'NOT_CERTIFIED_CHECKOUT'})).toBe('');
        expect(notice('create', 'DESIGNER', {})).toBe('');
    });

    it('says why a view-mode workspace is read-only, the first matching reason winning', () => {
        expect(notice('view', 'DESIGNER', {lifecycleState: 'CERTIFIED'})).toBe('WORKSPACE_READ_ONLY_CERTIFIED|Jane Doe');
        expect(notice('view', 'DESIGNER', {lifecycleState: 'NOT_CERTIFIED_CHECKIN'})).toBe('WORKSPACE_READ_ONLY_CHECKED_IN|Jane Doe');
        expect(notice('view', 'DESIGNER', {lifecycleState: 'NOT_CERTIFIED_CHECKOUT'})).toBe('WORKSPACE_READ_ONLY_CHECKED_OUT_BY_OTHER|Jane Doe');
        expect(notice('view', 'DESIGNER', {lifecycleState: 'CERTIFIED', isLatestVersion: () => false})).toBe('WORKSPACE_READ_ONLY_NOT_LATEST|Jane Doe');
        expect(notice('view', 'TESTER', {lifecycleState: 'CERTIFIED', isLatestVersion: () => false})).toBe('WORKSPACE_READ_ONLY_ROLE|Jane Doe');
        expect(notice('view', 'TESTER', {lifecycleState: 'CERTIFIED', isArchived: true})).toBe('WORKSPACE_READ_ONLY_ARCHIVED|Jane Doe');
    });
});

describe('workspace-container template', () => {
    // tslint:disable-next-line:no-var-requires
    const template: string = require('./workspace-container.component.html');
    const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');

    it('puts the read-only notice in the tab-title strip, after the heading', () => {
        const notice = doc.querySelector('[data-tests-id="read-only-notice"]');
        expect(notice.parentElement.classList.contains('w-sdc-main-container-body-content-header')).toBe(true);
        expect(notice.previousElementSibling.tagName).toBe('H2');
        expect(notice.getAttribute('*ngIf')).toBe('readOnlyNotice()');
    });
});
