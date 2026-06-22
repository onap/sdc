import {GeneralViewModel} from 'app/view-models/workspace/tabs/general/general-view-model';
import {ComponentState, ComponentType, ResourceType, WorkspaceMode} from 'app/utils/constants';
import {createMockComponent, createServiceComponent} from '../../../../../jest/mocks/workspace-component.mock';
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
        csarUUID: undefined
    });

    const scope: any = {
        $on: jest.fn(),
        $watch: jest.fn(),
        $watchCollection: jest.fn(),
        $broadcast: jest.fn(),
        component,
        originComponent: {...component, name: 'TestComponent', selectedCategory: 'Generic'},
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
            if (key === 'resourceCategories') return [{name: 'Generic', subcategories: [{name: 'Abstract'}]}, {name: 'Network', subcategories: [{name: 'L2'}, {name: 'L3'}]}];
            if (key === 'serviceCategories') return [{name: 'Mobility', subcategories: [{name: 'Core'}]}, {name: 'Network', subcategories: [{name: 'Transport'}]}];
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
    const modalsHandler = {openUpdateIconModal: jest.fn(() => Promise.resolve(false))};
    const modalServiceSdcUI = {openErrorDetailModal: jest.fn()};
    const notification = {success: jest.fn(), info: jest.fn(), error: jest.fn()};
    const componentFactory = {createComponent: jest.fn((c) => ({...c})), updateComponentFromCsar: jest.fn()};
    const validationUtils = {};
    const fileUtils = {base64toBlob: jest.fn()};
    const onBoardingService = {getOnboardingVSPs: jest.fn(() => ({map: jest.fn(() => ({subscribe: jest.fn()}))}))};
    const importVSPService = {openOnboardingModal: jest.fn(() => ({subscribe: jest.fn()}))};
    const elementService = {getCategoryBaseTypes: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb({required: false, baseTypes: []}))}))};
    const modelService = {
        getModels: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb([]))})),
        getModelsOfType: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb([]))}))
    };
    const sdcConfig = {csarFileExtension: ['csar', 'zip'], toscaFileExtension: ['yaml', 'yml']};

    const vm = new GeneralViewModel(
        scope,
        cacheService,
        /^[\s\S]*$/,  // ComponentNameValidationPattern
        /^[\s\S]*$/,  // ContactIdValidationPattern
        /^[\s\S]*$/,  // TagValidationPattern
        /^[\s\S]*$/,  // VendorReleaseValidationPattern
        /^[\s\S]*$/,  // VendorNameValidationPattern
        /^[\s\S]*$/,  // VendorModelNumberValidationPattern
        /^[\s\S]*$/,  // CommentValidationPattern
        validationUtils as any,
        fileUtils as any,
        sdcConfig as any,
        $state,
        modalsHandler as any,
        modalServiceSdcUI as any,
        eventListenerService as any,
        notification,
        progressService as any,
        jest.fn(),     // $interval
        $filter,
        jest.fn(),     // $timeout
        onBoardingService as any,
        componentFactory as any,
        importVSPService as any,
        elementService as any,
        modelService as any,
        $stateParams
    );

    return {vm, scope, mocks: {cacheService, $state, $stateParams, $filter, eventListenerService, componentFactory, elementService, modelService, modalsHandler, notification}};
}

describe('GeneralViewModel - initScopeValidation', () => {
    it('sets all validation patterns on scope', () => {
        const {scope} = createGeneralVM();
        expect(scope.validation).toBeDefined();
        expect(scope.validation.componentNameValidationPattern).toBeInstanceOf(RegExp);
        expect(scope.validation.contactIdValidationPattern).toBeInstanceOf(RegExp);
        expect(scope.validation.tagValidationPattern).toBeInstanceOf(RegExp);
        expect(scope.validation.VendorReleaseValidationPattern).toBeInstanceOf(RegExp);
        expect(scope.validation.VendorNameValidationPattern).toBeInstanceOf(RegExp);
        expect(scope.validation.VendorModelNumberValidationPattern).toBeInstanceOf(RegExp);
        expect(scope.validation.commentValidationPattern).toBeInstanceOf(RegExp);
    });
});

describe('GeneralViewModel - validateName', () => {
    it('does nothing when name is empty', () => {
        const {scope} = createGeneralVM();
        scope.component.name = '';
        scope.validateName(false);
        expect(scope.component.validateName).not.toHaveBeenCalled();
    });

    it('clears nameExist error when name is empty', () => {
        const {scope} = createGeneralVM();
        scope.component.name = '';
        scope.validateName(false);
        expect(scope.editForm.componentName.$setValidity).toHaveBeenCalledWith('nameExist', true);
    });

    it('calls server validation when name differs from origin (non-init)', () => {
        const {scope} = createGeneralVM();
        scope.component.name = 'NewName';
        scope.originComponent.name = 'TestComponent';
        scope.validateName(false);
        expect(scope.component.validateName).toHaveBeenCalledWith('NewName', 'VF');
    });

    it('skips validation for CSAR resource', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
            componentType: ComponentType.RESOURCE,
            resourceType: ResourceType.VF,
            getAllVersionsAsSortedArray: jest.fn(() => []),
            getComponentSubType: jest.fn(() => 'VF'),
            getStatus: jest.fn(() => 'IN DESIGN'),
            isLatestVersion: jest.fn(() => true),
            isCsarComponent: jest.fn(() => true),
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
            csarUUID: 'some-uuid'
        });
        const {scope} = createGeneralVM({component});
        scope.component.name = 'NewName';
        scope.originComponent.name = 'OldName';
        scope.validateName(false);
        expect(scope.component.validateName).not.toHaveBeenCalled();
    });

    it('clears nameExist when name matches origin (case-insensitive)', () => {
        const {scope} = createGeneralVM();
        scope.component.name = 'testcomponent';
        scope.originComponent.name = 'TestComponent';
        scope.validateName(false);
        expect(scope.editForm.componentName.$setValidity).toHaveBeenCalledWith('nameExist', true);
    });

    it('on init: validates only when name changed from origin', () => {
        const {scope} = createGeneralVM();
        scope.component.name = 'ChangedName';
        scope.originComponent.name = 'TestComponent';
        scope.validateName(true);
        expect(scope.component.validateName).toHaveBeenCalledWith('ChangedName', 'VF');
    });

    it('on init: skips validation when name matches origin', () => {
        const {scope} = createGeneralVM();
        scope.component.name = 'TestComponent';
        scope.originComponent.name = 'TestComponent';
        scope.validateName(true);
        expect(scope.component.validateName).not.toHaveBeenCalled();
    });
});

describe('GeneralViewModel - initCategories', () => {
    it('gets resourceCategories from cache for RESOURCE type', () => {
        const {scope, mocks} = createGeneralVM();
        scope.componentType = ComponentType.RESOURCE;
        scope.initCategories();
        expect(mocks.cacheService.get).toHaveBeenCalledWith('resourceCategories');
        expect(scope.categories).toBeDefined();
        expect(scope.categories.length).toBe(2);
    });

    it('gets serviceCategories from cache for SERVICE type', () => {
        const component = createServiceComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
            getAllVersionsAsSortedArray: jest.fn(() => []),
            getComponentSubType: jest.fn(() => 'SERVICE'),
            getStatus: jest.fn(() => 'IN DESIGN'),
            isLatestVersion: jest.fn(() => true),
            isCsarComponent: jest.fn(() => false),
            isAlreadyCertified: jest.fn(() => false),
            isArchived: false,
            modules: [],
            tags: ['TestService'],
            selectedCategory: 'Mobility',
            categories: [{name: 'Mobility', subcategories: [{name: 'Core'}]}],
            validateName: jest.fn(() => Promise.resolve({isValid: true})),
            componentMetadata: {componentType: ComponentType.SERVICE},
            vendorName: undefined,
            importedFile: undefined,
            csarUUID: undefined,
            serviceType: 'Service',
            instantiationType: 'A-la-carte'
        });
        const {scope, mocks} = createGeneralVM({component, isEditMode: true});
        scope.componentType = ComponentType.SERVICE;
        scope.initCategories();
        expect(mocks.cacheService.get).toHaveBeenCalledWith('serviceCategories');
    });
});

describe('GeneralViewModel - validateField', () => {
    it('returns true when field is dirty and invalid', () => {
        const {scope} = createGeneralVM();
        const field = {$dirty: true, $invalid: true};
        expect(scope.validateField(field)).toBe(true);
    });

    it('returns false when field is clean', () => {
        const {scope} = createGeneralVM();
        const field = {$dirty: false, $invalid: true};
        expect(scope.validateField(field)).toBe(false);
    });

    it('returns false when field is valid', () => {
        const {scope} = createGeneralVM();
        const field = {$dirty: true, $invalid: false};
        expect(scope.validateField(field)).toBe(false);
    });

    it('returns false for undefined field', () => {
        const {scope} = createGeneralVM();
        expect(scope.validateField(undefined)).toBe(false);
    });
});

describe('GeneralViewModel - possibleToUpdateIcon', () => {
    it('returns false without category selected', () => {
        const {scope} = createGeneralVM();
        scope.componentCategories = {selectedCategory: ''};
        expect(scope.possibleToUpdateIcon()).toBe(false);
    });

    it('returns false without vendorName on resource', () => {
        const {scope} = createGeneralVM();
        scope.componentCategories = {selectedCategory: 'Generic'};
        scope.component.vendorName = '';
        expect(scope.possibleToUpdateIcon()).toBe(false);
    });

    it('returns false when already certified', () => {
        const {scope} = createGeneralVM();
        scope.componentCategories = {selectedCategory: 'Generic'};
        scope.component.isAlreadyCertified = jest.fn(() => true);
        expect(scope.possibleToUpdateIcon()).toBe(false);
    });

    it('returns true when all conditions met', () => {
        const {scope} = createGeneralVM();
        scope.componentCategories = {selectedCategory: 'Generic'};
        scope.component.vendorName = 'TestVendor';
        scope.component.isAlreadyCertified = jest.fn(() => false);
        expect(scope.possibleToUpdateIcon()).toBe(true);
    });

    it('returns true for service without vendorName (vendorName not required for services)', () => {
        const {scope} = createGeneralVM();
        scope.componentCategories = {selectedCategory: 'Mobility'};
        scope.component.isResource = jest.fn(() => false);
        scope.component.vendorName = undefined;
        scope.component.isAlreadyCertified = jest.fn(() => false);
        expect(scope.possibleToUpdateIcon()).toBe(true);
    });
});

describe('GeneralViewModel - isVspImport', () => {
    it('returns false for non-resource', () => {
        const component = createServiceComponent({
            getAllVersionsAsSortedArray: jest.fn(() => []),
            getComponentSubType: jest.fn(() => 'SERVICE'),
            getStatus: jest.fn(() => 'IN DESIGN'),
            isLatestVersion: jest.fn(() => true),
            isCsarComponent: jest.fn(() => false),
            isAlreadyCertified: jest.fn(() => false),
            modules: [],
            tags: [],
            selectedCategory: 'Mobility',
            categories: [{name: 'Mobility', subcategories: [{name: 'Core'}]}],
            validateName: jest.fn(),
            componentMetadata: {componentType: ComponentType.SERVICE},
            importedFile: undefined,
            csarUUID: undefined,
            serviceType: 'Service',
            instantiationType: 'A-la-carte'
        });
        const {scope} = createGeneralVM({component});
        expect(scope.isVspImport()).toBe(false);
    });

    it('returns true for resource with csarUUID', () => {
        const component = createMockComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
            componentType: ComponentType.RESOURCE,
            resourceType: ResourceType.VF,
            getAllVersionsAsSortedArray: jest.fn(() => []),
            getComponentSubType: jest.fn(() => 'VF'),
            getStatus: jest.fn(() => 'IN DESIGN'),
            isLatestVersion: jest.fn(() => true),
            isCsarComponent: jest.fn(() => true),
            isAlreadyCertified: jest.fn(() => false),
            modules: [],
            tags: [],
            selectedCategory: 'Generic',
            categories: [{name: 'Generic', subcategories: [{name: 'Abstract'}]}],
            validateName: jest.fn(),
            componentMetadata: {componentType: ComponentType.RESOURCE},
            vendorName: 'V',
            importedFile: undefined,
            csarUUID: 'uuid-123'
        });
        const {scope} = createGeneralVM({component});
        expect(scope.isVspImport()).toBe(true);
    });
});
