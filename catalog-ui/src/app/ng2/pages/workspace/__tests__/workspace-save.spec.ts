import {WorkspaceViewModel} from 'app/view-models/workspace/workspace-view-model';
import {ComponentState, ComponentType, EVENTS, States, WorkspaceMode} from 'app/utils/constants';
import {createMockComponent} from '../../../../../jest/mocks/workspace-component.mock';
import {createWorkspaceVMDeps, depsToMap} from '../controller-test-helpers';

jest.mock('app/models/plugins-config', () => ({
    PluginsConfiguration: {plugins: []}
}));

function createVM(options: any = {}) {
    const deps = createWorkspaceVMDeps(options);
    const map = depsToMap(deps);
    const vm = new WorkspaceViewModel(
        deps[0], deps[1], deps[2], deps[3], deps[4], deps[5], deps[6],
        deps[7], deps[8], deps[9], deps[10], deps[11], deps[12], deps[13],
        deps[14], deps[15], deps[16], deps[17], deps[18], deps[19], deps[20],
        deps[21], deps[22], deps[23]
    );
    return {vm, scope: map.scope, mocks: map};
}

function createEditModeVM() {
    const component = createMockComponent({
        lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
        lastUpdaterUserId: 'cs0008',
        componentType: ComponentType.RESOURCE,
        getAllVersionsAsSortedArray: jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]),
        getComponentSubType: jest.fn(() => 'VF'),
        getStatus: jest.fn(() => 'IN DESIGN'),
        isLatestVersion: jest.fn(() => true),
        isCsarComponent: jest.fn(() => false),
        isArchived: false,
        modules: [{name: 'module1'}],
        tags: ['TestComponent', 'tag1'],
        updateComponent: jest.fn(function() { return Promise.resolve(this); }),
        getComponent: jest.fn(function() { return Promise.resolve(this); }),
        componentMetadata: {componentType: ComponentType.RESOURCE}
    });
    return createVM({component});
}

describe('WorkspaceViewModel - State Change Interception', () => {
    it('does nothing in view mode', () => {
        const {scope, mocks} = createVM();
        const handler = mocks.scope.$on.mock.calls.find((c: any[]) => c[0] === '$stateChangeStart');
        expect(handler).toBeDefined();

        scope.mode = WorkspaceMode.VIEW;
        const event = {preventDefault: jest.fn()};
        handler[1](event, {name: 'workspace.composition'}, {id: 'unique-id-1'}, {name: 'workspace.general'}, {id: 'unique-id-1'});
        expect(event.preventDefault).not.toHaveBeenCalled();
    });

    it('in edit mode with unsaved changes, prevents navigation and saves', async () => {
        const {scope, mocks} = createVM();
        scope.mode = WorkspaceMode.EDIT;
        scope.isValidForm = true;
        mocks.$state.current.data.unsavedChanges = true;

        scope.save = jest.fn(() => Promise.resolve());
        scope.onMenuItemPressed = jest.fn();

        const handler = mocks.scope.$on.mock.calls.find((c: any[]) => c[0] === '$stateChangeStart');
        const event = {preventDefault: jest.fn()};
        handler[1](event, {name: 'workspace.composition'}, {id: 'unique-id-1'}, {name: 'workspace.general'}, {id: 'unique-id-1'});

        expect(event.preventDefault).toHaveBeenCalled();
        await Promise.resolve();
        expect(scope.save).toHaveBeenCalled();
    });

    it('in edit mode with invalid form, prevents navigation without saving', () => {
        const {scope, mocks} = createVM();
        scope.mode = WorkspaceMode.EDIT;
        scope.isValidForm = false;
        mocks.$state.current.data.unsavedChanges = true;

        scope.save = jest.fn(() => Promise.resolve());

        const handler = mocks.scope.$on.mock.calls.find((c: any[]) => c[0] === '$stateChangeStart');
        const event = {preventDefault: jest.fn()};
        handler[1](event, {name: 'workspace.composition'}, {id: 'unique-id-1'}, {name: 'workspace.general'}, {id: 'unique-id-1'});

        expect(event.preventDefault).toHaveBeenCalled();
        expect(scope.save).not.toHaveBeenCalled();
    });

    it('skips interception when navigating to different component', () => {
        const {scope, mocks} = createVM();
        scope.mode = WorkspaceMode.EDIT;
        mocks.$state.current.data.unsavedChanges = true;

        scope.save = jest.fn(() => Promise.resolve());

        const handler = mocks.scope.$on.mock.calls.find((c: any[]) => c[0] === '$stateChangeStart');
        const event = {preventDefault: jest.fn()};
        handler[1](event, {name: 'workspace.general'}, {id: 'different-id'}, {name: 'workspace.general'}, {id: 'unique-id-1'});

        expect(event.preventDefault).not.toHaveBeenCalled();
    });
});

describe('WorkspaceViewModel - save()', () => {
    it('notifies ON_WORKSPACE_SAVE_BUTTON_CLICK event', () => {
        const {scope, mocks} = createEditModeVM();
        scope.save();
        expect(mocks.eventListenerService.notifyObservers).toHaveBeenCalledWith(EVENTS.ON_WORKSPACE_SAVE_BUTTON_CLICK);
    });

    it('calls startProgress and disableMenuItems', () => {
        const {scope} = createEditModeVM();
        scope.save();
        expect(scope.isCreateProgress).toBe(true);
        expect(scope.progressMessage).toBe('Updating Asset...');
    });

    it('on success: clears unsaved flags', async () => {
        const {scope, mocks} = createEditModeVM();
        mocks.$state.current.data = {unsavedChanges: true};
        scope.unsavedFile = true;

        await scope.save();

        expect(mocks.$state.current.data.unsavedChanges).toBe(false);
        expect(scope.unsavedFile).toBe(false);
    });

    it('on success: stops progress and enables UI', async () => {
        const {scope} = createEditModeVM();
        await scope.save();
        expect(scope.isCreateProgress).toBe(false);
        expect(scope.disabledButtons).toBe(false);
    });

    it('on success: removes CHANGE_COMPONENT_CSAR_VERSION_FLAG from cache if present', async () => {
        const {scope, mocks} = createEditModeVM();
        mocks.cacheService.contains.mockReturnValue(true);
        await scope.save();
        expect(mocks.cacheService.remove).toHaveBeenCalled();
    });

    it('on failure: notifies error event and rejects', async () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
            componentType: ComponentType.RESOURCE,
            getAllVersionsAsSortedArray: jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]),
            getComponentSubType: jest.fn(() => 'VF'),
            getStatus: jest.fn(() => 'IN DESIGN'),
            isLatestVersion: jest.fn(() => true),
            isCsarComponent: jest.fn(() => false),
            isArchived: false,
            modules: [{name: 'module1'}],
            updateComponent: jest.fn(() => Promise.reject('error')),
            componentMetadata: {componentType: ComponentType.RESOURCE}
        });
        const {scope, mocks} = createVM({component});

        await expect(scope.save()).rejects.toBeUndefined();
        expect(mocks.eventListenerService.notifyObservers).toHaveBeenCalledWith(EVENTS.ON_WORKSPACE_SAVE_BUTTON_ERROR);
    });
});

describe('WorkspaceViewModel - create()', () => {
    it('calls startProgress with Creating Asset message', () => {
        const {scope} = createVM({stateParamsOverrides: {id: undefined}, stateOverrides: {params: {id: undefined}}});
        scope.component.updateComponent = jest.fn(() => ({then: jest.fn()}));
        scope.create();
        expect(scope.isCreateProgress).toBe(true);
        expect(scope.progressMessage).toBe('Creating Asset...');
    });

    it('disables general tab during creation', () => {
        const {scope} = createVM({stateParamsOverrides: {id: undefined}, stateOverrides: {params: {id: undefined}}});
        scope.component.updateComponent = jest.fn(() => ({then: jest.fn()}));
        scope.create();
        expect(scope.leftBarTabs.menuItems[0].isDisabled).toBe(true);
    });
});
