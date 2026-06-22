import {WorkspaceViewModel} from 'app/view-models/workspace/workspace-view-model';
import {ComponentState, ComponentType, EVENTS, ResourceType, WorkspaceMode} from 'app/utils/constants';
import {createMockComponent, createServiceComponent} from '../../../../../jest/mocks/workspace-component.mock';
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
        updateComponent: jest.fn(function() { return Promise.resolve(this); }),
        getComponent: jest.fn(function() { return Promise.resolve(this); }),
        componentMetadata: {componentType: ComponentType.RESOURCE}
    });
    return createVM({component});
}

describe('WorkspaceViewModel - changeLifecycleState', () => {
    it('in general view (not deleteVersion): notifies ON_LIFECYCLE_CHANGE_WITH_SAVE', () => {
        const {scope, mocks} = createEditModeVM();
        scope.changeLifecycleState('checkin');
        expect(mocks.eventListenerService.notifyObservers).toHaveBeenCalledWith(EVENTS.ON_LIFECYCLE_CHANGE_WITH_SAVE, 'checkin');
    });

    it('for deleteVersion: calls handleChangeLifecycleState directly', () => {
        const {scope, mocks} = createEditModeVM();
        scope.handleChangeLifecycleState = jest.fn();
        scope.changeLifecycleState('deleteVersion');
        expect(scope.handleChangeLifecycleState).toHaveBeenCalledWith('deleteVersion');
    });

    it('outside general view: calls handleChangeLifecycleState directly', () => {
        const {scope, mocks} = createEditModeVM();
        mocks.$state.current.name = 'workspace.composition';
        scope.handleChangeLifecycleState = jest.fn();
        scope.changeLifecycleState('checkin');
        expect(scope.handleChangeLifecycleState).toHaveBeenCalledWith('checkin');
    });
});

describe('WorkspaceViewModel - handleChangeLifecycleState', () => {
    it('monitor state navigates to workspace.distribution', () => {
        const {scope, mocks} = createEditModeVM();
        scope.handleChangeLifecycleState('monitor');
        expect(mocks.$state.go).toHaveBeenCalledWith('workspace.distribution');
    });

    it('calls ChangeLifecycleStateHandler with correct data', () => {
        const {scope, mocks} = createEditModeVM();
        scope.changeLifecycleStateButtons = {
            checkin: {text: 'Check in', url: 'lifecycleState/CHECKIN'}
        };
        scope.handleChangeLifecycleState('checkin');
        expect(mocks.changeLifecycleStateHandler.changeLifecycleState).toHaveBeenCalled();
        const args = mocks.changeLifecycleStateHandler.changeLifecycleState.mock.calls[0];
        expect(args[1]).toEqual({text: 'Check in', url: 'lifecycleState/CHECKIN'});
    });

    it('CHECKOUT success: updates bridge service isViewOnly to false', () => {
        const {scope, mocks} = createEditModeVM();
        scope.changeLifecycleStateButtons = {
            checkout: {text: 'Check Out', url: 'lifecycleState/CHECKOUT'}
        };
        scope.handleChangeLifecycleState('checkout');
        expect(mocks.workspaceNg1BridgeService.updateIsViewOnly).toHaveBeenCalledWith(false);
    });

    it('CHECKOUT success: notifies CHECK_OUT event bus', () => {
        const {scope, mocks} = createEditModeVM();
        scope.changeLifecycleStateButtons = {
            checkout: {text: 'Check Out', url: 'lifecycleState/CHECKOUT'}
        };
        scope.handleChangeLifecycleState('checkout');
        expect(mocks.eventBusService.notify).toHaveBeenCalledWith('CHECK_OUT', expect.any(Object), false);
    });

    it('CHECKOUT success: shows success notification', () => {
        const {scope, mocks} = createEditModeVM();
        scope.changeLifecycleStateButtons = {
            checkout: {text: 'Check Out', url: 'lifecycleState/CHECKOUT'}
        };
        scope.handleChangeLifecycleState('checkout');
        expect(mocks.notification.success).toHaveBeenCalled();
    });

    it('CHECKIN success: navigates to dashboard', () => {
        const {scope, mocks} = createEditModeVM();
        scope.changeLifecycleStateButtons = {
            checkin: {text: 'Check in', url: 'lifecycleState/CHECKIN'}
        };
        scope.handleChangeLifecycleState('checkin');
        expect(mocks.$state.go).toHaveBeenCalledWith('dashboard');
    });

    it('CHECKIN success: updates bridge service isViewOnly to true', () => {
        const {scope, mocks} = createEditModeVM();
        scope.changeLifecycleStateButtons = {
            checkin: {text: 'Check in', url: 'lifecycleState/CHECKIN'}
        };
        scope.handleChangeLifecycleState('checkin');
        expect(mocks.workspaceNg1BridgeService.updateIsViewOnly).toHaveBeenCalledWith(true);
    });

    it('UNDOCHECKOUT success: notifies UNDO_CHECK_OUT event bus then navigates to dashboard', () => {
        const {scope, mocks} = createEditModeVM();
        scope.changeLifecycleStateButtons = {
            undoCheckout: {text: 'Undo Checkout', url: 'lifecycleState/UNDOCHECKOUT'}
        };
        scope.handleChangeLifecycleState('undoCheckout');
        expect(mocks.eventBusService.notify).toHaveBeenCalledWith('UNDO_CHECK_OUT', expect.any(Object), false);
        expect(mocks.$state.go).toHaveBeenCalledWith('dashboard');
    });

    it('CERTIFY success: calls handleCertification', () => {
        const {scope, mocks} = createEditModeVM();
        scope.changeLifecycleStateButtons = {
            certify: {text: 'Certify', url: 'lifecycleState/certify'}
        };
        scope.handleCertification = jest.fn();
        scope.handleChangeLifecycleState('certify');
        expect(scope.handleCertification).toHaveBeenCalled();
    });

    it('DISTRIBUTE success: shows success notification and re-inits buttons', () => {
        const component = createServiceComponent({
            lifecycleState: ComponentState.CERTIFIED,
            distributionStatus: 'DISTRIBUTION_NOT_APPROVED',
            getAllVersionsAsSortedArray: jest.fn(() => [{versionNumber: '1.0', versionId: 'unique-id-1'}]),
            getComponentSubType: jest.fn(() => 'SERVICE'),
            getStatus: jest.fn(() => 'CERTIFIED'),
            isLatestVersion: jest.fn(() => true),
            isCsarComponent: jest.fn(() => false),
            modules: [],
            componentMetadata: {componentType: ComponentType.SERVICE}
        });
        const {scope, mocks} = createVM({component});
        scope.changeLifecycleStateButtons = {
            distribute: {text: 'Distribute', url: 'distribution/PROD/activate'}
        };
        scope.handleChangeLifecycleState('distribute');
        expect(mocks.notification.success).toHaveBeenCalled();
    });
});

describe('WorkspaceViewModel - handleCertification', () => {
    it('for VF: calls getDependencies', () => {
        const {scope, mocks} = createEditModeVM();
        scope.handleCertification(scope.component);
        expect(mocks.componentServiceNg2.getDependencies).toHaveBeenCalled();
    });

    it('with no dependencies: shows certification success notification', () => {
        const {scope, mocks} = createEditModeVM();
        mocks.componentServiceNg2.getDependencies.mockReturnValue({
            subscribe: jest.fn((cb: Function) => cb([]))
        });
        scope.handleCertification(scope.component);
        expect(mocks.notification.success).toHaveBeenCalled();
    });

    it('with dependencies: opens automated upgrade modal', () => {
        const {scope, mocks} = createEditModeVM();
        mocks.componentServiceNg2.getDependencies.mockReturnValue({
            subscribe: jest.fn((cb: Function) => cb([{dependencies: ['dep1']}]))
        });
        scope.handleCertification(scope.component);
        expect(mocks.automatedUpgradeService.openAutomatedUpgradeModal).toHaveBeenCalled();
    });

    it('for non-VF resource: calls onSuccessWithoutUpgradeNeeded directly', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
            componentType: ComponentType.RESOURCE,
            resourceType: 'VFC',
            getAllVersionsAsSortedArray: jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]),
            getComponentSubType: jest.fn(() => 'VFC'),
            getStatus: jest.fn(() => 'IN DESIGN'),
            isLatestVersion: jest.fn(() => true),
            isCsarComponent: jest.fn(() => false),
            isArchived: false,
            modules: [{name: 'module1'}],
            componentMetadata: {componentType: ComponentType.RESOURCE}
        });
        const {scope, mocks} = createVM({component});
        scope.handleCertification(scope.component);
        expect(mocks.componentServiceNg2.getDependencies).not.toHaveBeenCalled();
        expect(mocks.notification.success).toHaveBeenCalled();
    });
});

describe('WorkspaceViewModel - Archive/Restore/Delete', () => {
    it('archiveComponent calls archive API', () => {
        const {scope, mocks} = createEditModeVM();
        scope.archiveComponent();
        expect(mocks.componentServiceNg2.archiveComponent).toHaveBeenCalledWith(ComponentType.RESOURCE, 'unique-id-1');
    });

    it('archiveComponent navigates if previousState is dashboard', () => {
        const {scope, mocks} = createEditModeVM();
        mocks.$state.params.previousState = 'dashboard';
        scope.archiveComponent();
        expect(mocks.$state.go).toHaveBeenCalledWith('dashboard');
    });

    it('restoreComponent calls restore API and shows success notification', () => {
        const {scope, mocks} = createEditModeVM();
        scope.restoreComponent();
        expect(mocks.componentServiceNg2.restoreComponent).toHaveBeenCalledWith(ComponentType.RESOURCE, 'unique-id-1');
        expect(mocks.notification.success).toHaveBeenCalled();
    });

    it('deleteArchivedComponent opens warning modal', () => {
        const {scope, mocks} = createEditModeVM();
        scope.deleteArchivedComponent();
        expect(mocks.modalServiceSdcUI.openWarningModal).toHaveBeenCalled();
    });
});
