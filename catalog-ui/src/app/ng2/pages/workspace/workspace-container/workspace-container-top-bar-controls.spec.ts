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
 * Pins the five workspace top-bar controls that f89b30141 dropped when it replaced the AngularJS
 * WorkspaceViewModel: Upgrade/Update Services, Restore, Delete version, Delete (archived) and
 * Archive, plus their four handlers. All five were reachable in the AngularJS template and none of
 * them existed in the Angular shell afterwards, so no test failed.
 */

// See workspace-container.component.spec.ts: `automated-upgrade.service.ts` reads the global `_` at
// module-evaluation time, which an ES import cannot be sequenced after.
(window as any)._ = require('lodash');
// tslint:disable-next-line:no-var-requires
const WorkspaceContainerComponent = require('./workspace-container.component').WorkspaceContainerComponent;
// tslint:disable-next-line:no-var-requires
const template: string = require('./workspace-container.component.html');

import {Role, WorkspaceMode} from 'app/ng2/utils/constants';
import 'rxjs/add/observable/of';
import 'rxjs/add/observable/throw';
import {Observable} from 'rxjs/Observable';

function makeComponent(overrides: any = {}) {
    return {
        uniqueId: 'comp-1',
        name: 'MyService',
        componentType: 'SERVICE',
        lifecycleState: 'NOT_CERTIFIED_CHECKOUT',
        isArchived: false,
        isService: () => true,
        isResource: () => false,
        isLatestVersion: () => true,
        getComponentSubType: () => 'SERVICE',
        ...overrides
    };
}

function createComponent(overrides: any = {}) {
    const sdcMenu: any = {
        roles: {},
        LifeCycleStatuses: {},
        DistributionStatuses: {},
        component_workspace_menu_option: {},
        alertMessages: {okButton: 'OK'}
    };
    const componentFactory: any = {createComponent: (c: any) => c};
    const menuHandler: any = {generateBreadcrumbsModelFromComponents: jest.fn()};
    const changeLifecycleStateHandler: any = {changeLifecycleState: jest.fn()};
    const progressService: any = {initCreateComponentProgress: jest.fn(), deleteProgressValue: jest.fn()};
    const cacheService: any = {get: jest.fn(), set: jest.fn(), remove: jest.fn()};
    const eventListenerService: any = {registerObserverCallback: jest.fn(), unRegisterObserver: jest.fn(), notifyObservers: jest.fn()};
    const homeService: any = {getAllComponents: jest.fn()};
    const catalogService: any = {getCatalog: jest.fn()};
    const componentServiceNg2: any = overrides.componentServiceNg2 || {
        getDependencies: jest.fn(),
        archiveComponent: jest.fn(),
        restoreComponent: jest.fn(),
        deleteComponent: jest.fn()
    };
    const automatedUpgradeService: any = {openAutomatedUpgradeModal: jest.fn()};
    const eventBusService: any = {notify: jest.fn()};
    const modalServiceSdcUI: any = {openInfoModal: jest.fn(), openWarningModal: jest.fn()};
    const pluginsService: any = {isPluginDisplayedInContext: jest.fn(() => false)};
    const workspaceNg1BridgeService: any = {updateIsViewOnly: jest.fn()};
    const workspaceService: any = {component: undefined, containerActions: undefined};
    const translateService: any = {translate: (k: string) => k};
    const navigationService: any = overrides.navigationService || {
        getParam: jest.fn(),
        getParams: jest.fn(() => ({})),
        getCurrentStateName: jest.fn(() => 'workspace.general'),
        onNavigationSuccess: jest.fn(() => jest.fn()),
        navigate: jest.fn(),
        reload: jest.fn()
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
    comp.component = overrides.component || makeComponent();
    comp.role = overrides.role !== undefined ? overrides.role : Role.DESIGNER;
    comp.mode = overrides.mode !== undefined ? overrides.mode : WorkspaceMode.EDIT;
    return {comp, componentServiceNg2, automatedUpgradeService, modalServiceSdcUI, navigationService,
        notificationsService, cacheService, cdr};
}

describe('top-bar control visibility', () => {

    describe('showDeleteVersionButton', () => {
        it('shows for a designer on a checked-out component', () => {
            expect(createComponent().comp.showDeleteVersionButton()).toBe(true);
        });
        it('hides for a non-designer', () => {
            expect(createComponent({role: Role.TESTER}).comp.showDeleteVersionButton()).toBe(false);
        });
        it('hides in create mode', () => {
            expect(createComponent({mode: WorkspaceMode.CREATE}).comp.showDeleteVersionButton()).toBe(false);
        });
        it('hides once certified', () => {
            const component = makeComponent({lifecycleState: 'CERTIFIED'});
            expect(createComponent({component}).comp.showDeleteVersionButton()).toBe(false);
        });
        it('hides on an archived component', () => {
            const component = makeComponent({isArchived: true});
            expect(createComponent({component}).comp.showDeleteVersionButton()).toBe(false);
        });
    });

    describe('showArchiveButton', () => {
        it('shows for a designer on a certified component', () => {
            const component = makeComponent({lifecycleState: 'CERTIFIED'});
            expect(createComponent({component}).comp.showArchiveButton()).toBe(true);
        });
        it('hides while checked out — the checkout must be resolved first', () => {
            expect(createComponent().comp.showArchiveButton()).toBe(false);
        });
        it('hides for a non-designer', () => {
            const component = makeComponent({lifecycleState: 'CERTIFIED'});
            expect(createComponent({component, role: Role.OPS}).comp.showArchiveButton()).toBe(false);
        });
        it('hides once already archived', () => {
            const component = makeComponent({lifecycleState: 'CERTIFIED', isArchived: true});
            expect(createComponent({component}).comp.showArchiveButton()).toBe(false);
        });
        it('hides in create mode', () => {
            const component = makeComponent({lifecycleState: 'CERTIFIED'});
            expect(createComponent({component, mode: WorkspaceMode.CREATE}).comp.showArchiveButton()).toBe(false);
        });
    });

    describe('showRestoreButton / showDeleteArchivedButton', () => {
        it('both show once archived', () => {
            const {comp} = createComponent({component: makeComponent({isArchived: true})});
            expect(comp.showRestoreButton()).toBe(true);
            expect(comp.showDeleteArchivedButton()).toBe(true);
        });
        it('both hide while not archived', () => {
            const {comp} = createComponent();
            expect(comp.showRestoreButton()).toBe(false);
            expect(comp.showDeleteArchivedButton()).toBe(false);
        });
        // Deliberately role-independent, as in the AngularJS original: a non-designer sees Restore
        // but the template greys it out via [class.disabled].
        it('show for a non-designer too', () => {
            const {comp} = createComponent({component: makeComponent({isArchived: true}), role: Role.OPS});
            expect(comp.showRestoreButton()).toBe(true);
            expect(comp.showDeleteArchivedButton()).toBe(true);
        });
        it('both hide in create mode', () => {
            const {comp} = createComponent({component: makeComponent({isArchived: true}), mode: WorkspaceMode.CREATE});
            expect(comp.showRestoreButton()).toBe(false);
            expect(comp.showDeleteArchivedButton()).toBe(false);
        });
    });

    describe('showUpgradeServicesButton', () => {
        it('shows for a certified service', () => {
            const component = makeComponent({lifecycleState: 'CERTIFIED'});
            expect(createComponent({component}).comp.showUpgradeServicesButton()).toBe(true);
        });
        it('shows for a certified VF', () => {
            const component = makeComponent({
                lifecycleState: 'CERTIFIED', isService: () => false, isResource: () => true, getComponentSubType: () => 'VF'
            });
            expect(createComponent({component}).comp.showUpgradeServicesButton()).toBe(true);
        });
        it('hides for a certified VFC — only services and VFs can be upgraded', () => {
            const component = makeComponent({
                lifecycleState: 'CERTIFIED', isService: () => false, isResource: () => true, getComponentSubType: () => 'VFC'
            });
            expect(createComponent({component}).comp.showUpgradeServicesButton()).toBe(false);
        });
        it('hides while not certified', () => {
            expect(createComponent().comp.showUpgradeServicesButton()).toBe(false);
        });
        it('hides for a non-designer', () => {
            const component = makeComponent({lifecycleState: 'CERTIFIED'});
            expect(createComponent({component, role: Role.GOVERNOR}).comp.showUpgradeServicesButton()).toBe(false);
        });
    });
});

describe('archiveComponent', () => {
    it('archives, drops the archive cache and notifies', () => {
        const componentServiceNg2: any = {
            getDependencies: jest.fn(), restoreComponent: jest.fn(), deleteComponent: jest.fn(),
            archiveComponent: jest.fn(() => Observable.of({}))
        };
        const {comp, cacheService, notificationsService} = createComponent({componentServiceNg2});
        comp.archiveComponent();
        expect(componentServiceNg2.archiveComponent).toHaveBeenCalledWith('SERVICE', 'comp-1');
        expect(comp.component.isArchived).toBe(true);
        expect(cacheService.remove).toHaveBeenCalledWith('archiveComponents');
        expect(notificationsService.push).toHaveBeenCalled();
        expect(comp.isLoading).toBe(false);
    });

    it('clears the loader and leaves the state alone when the request fails', () => {
        const componentServiceNg2: any = {
            getDependencies: jest.fn(), restoreComponent: jest.fn(), deleteComponent: jest.fn(),
            archiveComponent: jest.fn(() => Observable.throw('boom'))
        };
        const {comp, notificationsService} = createComponent({componentServiceNg2});
        comp.archiveComponent();
        expect(comp.isLoading).toBe(false);
        expect(comp.component.isArchived).toBe(false);
        expect(notificationsService.push).not.toHaveBeenCalled();
    });

    it('returns to the catalog it was opened from', () => {
        const componentServiceNg2: any = {
            getDependencies: jest.fn(), restoreComponent: jest.fn(), deleteComponent: jest.fn(),
            archiveComponent: jest.fn(() => Observable.of({}))
        };
        const navigationService: any = {
            getParam: jest.fn(() => 'catalog'),
            getParams: jest.fn(() => ({})),
            getCurrentStateName: jest.fn(() => 'workspace.general'),
            onNavigationSuccess: jest.fn(() => jest.fn()),
            navigate: jest.fn(),
            reload: jest.fn()
        };
        const {comp} = createComponent({componentServiceNg2, navigationService});
        comp.archiveComponent();
        expect(navigationService.navigate).toHaveBeenCalledWith('catalog');
    });

    // An archive from a workspace entered directly (no previousState) has nowhere to go back to;
    // the AngularJS original navigated nowhere rather than guessing.
    it('navigates nowhere without a previousState', () => {
        const componentServiceNg2: any = {
            getDependencies: jest.fn(), restoreComponent: jest.fn(), deleteComponent: jest.fn(),
            archiveComponent: jest.fn(() => Observable.of({}))
        };
        const {comp, navigationService} = createComponent({componentServiceNg2});
        comp.archiveComponent();
        expect(navigationService.navigate).not.toHaveBeenCalled();
    });
});

describe('restoreComponent', () => {
    it('restores, flips the flag, drops the cache and reloads', () => {
        const componentServiceNg2: any = {
            getDependencies: jest.fn(), archiveComponent: jest.fn(), deleteComponent: jest.fn(),
            restoreComponent: jest.fn(() => Observable.of({}))
        };
        const {comp, cacheService, notificationsService, navigationService} =
            createComponent({componentServiceNg2, component: makeComponent({isArchived: true})});
        comp.restoreComponent();
        expect(componentServiceNg2.restoreComponent).toHaveBeenCalledWith('SERVICE', 'comp-1');
        expect(comp.component.isArchived).toBe(false);
        expect(cacheService.remove).toHaveBeenCalledWith('archiveComponents');
        expect(notificationsService.push).toHaveBeenCalled();
        expect(navigationService.reload).toHaveBeenCalled();
    });

    it('clears the loader when the request fails', () => {
        const componentServiceNg2: any = {
            getDependencies: jest.fn(), archiveComponent: jest.fn(), deleteComponent: jest.fn(),
            restoreComponent: jest.fn(() => Observable.throw('boom'))
        };
        const {comp, navigationService} = createComponent({componentServiceNg2, component: makeComponent({isArchived: true})});
        comp.restoreComponent();
        expect(comp.isLoading).toBe(false);
        expect(navigationService.reload).not.toHaveBeenCalled();
    });
});

describe('deleteArchivedComponent', () => {
    it('confirms before deleting and wires OK to the delete itself', () => {
        const {comp, modalServiceSdcUI, componentServiceNg2} =
            createComponent({component: makeComponent({isArchived: true})});
        comp.deleteArchivedComponent();
        expect(modalServiceSdcUI.openWarningModal).toHaveBeenCalled();
        // Nothing may be deleted until the user confirms.
        expect(componentServiceNg2.deleteComponent).not.toHaveBeenCalled();
        const buttons = modalServiceSdcUI.openWarningModal.mock.calls[0][3];
        expect(buttons).toHaveLength(1);
        expect(buttons[0].callback).toBe(comp.handleDeleteArchivedComponent);
        expect(buttons[0].closeModal).toBe(true);
    });

    it('deletes, drops the cache, notifies success and leaves for the dashboard', () => {
        const componentServiceNg2: any = {
            getDependencies: jest.fn(), archiveComponent: jest.fn(), restoreComponent: jest.fn(),
            deleteComponent: jest.fn(() => Observable.of({}))
        };
        const navigationService: any = {
            getParam: jest.fn(() => 'workspace.general'),
            getParams: jest.fn(() => ({})),
            getCurrentStateName: jest.fn(() => 'workspace.general'),
            onNavigationSuccess: jest.fn(() => jest.fn()),
            navigate: jest.fn(),
            reload: jest.fn()
        };
        const {comp, cacheService, notificationsService} =
            createComponent({componentServiceNg2, navigationService, component: makeComponent({isArchived: true})});
        comp.handleDeleteArchivedComponent();
        expect(componentServiceNg2.deleteComponent).toHaveBeenCalledWith('SERVICE', 'comp-1');
        expect(cacheService.remove).toHaveBeenCalledWith('archiveComponents');
        expect(notificationsService.push).toHaveBeenCalled();
        // The deleted component's workspace is gone, so an unrecognised previousState falls back.
        expect(navigationService.navigate).toHaveBeenCalledWith('dashboard');
        expect(comp.isLoading).toBe(false);
    });

    it('notifies an error and stays put when the delete fails', () => {
        const componentServiceNg2: any = {
            getDependencies: jest.fn(), archiveComponent: jest.fn(), restoreComponent: jest.fn(),
            deleteComponent: jest.fn(() => Observable.throw('boom'))
        };
        const {comp, notificationsService, navigationService} =
            createComponent({componentServiceNg2, component: makeComponent({isArchived: true})});
        comp.handleDeleteArchivedComponent();
        expect(notificationsService.push).toHaveBeenCalled();
        expect(notificationsService.push.mock.calls[0][0].type).toBe('error');
        expect(navigationService.navigate).not.toHaveBeenCalled();
        expect(comp.isLoading).toBe(false);
    });
});

describe('openAutomatedUpgradeModal', () => {
    it('fetches the dependencies first and opens the modal with them', () => {
        const dependencies = [{version: '1.0', dependencies: [{name: 'svc'}]}];
        const componentServiceNg2: any = {
            archiveComponent: jest.fn(), restoreComponent: jest.fn(), deleteComponent: jest.fn(),
            getDependencies: jest.fn(() => Observable.of(dependencies))
        };
        const component = makeComponent({lifecycleState: 'CERTIFIED'});
        const {comp, automatedUpgradeService} = createComponent({componentServiceNg2, component});
        comp.openAutomatedUpgradeModal();
        expect(componentServiceNg2.getDependencies).toHaveBeenCalledWith('SERVICE', 'comp-1');
        // The third argument is load-bearing: `true` means "opened from the onboarding update flow"
        // and changes the modal's copy and its post-upgrade navigation.
        expect(automatedUpgradeService.openAutomatedUpgradeModal).toHaveBeenCalledWith(dependencies, component, false);
        expect(comp.isLoading).toBe(false);
    });
});

describe('hasNoDependencies', () => {
    // The Upgrade button is DISABLED when nothing depends on the component, so this flag defaulting
    // the wrong way would grey out the only affordance for the whole feature.
    it('starts true so the button is disabled until the dependency probe answers', () => {
        expect(createComponent().comp.hasNoDependencies).toBe(true);
    });

    it('goes false once a dependent version comes back', () => {
        const componentServiceNg2: any = {
            archiveComponent: jest.fn(), restoreComponent: jest.fn(), deleteComponent: jest.fn(),
            getDependencies: jest.fn(() => Observable.of([{version: '1.0', dependencies: [{name: 'svc'}]}]))
        };
        const {comp} = createComponent({componentServiceNg2, component: makeComponent({lifecycleState: 'CERTIFIED'})});
        (comp as any).verifyIfDependenciesExist();
        expect(comp.hasNoDependencies).toBe(false);
    });
});

describe('lifecycleButtonEntries', () => {

    // A duplicate data-tests-id is unreachable for Selenium/Playwright, which take the FIRST match.
    it('omits deleteVersion so only the standalone sprite span carries that test id', () => {
        const {comp} = createComponent();
        comp.changeLifecycleStateButtons = {
            certify: {text: 'Certify'},
            checkIn: {text: 'Check in'},
            deleteVersion: {text: 'Delete Version'}
        };
        (comp as any).updateLifecycleButtonEntries();
        expect(comp.lifecycleButtonEntries.map((e: any) => e.key)).toEqual(['certify', 'checkIn']);
    });

    it('leaves the first-button styling on Certify', () => {
        const {comp} = createComponent();
        comp.changeLifecycleStateButtons = {certify: {text: 'Certify'}, deleteVersion: {text: 'Delete Version'}};
        (comp as any).updateLifecycleButtonEntries();
        expect(comp.lifecycleButtonEntries[0].key).toBe('certify');
    });
});

describe('top-bar template', () => {

    // Asserting on the template STRING, not a rendered fixture: compiling this shell means
    // <router-outlet> and <top-nav>. It proves the markup exists and is wired to the right handler;
    // the Playwright suite (workspace-topbar-controls.spec.ts) proves it renders and is clickable.
    [
        ['open-upgrade-vsp-popup', 'openAutomatedUpgradeModal()'],
        ['restore-component-button', 'restoreComponent()'],
        ['delete_version', `changeLifecycleState('deleteVersion')`],
        ['delete_archive_version', 'deleteArchivedComponent()'],
        ['archive-component-button', 'archiveComponent()']
    ].forEach(([testId, handler]) => {
        it(`renders ${testId} wired to ${handler}`, () => {
            expect(template).toContain(`data-tests-id="${testId}"`);
            expect(template).toContain(handler);
        });
    });

    // The lifecycle loop must not render its own deleteVersion entry — the standalone
    // delete_version span above is what provides the affordance, as in the AngularJS template.
    // `[hidden]` would satisfy that visually while still emitting a second element with the same
    // data-tests-id, which is what actually broke the control (see updateLifecycleButtonEntries).
    it('does not hide the loop entry — it must be filtered out of the array instead', () => {
        expect(template).not.toContain('[hidden]');
    });

    it('labels the upgrade button per component kind', () => {
        expect(template).toContain(`component.isResource() ? 'Upgrade Services' : 'Update Services'`);
    });
});
