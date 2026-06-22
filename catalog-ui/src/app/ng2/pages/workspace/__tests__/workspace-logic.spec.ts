import {WorkspaceViewModel} from 'app/view-models/workspace/workspace-view-model';
import {ComponentState, ComponentType, Role, States, WorkspaceMode} from 'app/utils/constants';
import {createMockComponent, createCertifiedComponent, createServiceComponent} from '../../../../../jest/mocks/workspace-component.mock';
import {createMockUser, mockTester} from '../../../../../jest/mocks/user-data.mock';
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

describe('WorkspaceViewModel - Mode Determination', () => {
    it('should set CREATE mode when no id in state params', () => {
        const {scope} = createVM({stateParamsOverrides: {id: undefined}, stateOverrides: {params: {id: undefined}}});
        expect(scope.mode).toBe(WorkspaceMode.CREATE);
    });

    it('should set EDIT mode for resource checked out by current DESIGNER user', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
            componentType: ComponentType.RESOURCE
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'IN DESIGN');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [{name: 'module1'}];

        const {scope} = createVM({component});
        expect(scope.mode).toBe(WorkspaceMode.EDIT);
    });

    it('should set EDIT mode for service checked out by current DESIGNER user', () => {
        const component = createServiceComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008'
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'SERVICE');
        component.getStatus = jest.fn(() => 'IN DESIGN');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [];

        const {scope} = createVM({component});
        expect(scope.mode).toBe(WorkspaceMode.EDIT);
    });

    it('should set VIEW mode when checked out by a different user', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'other-user',
            componentType: ComponentType.RESOURCE
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'IN DESIGN');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [];

        const {scope} = createVM({component});
        expect(scope.mode).toBe(WorkspaceMode.VIEW);
    });

    it('should set VIEW mode when user is TESTER role', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'tester01',
            componentType: ComponentType.RESOURCE
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'IN DESIGN');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [];

        const {scope} = createVM({component, user: mockTester});
        expect(scope.mode).toBe(WorkspaceMode.VIEW);
    });

    it('should set VIEW mode for certified component', () => {
        const component = createCertifiedComponent();
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '1.0', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'CERTIFIED');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [];

        const {scope} = createVM({component});
        expect(scope.mode).toBe(WorkspaceMode.VIEW);
    });

    it('should call workspaceNg1BridgeService.updateIsViewOnly(true) in VIEW mode', () => {
        const component = createCertifiedComponent();
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '1.0', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'CERTIFIED');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [];

        const {mocks} = createVM({component});
        expect(mocks.workspaceNg1BridgeService.updateIsViewOnly).toHaveBeenCalledWith(true);
    });

    it('should call workspaceNg1BridgeService.updateIsViewOnly(false) in EDIT mode', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
            componentType: ComponentType.RESOURCE
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'IN DESIGN');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [{name: 'module1'}];

        const {mocks} = createVM({component});
        expect(mocks.workspaceNg1BridgeService.updateIsViewOnly).toHaveBeenCalledWith(false);
    });
});

describe('WorkspaceViewModel - Mode Query Methods', () => {
    let scope: any;

    beforeEach(() => {
        const result = createVM();
        scope = result.scope;
    });

    it('isViewMode returns true in VIEW mode', () => {
        scope.mode = WorkspaceMode.VIEW;
        expect(scope.isViewMode()).toBe(true);
    });

    it('isViewMode returns false in EDIT mode', () => {
        scope.mode = WorkspaceMode.EDIT;
        expect(scope.isViewMode()).toBe(false);
    });

    it('isEditMode returns true in EDIT mode', () => {
        scope.mode = WorkspaceMode.EDIT;
        expect(scope.isEditMode()).toBe(true);
    });

    it('isCreateMode returns true in CREATE mode', () => {
        scope.mode = WorkspaceMode.CREATE;
        expect(scope.isCreateMode()).toBe(true);
    });

    it('isDesigner returns true for DESIGNER role', () => {
        expect(scope.isDesigner()).toBe(true);
    });

    it('isDesigner returns false for TESTER role', () => {
        const {scope: s} = createVM({user: mockTester});
        // Tester role means VIEW mode, but isDesigner checks role
        expect(s.isDesigner()).toBe(false);
    });

    it('isDisableMode returns true in VIEW + NOT_CERTIFIED_CHECKIN', () => {
        scope.mode = WorkspaceMode.VIEW;
        scope.component.lifecycleState = ComponentState.NOT_CERTIFIED_CHECKIN;
        expect(scope.isDisableMode()).toBe(true);
    });

    it('isDisableMode returns false in EDIT mode', () => {
        scope.mode = WorkspaceMode.EDIT;
        expect(scope.isDisableMode()).toBe(false);
    });

    it('isGeneralView returns true when state is workspace.general', () => {
        expect(scope.isGeneralView()).toBe(true);
    });

    it('isGeneralView returns false for other states', () => {
        scope.$state.current.name = States.WORKSPACE_COMPOSITION;
        expect(scope.isGeneralView()).toBe(false);
    });
});

describe('WorkspaceViewModel - Button Visibility', () => {
    it('checkDisableButton returns true in CREATE mode', () => {
        const {scope} = createVM({stateParamsOverrides: {id: undefined}, stateOverrides: {params: {id: undefined}}});
        const button = {url: 'lifecycleState/CHECKIN', disabled: false};
        expect(scope.checkDisableButton(button)).toBe(true);
    });

    it('checkDisableButton returns true when button.disabled is true', () => {
        const {scope} = createVM();
        const button = {url: 'lifecycleState/CHECKIN', disabled: true};
        expect(scope.checkDisableButton(button)).toBe(true);
    });

    it('checkDisableButton returns true when disabledButtons is true', () => {
        const {scope} = createVM();
        scope.disabledButtons = true;
        const button = {url: 'lifecycleState/CHECKIN', disabled: false};
        expect(scope.checkDisableButton(button)).toBe(true);
    });

    it('checkDisableButton returns true when form is invalid', () => {
        const {scope} = createVM();
        scope.isValidForm = false;
        const button = {url: 'lifecycleState/CHECKIN', disabled: false};
        expect(scope.checkDisableButton(button)).toBe(true);
    });

    it('checkDisableButton returns true when unsaved changes exist', () => {
        const {scope} = createVM();
        scope.unsavedChanges = true;
        const button = {url: 'lifecycleState/CHECKIN', disabled: false};
        expect(scope.checkDisableButton(button)).toBe(true);
    });

    it('checkDisableButton returns true when component is archived', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
            componentType: ComponentType.RESOURCE,
            isArchived: true
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'IN DESIGN');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [{name: 'module1'}];

        const {scope} = createVM({component});
        const button = {url: 'lifecycleState/CHECKIN', disabled: false};
        expect(scope.checkDisableButton(button)).toBe(true);
    });

    it('checkDisableButton returns true for CHECKOUT when not latest version', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
            componentType: ComponentType.RESOURCE,
            isArchived: false
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'IN DESIGN');
        component.isLatestVersion = jest.fn(() => false);
        component.modules = [{name: 'module1'}];

        const {scope} = createVM({component});
        const button = {url: 'lifecycleState/CHECKOUT', disabled: false};
        expect(scope.checkDisableButton(button)).toBe(true);
    });

    it('checkDisableButton returns false for CHECKOUT when latest version', () => {
        const {scope} = createVM();
        scope.unsavedChanges = false;
        scope.disabledButtons = false;
        scope.isValidForm = true;
        const button = {url: 'lifecycleState/CHECKOUT', disabled: false};
        expect(scope.checkDisableButton(button)).toBe(false);
    });
});

describe('WorkspaceViewModel - Lifecycle Button Init', () => {
    it('reads buttons from sdcMenu for resource lifecycle state', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
            componentType: ComponentType.RESOURCE
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'IN DESIGN');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [{name: 'module1'}];

        const {scope} = createVM({component});
        expect(scope.changeLifecycleStateButtons).toBeDefined();
        expect(scope.changeLifecycleStateButtons.checkin).toBeDefined();
    });

    it('uses distributionStatus for certified services', () => {
        const component = createServiceComponent({
            lifecycleState: ComponentState.CERTIFIED,
            distributionStatus: 'DISTRIBUTION_NOT_APPROVED'
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '1.0', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'SERVICE');
        component.getStatus = jest.fn(() => 'CERTIFIED');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [];

        const {scope} = createVM({component});
        expect(scope.changeLifecycleStateButtons).toBeDefined();
        expect(scope.changeLifecycleStateButtons.distribute).toBeDefined();
    });
});

describe('WorkspaceViewModel - Status and UI Helpers', () => {
    it('getStatus returns IN DESIGN in create mode', () => {
        const {scope} = createVM({stateParamsOverrides: {id: undefined}, stateOverrides: {params: {id: undefined}}});
        expect(scope.getStatus()).toBe('IN DESIGN');
    });

    it('getStatus delegates to component.getStatus in non-create mode', () => {
        const {scope} = createVM();
        scope.component.getStatus = jest.fn(() => 'CERTIFIED');
        expect(scope.getStatus()).toBe('CERTIFIED');
    });

    it('showLifecycleIcon returns true for DESIGNER', () => {
        const {scope} = createVM();
        expect(scope.showLifecycleIcon()).toBe(true);
    });

    it('showLifecycleIcon returns false for TESTER', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'tester01'
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'IN DESIGN');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [];

        const {scope} = createVM({component, user: mockTester});
        expect(scope.showLifecycleIcon()).toBe(false);
    });

    it('showLatestVersion returns false when not latest version', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008'
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'IN DESIGN');
        component.isLatestVersion = jest.fn(() => false);
        component.modules = [{name: 'module1'}];

        const {scope} = createVM({component});
        expect(scope.showLatestVersion()).toBe(false);
    });

    it('showLatestVersion returns false for NOT_CERTIFIED_CHECKOUT in VIEW mode', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'other-user'
        });
        component.getAllVersionsAsSortedArray = jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]);
        component.getComponentSubType = jest.fn(() => 'VF');
        component.getStatus = jest.fn(() => 'IN DESIGN');
        component.isLatestVersion = jest.fn(() => true);
        component.modules = [];

        const {scope} = createVM({component});
        expect(scope.mode).toBe(WorkspaceMode.VIEW);
        expect(scope.showLatestVersion()).toBe(false);
    });
});
