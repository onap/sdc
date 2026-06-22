import {GeneralViewModel} from 'app/view-models/workspace/tabs/general/general-view-model';
import {ComponentState, ComponentType, ResourceType, WorkspaceMode} from 'app/utils/constants';
import {createMockComponent} from '../../../../../jest/mocks/workspace-component.mock';
import {createMockUser} from '../../../../../jest/mocks/user-data.mock';
import {createMockState, createMockStateParams, createMockFilter} from '../../../../../jest/mocks/state-service.mock';
import {createMockSdcMenu} from '../../../../../jest/mocks/workspace-menu.mock';

(global as any).angular = {copy: jest.fn((obj) => JSON.parse(JSON.stringify(obj)))};

function createGeneralVM(options: any = {}) {
    const user = options.user || createMockUser();
    const component = options.component || createMockComponent({
        lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
        lastUpdaterUserId: 'cs0008',
        componentType: ComponentType.RESOURCE,
        resourceType: ResourceType.VF,
        getAllVersionsAsSortedArray: jest.fn(() => [{versionNumber: '0.1', versionId: 'unique-id-1'}]),
        getComponentSubType: jest.fn(() => 'VF'),
        getStatus: jest.fn(() => 'IN DESIGN'),
        isLatestVersion: jest.fn(() => true),
        isCsarComponent: jest.fn(() => false),
        isAlreadyCertified: jest.fn(() => false),
        isArchived: false,
        modules: [],
        tags: ['TestComponent'],
        selectedCategory: 'Generic',
        categories: [{name: 'Generic', subcategories: [{name: 'Abstract'}]}],
        validateName: jest.fn(() => Promise.resolve({isValid: true})),
        componentMetadata: {componentType: ComponentType.RESOURCE},
        vendorName: 'TestVendor',
        importedFile: undefined,
        csarUUID: undefined,
        vspArchived: false
    });

    const scope: any = {
        $on: jest.fn(),
        $watch: jest.fn(),
        $watchCollection: jest.fn(),
        $broadcast: jest.fn(),
        component,
        originComponent: {...component, name: 'OriginalName', selectedCategory: 'Generic'},
        componentType: component.componentType || ComponentType.RESOURCE,
        isCreateMode: jest.fn(() => options.isCreateMode || false),
        isEditMode: jest.fn(() => options.isEditMode !== undefined ? options.isEditMode : true),
        isViewMode: jest.fn(() => false),
        setValidState: jest.fn(),
        setComponent: jest.fn(),
        setOriginComponent: jest.fn(),
        updateBreadcrumbs: jest.fn(),
        updateUnsavedFileFlag: jest.fn(),
        save: jest.fn(() => Promise.resolve()),
        handleChangeLifecycleState: jest.fn(),
        reload: jest.fn(),
        updateMenuComponentName: jest.fn(),
        mode: options.isCreateMode ? WorkspaceMode.CREATE : WorkspaceMode.EDIT,
        sdcMenu: createMockSdcMenu(),
        breadcrumbsModel: [{menuItems: [], selectedIndex: 0, updateSelectedMenuItemText: jest.fn()}, {menuItems: [], selectedIndex: 0, updateSelectedMenuItemText: jest.fn()}],
        unsavedFile: false,
        user,
        editForm: {
            $setPristine: jest.fn(),
            $valid: true,
            $dirty: false,
            componentName: {$error: {}, $setValidity: jest.fn(), $setDirty: jest.fn()}
        },
        getMetadataKeyValidValues: jest.fn(() => []),
        ...options.scopeOverrides
    };

    const cacheService = {
        get: jest.fn((key: string) => {
            if (key === 'user') return user;
            if (key === 'resourceCategories') return [{name: 'Generic', subcategories: [{name: 'Abstract'}]}];
            if (key === 'serviceCategories') return [{name: 'Mobility', subcategories: [{name: 'Core'}]}];
            if (key === 'UIConfiguration') return {environmentContext: {defaultValue: 'General_Revenue-Bearing', validValues: ['General_Revenue-Bearing']}};
            return undefined;
        }),
        set: jest.fn(),
        remove: jest.fn(),
        contains: jest.fn(() => false)
    };

    const $state = createMockState();
    $state.current = {name: 'workspace.general', data: {unsavedChanges: false}};
    const $stateParams = createMockStateParams(options.stateParamsOverrides);
    const $filter = createMockFilter();

    const eventListenerService = {registerObserverCallback: jest.fn(), unRegisterObserver: jest.fn(), notifyObservers: jest.fn()};
    const progressService = {initCreateComponentProgress: jest.fn(), deleteProgressValue: jest.fn()};
    const modalsHandler = {openUpdateIconModal: jest.fn(() => Promise.resolve(true))};
    const modalServiceSdcUI = {openErrorDetailModal: jest.fn()};
    const notification = {success: jest.fn(), info: jest.fn(), error: jest.fn()};
    const componentFactory = {createComponent: jest.fn((c) => ({...c, name: 'OriginalName'})), updateComponentFromCsar: jest.fn()};
    const validationUtils = {};
    const fileUtils = {base64toBlob: jest.fn()};
    const onBoardingService = {getOnboardingVSPs: jest.fn(() => ({map: jest.fn(() => ({subscribe: jest.fn()}))}))};
    const importVSPService = {openOnboardingModal: jest.fn(() => ({subscribe: jest.fn()}))};
    const elementService = {getCategoryBaseTypes: jest.fn(() => ({subscribe: jest.fn()}))};
    const modelService = {
        getModels: jest.fn(() => ({subscribe: jest.fn()})),
        getModelsOfType: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb([]))}))
    };
    const sdcConfig = {csarFileExtension: ['csar', 'zip'], toscaFileExtension: ['yaml', 'yml']};

    const vm = new GeneralViewModel(
        scope, cacheService,
        /^[\s\S]*$/, /^[\s\S]*$/, /^[\s\S]*$/, /^[\s\S]*$/, /^[\s\S]*$/, /^[\s\S]*$/, /^[\s\S]*$/,
        validationUtils as any, fileUtils as any, sdcConfig as any, $state,
        modalsHandler as any, modalServiceSdcUI as any, eventListenerService as any,
        notification, progressService as any, jest.fn(), $filter, jest.fn(),
        onBoardingService as any, componentFactory as any, importVSPService as any,
        elementService as any, modelService as any, $stateParams
    );

    return {vm, scope, mocks: {cacheService, componentFactory, modalsHandler, eventListenerService}};
}

describe('GeneralViewModel - revert', () => {
    it('restores component from originComponent via ComponentFactory', () => {
        const {scope, mocks} = createGeneralVM();
        scope.component.name = 'Modified';
        scope.revert();
        expect(mocks.componentFactory.createComponent).toHaveBeenCalled();
        expect(scope.setComponent).toHaveBeenCalled();
    });

    it('resets componentCategories.selectedCategory to origin', () => {
        const {scope} = createGeneralVM();
        scope.componentCategories = {selectedCategory: 'Modified'};
        scope.revert();
        expect(scope.componentCategories.selectedCategory).toBe('Generic');
    });

    it('calls editForm.$setPristine', () => {
        const {scope} = createGeneralVM();
        scope.revert();
        expect(scope.editForm.$setPristine).toHaveBeenCalled();
    });

    it('sets unsavedChanges flag to false', () => {
        const {scope} = createGeneralVM();
        scope.revert();
        expect(scope.updateUnsavedFileFlag).toHaveBeenCalledWith(false);
    });
});

describe('GeneralViewModel - updateIcon', () => {
    it('calls ModalsHandler.openUpdateIconModal', () => {
        const {scope, mocks} = createGeneralVM();
        scope.updateIcon();
        expect(mocks.modalsHandler.openUpdateIconModal).toHaveBeenCalledWith(scope.component);
    });
});

describe('GeneralViewModel - setServiceFunction', () => {
    it('sets othersFlag=true and clears serviceFunction for Others', () => {
        const {scope} = createGeneralVM();
        scope.setServiceFunction('Others');
        expect(scope.othersFlag).toBe(true);
        expect(scope.component.serviceFunction).toBe('');
    });

    it('sets serviceFunction to option value for non-Others', () => {
        const {scope} = createGeneralVM();
        scope.setServiceFunction('Streaming');
        expect(scope.othersFlag).toBe(false);
        expect(scope.component.serviceFunction).toBe('Streaming');
    });
});

describe('GeneralViewModel - setServiceRole', () => {
    it('sets othersRoleFlag=true for Others', () => {
        const {scope} = createGeneralVM();
        scope.setServiceRole('Others');
        expect(scope.othersRoleFlag).toBe(true);
        expect(scope.component.serviceRole).toBe('');
    });

    it('sets serviceRole to option value for non-Others', () => {
        const {scope} = createGeneralVM();
        scope.setServiceRole('Transport');
        expect(scope.othersRoleFlag).toBe(false);
        expect(scope.component.serviceRole).toBe('Transport');
    });
});
