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
        isCsarComponent: jest.fn(() => options.isCsar || false),
        isAlreadyCertified: jest.fn(() => false),
        isArchived: false,
        modules: [],
        tags: ['TestComponent'],
        selectedCategory: 'Generic',
        categories: [{name: 'Generic', subcategories: [{name: 'Abstract'}]}],
        validateName: jest.fn(() => Promise.resolve({isValid: true})),
        componentMetadata: options.componentMetadata || {componentType: ComponentType.RESOURCE},
        vendorName: 'TestVendor',
        importedFile: undefined,
        csarUUID: options.csarUUID || undefined,
        csarVersion: options.csarVersion || undefined,
        derivedFromGenericType: undefined,
        model: undefined,
        vspArchived: false
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
    const modalsHandler = {openUpdateIconModal: jest.fn(() => Promise.resolve(false))};
    const modalServiceSdcUI = {openErrorDetailModal: jest.fn()};
    const notification = {success: jest.fn(), info: jest.fn(), error: jest.fn()};
    const componentFactory = {createComponent: jest.fn((c) => ({...c})), getComponentWithMetadataFromServer: jest.fn(), updateComponentFromCsar: jest.fn()};
    const validationUtils = {};
    const fileUtils = {base64toBlob: jest.fn()};
    const onBoardingService = {getOnboardingVSPs: jest.fn(() => ({map: jest.fn(() => ({subscribe: jest.fn()}))}))};
    const importVSPService = {openOnboardingModal: jest.fn(() => ({subscribe: jest.fn()}))};
    const elementService = {getCategoryBaseTypes: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb({required: false, baseTypes: [{toscaResourceName: 'org.openecomp.resource.abstract.nodes.service', versions: ['1.0', '2.0']}]}))}))};
    const modelService = {
        getModels: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb([{name: 'Model1', modelType: 'NORMATIVE'}, {name: 'Model2', modelType: 'NORMATIVE_EXTENSION', derivedFrom: 'Model1'}]))})),
        getModelsOfType: jest.fn(() => ({subscribe: jest.fn((cb: Function) => cb([{name: 'NormModel1'}]))}))
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

    return {vm, scope, mocks: {cacheService, $state, $stateParams, elementService, modelService, importVSPService, componentFactory}};
}

describe('GeneralViewModel - initModel', () => {
    it('for non-VSP resource: calls getModelsOfType normative', () => {
        const {scope, mocks} = createGeneralVM();
        scope.componentType = ComponentType.RESOURCE;
        scope.initModel();
        expect(mocks.modelService.getModelsOfType).toHaveBeenCalledWith('normative');
    });

    it('for create mode VSP with single model: sets model directly', () => {
        const {scope} = createGeneralVM({
            isCreateMode: true,
            isCsar: true,
            csarUUID: 'uuid-1',
            componentMetadata: {componentType: ComponentType.RESOURCE, models: ['SingleModel']}
        });
        scope.initModel();
        expect(scope.component.model).toBe('SingleModel');
        expect(scope.isModelRequired).toBe(true);
        expect(scope.showDefaultModelOption).toBe(false);
    });

    it('for create mode VSP with multiple models: presents sorted options', () => {
        const {scope} = createGeneralVM({
            isCreateMode: true,
            isCsar: true,
            csarUUID: 'uuid-1',
            componentMetadata: {componentType: ComponentType.RESOURCE, models: ['Zeta', 'Alpha', 'Beta']}
        });
        scope.initModel();
        expect(scope.models).toEqual(['Alpha', 'Beta', 'Zeta']);
        expect(scope.isModelRequired).toBe(true);
        expect(scope.defaultModelOption).toBe('Select');
    });

    it('for non-create VSP: calls modelService.getModels', () => {
        const {scope, mocks} = createGeneralVM({isCsar: true, csarUUID: 'uuid-1'});
        scope.initModel();
        expect(mocks.modelService.getModels).toHaveBeenCalled();
    });
});

describe('GeneralViewModel - initBaseTypes', () => {
    it('returns early when derivedFromGenericType is undefined', () => {
        const component = createServiceComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
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
            instantiationType: 'A-la-carte',
            derivedFromGenericType: undefined
        });
        const {scope, mocks} = createGeneralVM({component});
        scope.componentType = ComponentType.SERVICE;
        scope.initBaseTypes();
        expect(scope.component.derivedFromGenericVersion).toBeUndefined();
        expect(scope.showBaseTypeVersions).toBe(false);
    });

    it('calls elementService.getCategoryBaseTypes when derivedFromGenericType is set', () => {
        const component = createServiceComponent({
            lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
            lastUpdaterUserId: 'cs0008',
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
            instantiationType: 'A-la-carte',
            derivedFromGenericType: 'org.openecomp.resource.abstract.nodes.service',
            model: null
        });
        const {scope, mocks} = createGeneralVM({component});
        scope.componentType = ComponentType.SERVICE;
        scope.initBaseTypes();
        expect(mocks.elementService.getCategoryBaseTypes).toHaveBeenCalledWith('Mobility', null);
    });
});

describe('GeneralViewModel - openOnBoardingModal', () => {
    it('returns early if component is archived', () => {
        const {scope, mocks} = createGeneralVM();
        scope.component.vspArchived = true;
        scope.openOnBoardingModal();
        expect(mocks.importVSPService.openOnboardingModal).not.toHaveBeenCalled();
    });

    it('calls importVSPService.openOnboardingModal with csarUUID/version', () => {
        const {scope, mocks} = createGeneralVM({csarUUID: 'uuid-123', csarVersion: '2.0'});
        scope.component.vspArchived = false;
        scope.openOnBoardingModal();
        expect(mocks.importVSPService.openOnboardingModal).toHaveBeenCalledWith('uuid-123', '2.0');
    });
});
