import {ComponentState, ComponentType, EVENTS, Role, States, WorkspaceMode} from 'app/utils/constants';
import {createMockComponent, createServiceComponent} from '../../../../jest/mocks/workspace-component.mock';
import {createMockUser} from '../../../../jest/mocks/user-data.mock';
import {createMockState, createMockStateParams, createMockQ, createMockFilter} from '../../../../jest/mocks/state-service.mock';
import {createMockSdcMenu} from '../../../../jest/mocks/workspace-menu.mock';

export function createMockScope(overrides: any = {}): any {
    return {
        $on: jest.fn(),
        $watch: jest.fn(),
        $watchCollection: jest.fn(),
        $broadcast: jest.fn(),
        $emit: jest.fn(),
        $apply: jest.fn((fn?: Function) => fn && fn()),
        $digest: jest.fn(),
        ...overrides
    };
}

export interface WorkspaceVMContext {
    scope: any;
    mocks: {[key: string]: any};
}

export function createWorkspaceVMDeps(options: {
    component?: any;
    user?: any;
    stateOverrides?: any;
    stateParamsOverrides?: any;
    noBreadcrumbsCache?: boolean;
} = {}): any[] {
    const user = options.user || createMockUser();
    const component = options.component || createMockComponent({
        getAllVersionsAsSortedArray: jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]),
        getComponentSubType: jest.fn(() => 'VF'),
        getStatus: jest.fn(() => 'IN DESIGN'),
        isLatestVersion: jest.fn(() => true),
        isCsarComponent: jest.fn(() => false),
        isArchived: false,
        modules: [{name: 'module1'}],
        updateComponent: jest.fn(() => Promise.resolve(component)),
        getComponent: jest.fn(() => Promise.resolve(component)),
        componentMetadata: {componentType: ComponentType.RESOURCE}
    });

    const scope = createMockScope();
    const $state = createMockState(options.stateOverrides);
    const $stateParams = createMockStateParams(options.stateParamsOverrides);
    const sdcMenu = createMockSdcMenu();
    const $q = createMockQ();
    const $filter = createMockFilter();

    const cacheService = {
        get: jest.fn((key: string) => {
            if (key === 'user') return user;
            if (key === 'version') return '1.0';
            if (key === 'breadcrumbsComponents') return options.noBreadcrumbsCache ? undefined : [component];
            return undefined;
        }),
        set: jest.fn(),
        remove: jest.fn(),
        contains: jest.fn(() => false)
    };

    const componentFactory = {
        createComponent: jest.fn((c) => ({...c})),
        createComponentOnServer: jest.fn(() => ({then: jest.fn()})),
        importComponentOnServer: jest.fn(() => ({then: jest.fn()}))
    };

    const menuHandler = {
        findBreadcrumbComponentIndex: jest.fn(() => 0),
        generateBreadcrumbsModelFromComponents: jest.fn(() => ({menuItems: [], selectedIndex: 0}))
    };

    const changeLifecycleStateHandler = {
        changeLifecycleState: jest.fn((comp, data, scope, onSuccess) => {
            onSuccess(comp, data.url);
        })
    };

    const leftPaletteLoaderService = {};

    const eventListenerService = {
        registerObserverCallback: jest.fn(),
        unRegisterObserver: jest.fn(),
        notifyObservers: jest.fn()
    };

    const notification = {success: jest.fn(), info: jest.fn(), error: jest.fn()};

    const homeService = {getAllComponents: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb([component]))}))};
    const catalogService = {getCatalog: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb([component]))}))};
    const progressService = {initCreateComponentProgress: jest.fn(), deleteProgressValue: jest.fn()};

    const componentServiceNg2 = {
        archiveComponent: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb())})),
        restoreComponent: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb())})),
        deleteComponent: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb())})),
        getDependencies: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb([]))}))
    };

    const automatedUpgradeService = {openAutomatedUpgradeModal: jest.fn()};

    const eventBusService = {
        notify: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb())}))
    };

    const modalServiceSdcUI = {openWarningModal: jest.fn(), openInfoModal: jest.fn()};
    const pluginsService = {isPluginDisplayedInContext: jest.fn(() => false)};
    const workspaceNg1BridgeService = {updateIsViewOnly: jest.fn()};
    const workspaceService = {setComponentMetadata: jest.fn()};

    return [
        scope, component, componentFactory, $state, sdcMenu, $q, menuHandler,
        cacheService, changeLifecycleStateHandler, leftPaletteLoaderService, $filter,
        eventListenerService, notification, $stateParams, homeService, catalogService,
        progressService, componentServiceNg2, automatedUpgradeService, eventBusService,
        modalServiceSdcUI, pluginsService, workspaceNg1BridgeService, workspaceService
    ];
}

export function depsToMap(deps: any[]): {[key: string]: any} {
    const names = [
        'scope', 'component', 'componentFactory', '$state', 'sdcMenu', '$q', 'menuHandler',
        'cacheService', 'changeLifecycleStateHandler', 'leftPaletteLoaderService', '$filter',
        'eventListenerService', 'notification', '$stateParams', 'homeService', 'catalogService',
        'progressService', 'componentServiceNg2', 'automatedUpgradeService', 'eventBusService',
        'modalServiceSdcUI', 'pluginsService', 'workspaceNg1BridgeService', 'workspaceService'
    ];
    const map: any = {};
    names.forEach((name, i) => map[name] = deps[i]);
    return map;
}
