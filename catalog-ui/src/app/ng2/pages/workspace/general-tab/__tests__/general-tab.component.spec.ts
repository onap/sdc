import {GeneralTabComponent} from '../general-tab.component';
import {GeneralFormService} from '../general-form.service';
import {ComponentMetadataService} from '../component-metadata.service';
import {of} from 'rxjs';

const PATTERNS = {
    ComponentNameValidationPattern: /^(?=.*[^. ])[\s\w\&_.:-]{1,1024}$/,
    ContactIdValidationPattern: /^[\s\w-]{1,50}$/,
    TagValidationPattern: /^[\s\w_.-]{1,50}$/,
    VendorNameValidationPattern: /^.{1,60}$/,
    VendorReleaseValidationPattern: /^.{1,25}$/,
    VendorModelNumberValidationPattern: /^.{1,65}$/,
    CommentValidationPattern: /^[ -¿]*$/
};

function makeComponent(overrides: any = {}) {
    return Object.assign({
        name: 'MyVF', description: 'desc', vendorName: 'v', vendorRelease: '1',
        contactId: 'cs0008', tags: ['MyVF'], selectedCategory: 'cat',
        lifecycleState: 'NOT_CERTIFIED_CHECKOUT', lastUpdaterUserId: 'cs0008',
        uniqueId: 'id-1', componentType: 'RESOURCE',
        isService: () => false, isResource: () => true,
        getComponentSubType: () => 'VF',
        updateComponent: jest.fn(function() { return Promise.resolve(this); }),
        componentMetadata: {}, categorySpecificMetadata: {}
    }, overrides);
}

function makeInjector(stateParams: any = {id: 'id-1'}) {
    const services: any = {
        '$state': {current: {name: 'workspace.general', data: {unsavedChanges: false}}, go: jest.fn()},
        '$stateParams': stateParams,
        '$filter': () => (k: string) => k,
        'ComponentFactory': {createComponent: (c: any) => Object.assign({}, c)},
        'ImportVSPService': {},
        'OnboardingService': {},
        'Notification': {success: jest.fn(), error: jest.fn()},
        'ModelService': {getModels: jest.fn(() => of([])), getModelsOfType: jest.fn(() => of([]))},
        'ElementService': {getCategoryBaseTypes: jest.fn(() => of({required: false, baseTypes: []}))},
        'sdcConfig': {csarFileExtension: ['csar'], toscaFileExtension: ['yaml', 'yml']},
        'ComponentNameValidationPattern': PATTERNS.ComponentNameValidationPattern,
        'ContactIdValidationPattern': PATTERNS.ContactIdValidationPattern,
        'TagValidationPattern': PATTERNS.TagValidationPattern,
        'VendorNameValidationPattern': PATTERNS.VendorNameValidationPattern,
        'VendorReleaseValidationPattern': PATTERNS.VendorReleaseValidationPattern,
        'VendorModelNumberValidationPattern': PATTERNS.VendorModelNumberValidationPattern,
        'CommentValidationPattern': PATTERNS.CommentValidationPattern
    };
    return {get: (name: string) => services[name]};
}

function createComp(opts: any = {}) {
    const component = opts.component || makeComponent();
    const workspaceService: any = {component, isValidForm: true, containerActions: opts.containerActions,
        setComponent: jest.fn(function(c: any) { this.component = c; })};
    const cacheService: any = {get: jest.fn((k: string) => k === 'user' ? {userId: 'cs0008', role: 'DESIGNER'} : null), set: jest.fn(), remove: jest.fn(), contains: jest.fn(() => false)};
    const eventListener: any = {registerObserverCallback: jest.fn(), unRegisterObserver: jest.fn(), notifyObservers: jest.fn()};
    const cdr: any = {detectChanges: jest.fn()};
    const comp = new GeneralTabComponent(
        new GeneralFormService(), new ComponentMetadataService(),
        workspaceService, cacheService, eventListener, cdr,
        opts.injector || makeInjector(opts.stateParams)
    );
    return {comp, workspaceService, cacheService, eventListener, cdr};
}

describe('GeneralTabComponent - init', () => {
    it('builds the reactive form on ngOnInit', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.form).toBeTruthy();
        expect(comp.form.get('name')).toBeTruthy();
    });

    it('reads the component from WorkspaceService', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.component.name).toBe('MyVF');
    });

    it('is EDIT mode for a checked-out component owned by the user', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.isEditMode()).toBe(true);
        expect(comp.isCreateMode()).toBe(false);
    });

    it('is CREATE mode when stateParams has no id', () => {
        const {comp} = createComp({stateParams: {}});
        comp.ngOnInit();
        expect(comp.isCreateMode()).toBe(true);
    });

    it('completes destroy$ on ngOnDestroy', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        const spy = jest.fn();
        (comp as any).destroy$.subscribe({complete: spy});
        comp.ngOnDestroy();
        expect(spy).toHaveBeenCalled();
    });
});

describe('GeneralTabComponent - save (data-loss fix)', () => {
    it('save() writes the edited form into the component and PUTs it', async () => {
        const {comp, workspaceService} = createComp();
        comp.ngOnInit();
        comp.form.get('description').setValue('edited description');
        await comp.save();
        expect(workspaceService.component.description).toBe('edited description');
        expect(workspaceService.component.updateComponent).toHaveBeenCalled();
    });

    it('save() resolves on a successful updateComponent', async () => {
        const {comp} = createComp();
        comp.ngOnInit();
        await expect(comp.save()).resolves.toBeUndefined();
    });

    it('save() rejects when updateComponent fails', async () => {
        const failing = makeComponent({updateComponent: jest.fn(() => Promise.reject('boom'))});
        const {comp} = createComp({component: failing});
        comp.ngOnInit();
        await expect(comp.save()).rejects.toBeDefined();
    });

    it('ON_LIFECYCLE_CHANGE_WITH_SAVE saves then delegates to the shell when dirty+valid', async () => {
        const handleChangeLifecycleState = jest.fn();
        const startProgress = jest.fn();
        const stopProgress = jest.fn();
        const {comp, eventListener} = createComp({
            containerActions: {handleChangeLifecycleState, startProgress, stopProgress}
        });
        comp.ngOnInit();
        // grab the registered ON_LIFECYCLE_CHANGE_WITH_SAVE callback
        const call = eventListener.registerObserverCallback.mock.calls
            .find((c: any[]) => c[0] === 'onLifecycleChangeWithSave');
        expect(call).toBeDefined();
        (comp as any).$state.current.data.unsavedChanges = true;
        comp.form.get('description').setValue('x'); // make form valid+dirty
        await call[1]('certify');
        expect(handleChangeLifecycleState).toHaveBeenCalledWith('certify');
    });
});

describe('GeneralTabComponent - revert', () => {
    it('revert() restores the form to originComponent values and clears dirty', () => {
        const {comp, workspaceService} = createComp();
        comp.ngOnInit();
        comp.form.get('description').setValue('changed');
        comp.form.markAsDirty();
        comp.revert();
        expect(comp.form.get('description').value).toBe('desc');
        expect(comp.form.dirty).toBe(false);
        expect(workspaceService.isValidForm).toBe(true);
    });
});

describe('GeneralTabComponent - VSP import', () => {
    it('isVspImport is true for a resource with a csarUUID', () => {
        const csarComp = makeComponent({csarUUID: 'csar-1', isResource: () => true, isService: () => false});
        const {comp} = createComp({component: csarComp});
        comp.ngOnInit();
        expect(comp.isVspImport()).toBe(true);
    });

    it('isVspImport is false for a component without a csarUUID', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.isVspImport()).toBe(false);
    });
});

describe('GeneralTabComponent - dirty tracking', () => {
    it('sets unsavedChanges=true in EDIT mode when form becomes dirty', () => {
        const {comp} = createComp(); // EDIT mode: has id + checked-out owned by cs0008
        comp.ngOnInit();
        // Simulate user editing — setValue marks form dirty and emits valueChanges
        comp.form.get('description').setValue('new value');
        comp.form.markAsDirty();
        // Trigger a valueChanges emission to fire the subscription
        comp.form.get('description').setValue('new value 2');
        expect((comp as any).$state.current.data.unsavedChanges).toBe(true);
    });

    it('does NOT set unsavedChanges=true in CREATE mode when form becomes dirty', () => {
        const {comp} = createComp({stateParams: {}}); // CREATE mode: no id
        comp.ngOnInit();
        comp.form.get('description').setValue('new value');
        comp.form.markAsDirty();
        comp.form.get('description').setValue('new value 2');
        expect((comp as any).$state.current.data.unsavedChanges).toBe(false);
    });

    it('save fires before lifecycle change when form is dirty+valid in EDIT mode (regression guard)', async () => {
        const handleChangeLifecycleState = jest.fn();
        const startProgress = jest.fn();
        const stopProgress = jest.fn();
        const component = makeComponent();
        const {comp, eventListener} = createComp({
            component,
            containerActions: {handleChangeLifecycleState, startProgress, stopProgress}
        });
        comp.ngOnInit();

        // Edit field to make form dirty, which should auto-set unsavedChanges=true
        comp.form.get('description').setValue('edited before certify');
        comp.form.markAsDirty();
        comp.form.get('description').setValue('edited before certify 2');

        // Verify unsavedChanges was set by dirty tracking
        expect((comp as any).$state.current.data.unsavedChanges).toBe(true);

        // Fire the lifecycle callback — should save first, then delegate
        const call = eventListener.registerObserverCallback.mock.calls
            .find((c: any[]) => c[0] === 'onLifecycleChangeWithSave');
        expect(call).toBeDefined();
        await call[1]('certify');

        // Both the PUT (updateComponent) and the lifecycle change must have fired
        expect(component.updateComponent).toHaveBeenCalled();
        expect(handleChangeLifecycleState).toHaveBeenCalledWith('certify');
    });
});

// ─── Task 8c: initCategories tests ───────────────────────────────────────────

/** Extend makeInjector to include sdcMenu with hiddenCategories. */
function makeInjectorWithCategories(stateParams: any = {id: 'id-1'}, sdcMenuOverride?: any) {
    const defaultSdcMenu = {
        component_workspace_menu_option: {
            VF:      [{hiddenCategories: []}],
            VFC:     [{hiddenCategories: []}],
            SERVICE: [{hiddenCategories: ['SomeHidden']}]
        }
    };
    const sdcMenu = sdcMenuOverride || defaultSdcMenu;
    const services: any = {
        '$state': {current: {name: 'workspace.general', data: {unsavedChanges: false}}, go: jest.fn()},
        '$stateParams': stateParams,
        '$filter': () => (k: string) => k,
        'ComponentFactory': {createComponent: (c: any) => Object.assign({}, c)},
        'ImportVSPService': {},
        'OnboardingService': {},
        'Notification': {success: jest.fn(), error: jest.fn()},
        'ModelService': {getModels: jest.fn(() => of([])), getModelsOfType: jest.fn(() => of([]))},
        'ElementService': {getCategoryBaseTypes: jest.fn(() => of({required: false, baseTypes: []}))},
        'sdcMenu': sdcMenu,
        'sdcConfig': {csarFileExtension: ['csar'], toscaFileExtension: ['yaml', 'yml']},
        'ComponentNameValidationPattern': PATTERNS.ComponentNameValidationPattern,
        'ContactIdValidationPattern': PATTERNS.ContactIdValidationPattern,
        'TagValidationPattern': PATTERNS.TagValidationPattern,
        'VendorNameValidationPattern': PATTERNS.VendorNameValidationPattern,
        'VendorReleaseValidationPattern': PATTERNS.VendorReleaseValidationPattern,
        'VendorModelNumberValidationPattern': PATTERNS.VendorModelNumberValidationPattern,
        'CommentValidationPattern': PATTERNS.CommentValidationPattern
    };
    return {get: (name: string) => services[name]};
}

const RESOURCE_CATEGORIES = [
    {name: 'Common Network Resources', subcategories: [{name: 'Infrastructure'}, {name: 'Network Elements'}]},
    {name: 'Application Layer 4+', subcategories: [{name: 'Application Servers'}]}
];

const SERVICE_CATEGORIES = [
    {name: 'Network Service', subcategories: []},
    {name: 'SomeHidden', subcategories: []}
];

describe('GeneralTabComponent - Task 8c categories', () => {

    // Test 1 (regression guard): Resource categories load from cache into comp.categories
    it('Resource categories load from cache into comp.categories (non-empty)', () => {
        const resourceComp = makeComponent(); // RESOURCE, create mode (no id)
        const injector = makeInjectorWithCategories({});
        const {comp, cacheService} = createComp({component: resourceComp, injector, stateParams: {}});
        cacheService.get = jest.fn((k: string) => {
            if (k === 'user') { return {userId: 'cs0008', role: 'DESIGNER'}; }
            if (k === 'resourceCategories') { return RESOURCE_CATEGORIES; }
            return null;
        });
        comp.ngOnInit();
        expect((comp as any).categories).toBeDefined();
        expect((comp as any).categories.length).toBeGreaterThan(0);
        expect((comp as any).categories[0].name).toBe('Common Network Resources');
    });

    // Test 2: Service categories are loaded and the hidden category is excluded in create mode
    it('Service categories load and External-API filter excludes hidden categories in create mode', () => {
        const serviceComp = makeComponent(Object.assign({
            componentType: 'SERVICE',
            isService: () => true,
            isResource: () => false,
            getComponentSubType: () => 'SERVICE',
            lifecycleState: 'NOT_CERTIFIED_CHECKOUT',
            selectedCategory: 'Network Service'
        }));
        const injector = makeInjectorWithCategories({}); // create mode: no id
        const {comp, cacheService} = createComp({component: serviceComp, injector, stateParams: {}});
        cacheService.get = jest.fn((k: string) => {
            if (k === 'user') { return {userId: 'cs0008', role: 'DESIGNER'}; }
            if (k === 'serviceCategories') { return SERVICE_CATEGORIES; }
            return null;
        });
        comp.ngOnInit();
        const cats: any[] = (comp as any).categories;
        expect(cats).toBeDefined();
        // 'Network Service' should be included; 'SomeHidden' should be filtered out
        const names = cats.map((c: any) => c.name);
        expect(names).toContain('Network Service');
        expect(names).not.toContain('SomeHidden');
    });

    // Test 3: categories are populated before initModel so the dropdown is ready on first render
    it('categories are populated when ngOnInit completes in create mode (ready for Selenium selectByVisibleText)', () => {
        const resourceComp = makeComponent();
        const injector = makeInjectorWithCategories({});
        const {comp, cacheService} = createComp({component: resourceComp, injector, stateParams: {}});
        cacheService.get = jest.fn((k: string) => {
            if (k === 'user') { return {userId: 'cs0008', role: 'DESIGNER'}; }
            if (k === 'resourceCategories') { return RESOURCE_CATEGORIES; }
            return null;
        });
        comp.ngOnInit();
        // Categories must be non-empty synchronously after ngOnInit (cache is sync, no async)
        expect(Array.isArray((comp as any).categories)).toBe(true);
        expect((comp as any).categories.length).toBe(2);
    });
});

// ─── Task 8b: data-init tests ────────────────────────────────────────────────

function makeServiceComponent(overrides: any = {}) {
    return makeComponent(Object.assign({
        componentType: 'SERVICE',
        isService: () => true,
        isResource: () => false,
        instantiationType: '',
        environmentContext: undefined,
        model: undefined,
        categories: [{name: 'Network Service', subcategories: []}],
        derivedFromGenericType: null,
        componentMetadata: {}
    }, overrides));
}

function makeInjectorWithModels(stateParams: any = {id: 'id-1'}, modelServiceOverrides: any = {}, elementServiceOverrides: any = {}) {
    const modelService = Object.assign({
        getModels: jest.fn(() => of([])),
        getModelsOfType: jest.fn(() => of([]))
    }, modelServiceOverrides);
    const elementService = Object.assign({
        getCategoryBaseTypes: jest.fn(() => of({required: false, baseTypes: []}))
    }, elementServiceOverrides);
    const services: any = {
        '$state': {current: {name: 'workspace.general', data: {unsavedChanges: false}}, go: jest.fn()},
        '$stateParams': stateParams,
        '$filter': () => (k: string) => k,
        'ComponentFactory': {createComponent: (c: any) => Object.assign({}, c)},
        'ImportVSPService': {},
        'OnboardingService': {},
        'Notification': {success: jest.fn(), error: jest.fn()},
        'ModelService': modelService,
        'ElementService': elementService,
        'sdcConfig': {csarFileExtension: ['csar'], toscaFileExtension: ['yaml', 'yml']},
        'ComponentNameValidationPattern': PATTERNS.ComponentNameValidationPattern,
        'ContactIdValidationPattern': PATTERNS.ContactIdValidationPattern,
        'TagValidationPattern': PATTERNS.TagValidationPattern,
        'VendorNameValidationPattern': PATTERNS.VendorNameValidationPattern,
        'VendorReleaseValidationPattern': PATTERNS.VendorReleaseValidationPattern,
        'VendorModelNumberValidationPattern': PATTERNS.VendorModelNumberValidationPattern,
        'CommentValidationPattern': PATTERNS.CommentValidationPattern
    };
    return {get: (name: string) => services[name]};
}

// ─── Task 8d: Tags widget (addTag / deleteTag / displayedTags) ────────────────

describe('GeneralTabComponent - Task 8d tags widget', () => {

    it('addTag adds the tag to component.tags, syncs the form control, and clears newTag', () => {
        const component = makeComponent({tags: [], name: 'MyVF'});
        const {comp} = createComp({component, stateParams: {}});
        comp.ngOnInit();
        comp.newTag = 'mytag';
        comp.addTag();
        expect(comp.component.tags).toContain('mytag');
        expect(comp.form.get('tags').value).toContain('mytag');
        expect(comp.newTag).toBe('');
    });

    it('addTag rejects a duplicate tag', () => {
        const component = makeComponent({tags: ['a'], name: 'MyVF'});
        const {comp} = createComp({component, stateParams: {}});
        comp.ngOnInit();
        comp.newTag = 'a';
        comp.addTag();
        expect(comp.component.tags.length).toBe(1);
    });

    it('addTag rejects the component name (special tag)', () => {
        const component = makeComponent({tags: [], name: 'MyVF'});
        const {comp} = createComp({component, stateParams: {}});
        comp.ngOnInit();
        comp.newTag = 'MyVF';
        comp.addTag();
        expect(comp.component.tags).not.toContain('MyVF');
    });

    it('deleteTag removes the tag from component.tags and syncs the form control', () => {
        const component = makeComponent({tags: ['a', 'b'], name: 'MyVF'});
        const {comp} = createComp({component, stateParams: {}});
        comp.ngOnInit();
        comp.deleteTag('a');
        expect(comp.component.tags).not.toContain('a');
        expect(comp.component.tags).toContain('b');
        expect(comp.form.get('tags').value).not.toContain('a');
    });

    it('displayedTags excludes the component name', () => {
        const component = makeComponent({tags: ['MyVF', 'keep'], name: 'MyVF'});
        const {comp} = createComp({component, stateParams: {}});
        comp.ngOnInit();
        const displayed = comp.displayedTags;
        expect(displayed).not.toContain('MyVF');
        expect(displayed).toContain('keep');
    });
});

// ─── Task 8e: statusChanges create-mode guard (fixes disabled Create button / Selenium loader timeout) ──

describe('GeneralTabComponent - Task 8e statusChanges create-mode guard', () => {

    // Test 1: CREATE mode — dirtying the form must NOT emit ON_WORKSPACE_UNSAVED_CHANGES(true)
    it('does not emit ON_WORKSPACE_UNSAVED_CHANGES(true) in CREATE mode (Create button must stay enabled)', () => {
        const {comp, eventListener} = createComp({stateParams: {}}); // no id → CREATE mode
        comp.ngOnInit();
        expect(comp.isCreateMode()).toBe(true);

        // Dirty the form to trigger statusChanges
        comp.form.get('name').setValue('NewVFC');
        comp.form.markAsDirty();
        comp.form.get('name').updateValueAndValidity();

        // notifyObservers must never have been called with ('onWorkspaceUnsavedChanges', true, ...)
        const unsavedCalls = (eventListener.notifyObservers as jest.Mock).mock.calls
            .filter((args: any[]) => args[0] === 'onWorkspaceUnsavedChanges' && args[1] === true);
        expect(unsavedCalls.length).toBe(0);
    });

    // Test 2: EDIT mode — dirtying the form MUST emit ON_WORKSPACE_UNSAVED_CHANGES(true)
    it('emits ON_WORKSPACE_UNSAVED_CHANGES(true) in EDIT mode when form is dirtied (Task-6 behavior preserved)', () => {
        const {comp, eventListener} = createComp(); // default: EDIT mode (has id + checked-out owned by cs0008)
        comp.ngOnInit();
        expect(comp.isEditMode()).toBe(true);

        // Dirty the form to trigger statusChanges
        comp.form.get('name').setValue('EditedName');
        comp.form.markAsDirty();
        comp.form.get('name').updateValueAndValidity();

        // notifyObservers must have been called with ('onWorkspaceUnsavedChanges', true, ...) at least once
        const unsavedCalls = (eventListener.notifyObservers as jest.Mock).mock.calls
            .filter((args: any[]) => args[0] === 'onWorkspaceUnsavedChanges' && args[1] === true);
        expect(unsavedCalls.length).toBeGreaterThan(0);
    });

    // Test 3: CREATE mode — validity must still propagate (workspaceService.isValidForm) so Create button enables
    it('still propagates form validity to workspaceService.isValidForm in CREATE mode', () => {
        const {comp, workspaceService} = createComp({stateParams: {}}); // CREATE mode
        comp.ngOnInit();
        expect(comp.isCreateMode()).toBe(true);

        // Make form valid (fill required name field) and trigger statusChanges
        comp.form.get('name').setValue('ValidName');
        comp.form.get('name').updateValueAndValidity();

        // Validity must propagate regardless of mode
        expect(workspaceService.isValidForm).toBe(true);
    });
});

// ─── Task 8f: onCategoryChange builds component.categories (fixes create 400 SVC4051) ──────────

describe('GeneralTabComponent - Task 8f onCategoryChange', () => {

    // Helpers: a resource component with no selectedCategory set (we'll set it per test)
    function makeResourceForCategoryTest(selectedCategory: string = '') {
        return makeComponent({selectedCategory, tags: [], name: 'TestVF'});
    }

    function createCompForCategoryTest(resourceComp: any, categoriesOptionList: any[]) {
        const {comp, ...rest} = createComp({component: resourceComp, stateParams: {}}); // CREATE mode
        comp.ngOnInit();
        // Override the loaded categories option-list directly (simulates what initCategories loads from cache)
        (comp as any).categories = categoriesOptionList;
        return {comp, ...rest};
    }

    // Test 1 (regression guard): onCategoryChange builds a full-object component.categories
    //   from 'Network L2-3_#_Gateway' → [mainCategoryClone] with uniqueId preserved.
    it('builds component.categories with full objects from a sub-category selection (regression for create 400)', () => {
        const categoriesList = [
            {name: 'Network L2-3', uniqueId: 'main1', subcategories: [{name: 'Gateway', uniqueId: 'sub1'}]}
        ];
        const resourceComp = makeResourceForCategoryTest('Network L2-3_#_Gateway');
        const {comp} = createCompForCategoryTest(resourceComp, categoriesList);

        comp.form.get('category').setValue('Network L2-3_#_Gateway');
        comp.component.selectedCategory = 'Network L2-3_#_Gateway';
        comp.onCategoryChange();

        expect(comp.component.categories).toBeDefined();
        expect(comp.component.categories.length).toBe(1);
        expect(comp.component.categories[0].name).toBe('Network L2-3');
        expect(comp.component.categories[0].uniqueId).toBe('main1');
        expect(comp.component.categories[0].subcategories[0].name).toBe('Gateway');
    });

    // Test 2: main-only category (no _#_) works — no subcategory slice
    it('builds component.categories for a main-only category (no subcategory separator)', () => {
        const categoriesList = [
            {name: 'Generic', uniqueId: 'g1'}
        ];
        const resourceComp = makeResourceForCategoryTest('Generic');
        const {comp} = createCompForCategoryTest(resourceComp, categoriesList);

        comp.form.get('category').setValue('Generic');
        comp.component.selectedCategory = 'Generic';
        comp.onCategoryChange();

        expect(comp.component.categories).toBeDefined();
        expect(comp.component.categories.length).toBe(1);
        expect(comp.component.categories[0].name).toBe('Generic');
    });

    // Test 3: empty selection clears component.categories
    it('sets component.categories to undefined when selection is cleared', () => {
        const categoriesList = [
            {name: 'Network L2-3', uniqueId: 'main1', subcategories: [{name: 'Gateway', uniqueId: 'sub1'}]}
        ];
        const resourceComp = makeResourceForCategoryTest('');
        const {comp} = createCompForCategoryTest(resourceComp, categoriesList);

        comp.form.get('category').setValue('');
        comp.component.selectedCategory = '';
        comp.onCategoryChange();

        expect(comp.component.categories).toBeUndefined();
    });

    // Test 4: deep-clone — mutating component.categories[0] must NOT mutate the source options list
    it('deep-clones the category object so mutations do not affect the source list', () => {
        const categoriesList = [
            {name: 'Network L2-3', uniqueId: 'main1', subcategories: [{name: 'Gateway', uniqueId: 'sub1'}]}
        ];
        const resourceComp = makeResourceForCategoryTest('Network L2-3_#_Gateway');
        const {comp} = createCompForCategoryTest(resourceComp, categoriesList);

        comp.form.get('category').setValue('Network L2-3_#_Gateway');
        comp.component.selectedCategory = 'Network L2-3_#_Gateway';
        comp.onCategoryChange();

        // Mutate the returned object
        comp.component.categories[0].uniqueId = 'MUTATED';

        // Source list must be unchanged
        expect((comp as any).categories[0].uniqueId).toBe('main1');
    });
});

// ─── Task 8g: auto-set contactId in CREATE mode + default icon on category change ──

describe('GeneralTabComponent - Task 8g contactId auto-set + default icon', () => {

    // Test 1 (regression guard for SVC4049): CREATE mode auto-sets contactId from cached user.
    // The VFC-import Selenium canary never types a contactId — it relies on the UI auto-populating
    // from the logged-in user. Without this, the create POST returns 400 SVC4049 "Missing contact".
    it('CREATE mode auto-sets contactId from cached user (regression guard for SVC4049)', () => {
        // Start with a component that has no contactId — the canary never sets one
        const createModeComp = makeComponent({contactId: ''});
        const {comp, cacheService} = createComp({component: createModeComp, stateParams: {}});
        // cacheService.get('user') returns {userId: 'cs0008', role: 'DESIGNER'} (from createComp default)
        comp.ngOnInit();
        expect(comp.isCreateMode()).toBe(true);
        expect(comp.component.contactId).toBe('cs0008');
        expect(comp.form.get('contactId').value).toBe('cs0008');
    });

    // Test 2: EDIT mode must NOT overwrite an existing contactId.
    it('EDIT mode does not overwrite an existing contactId', () => {
        const editModeComp = makeComponent({contactId: 'existing-user'});
        const {comp} = createComp({component: editModeComp}); // default: EDIT mode (has id)
        comp.ngOnInit();
        expect(comp.isEditMode()).toBe(true);
        expect(comp.component.contactId).toBe('existing-user');
    });

    // Test 3: onCategoryChange sets component.icon to DEFAULT_ICON ('defaulticon') when a category is selected.
    it('onCategoryChange sets component.icon to defaulticon when a category is selected', () => {
        const categoriesList = [
            {name: 'Network L2-3', uniqueId: 'main1', subcategories: [{name: 'Gateway', uniqueId: 'sub1'}]}
        ];
        const resourceComp = makeComponent({selectedCategory: '', icon: 'someOtherIcon'});
        const {comp} = createComp({component: resourceComp, stateParams: {}});
        comp.ngOnInit();
        (comp as any).categories = categoriesList;

        comp.form.get('category').setValue('Network L2-3_#_Gateway');
        comp.onCategoryChange();

        expect(comp.component.icon).toBe('defaulticon');
    });
});

// ─── Task 8b: data-init tests ────────────────────────────────────────────────

describe('GeneralTabComponent - Task 8b data-init', () => {

    // Test 1: initInstantiationTypes for a Service component
    it('initInstantiationTypes populates A_LA_CARTE and MACRO for a Service component', () => {
        const serviceComp = makeServiceComponent();
        const injector = makeInjectorWithModels();
        const {comp} = createComp({component: serviceComp, injector});
        comp.ngOnInit();
        expect((comp as any).instantiationTypes).toContain('A-la-carte');
        expect((comp as any).instantiationTypes).toContain('Macro');
    });

    // Test 2: initInstantiationTypes does NOT run for a Resource component
    it('initInstantiationTypes leaves instantiationTypes empty for a Resource component', () => {
        const resourceComp = makeComponent(); // default is Resource
        const injector = makeInjectorWithModels();
        const {comp} = createComp({component: resourceComp, injector});
        comp.ngOnInit();
        const types = (comp as any).instantiationTypes;
        expect(!types || types.length === 0).toBe(true);
    });

    // Test 3: initEnvironmentContext sets default from UIConfiguration for a Service in create/checkout state
    it('initEnvironmentContext sets environmentContext default from UIConfiguration cache', () => {
        const serviceComp = makeServiceComponent({
            lifecycleState: 'NOT_CERTIFIED_CHECKOUT',
            lastUpdaterUserId: 'cs0008'
        });
        const injector = makeInjectorWithModels({id: 'id-1'});
        const {comp, cacheService} = createComp({component: serviceComp, injector});
        // Mock the UIConfiguration in cacheService
        cacheService.get = jest.fn((k: string) => {
            if (k === 'user') { return {userId: 'cs0008', role: 'DESIGNER'}; }
            if (k === 'UIConfiguration') {
                return {environmentContext: {defaultValue: 'General_Revenue-Bearing', validValues: ['General_Revenue-Bearing', 'General_Non-Revenue-Bearing']}};
            }
            return null;
        });
        comp.ngOnInit();
        expect((comp as any).environmentContextObj).toBeDefined();
        expect((comp as any).environmentContextObj.defaultValue).toBe('General_Revenue-Bearing');
        // Because environmentContext is undefined (falsy) and we are in checkout, it should be set
        expect(serviceComp.environmentContext).toBe('General_Revenue-Bearing');
    });

    // Test 4: initModel normative path populates models from getModelsOfType('normative')
    it('initModel normative path populates models from getModelsOfType', () => {
        const serviceComp = makeServiceComponent({csarUUID: undefined});
        const injector = makeInjectorWithModels(
            {id: 'id-1'}, // non-create mode
            {getModelsOfType: jest.fn(() => of([{name: 'modelA'}, {name: 'modelB'}]))}
        );
        const {comp} = createComp({component: serviceComp, injector});
        comp.ngOnInit();
        expect((comp as any).models).toContain('modelA');
        expect((comp as any).models).toContain('modelB');
    });

    // Test 5: tags round-trip — editing tags form control and calling save() writes back to component.tags
    it('tags round-trip: edited tags are written to component.tags on save()', async () => {
        const serviceComp = makeServiceComponent({tags: ['tag1'], name: 'MyService'});
        const injector = makeInjectorWithModels();
        const {comp, workspaceService} = createComp({component: serviceComp, injector});
        comp.ngOnInit();
        // Edit the tags form control
        comp.form.get('tags').setValue(['tag1', 'tag2']);
        await comp.save();
        // After save the component tags must include tag2 (name 'MyService' would be stripped by _.without, not tag2)
        expect(workspaceService.component.tags).toContain('tag2');
    });
});

describe('GeneralTabComponent - Service field round-trip (C1/C2/C3 fix)', () => {
    function makeServiceComp(overrides: any = {}) {
        return makeComponent(Object.assign({
            componentType: 'SERVICE',
            isService: () => true,
            isResource: () => false,
            getComponentSubType: () => 'SERVICE',
            model: 'SDC AID',
            instantiationType: 'A-la-carte',
            namingPolicy: 'np-1',
            ecompGeneratedNaming: false,
            environmentContext: 'General_Revenue-Bearing',
            serviceType: 'svc-type',
            serviceFunction: 'svc-fn',
            serviceRole: 'svc-role',
            categorySpecificMetadata: {}
        }, overrides));
    }

    it('patches Service-specific form controls FROM the component on load (C1)', () => {
        const {comp} = createComp({component: makeServiceComp()});
        comp.ngOnInit();
        expect(comp.form.get('model').value).toBe('SDC AID');
        expect(comp.form.get('instantiationType').value).toBe('A-la-carte');
        expect(comp.form.get('namingPolicy').value).toBe('np-1');
        expect(comp.form.get('ecompGeneratedNaming').value).toBe(false);
        expect(comp.form.get('environmentContext').value).toBe('General_Revenue-Bearing');
        expect(comp.form.get('serviceType').value).toBe('svc-type');
        expect(comp.form.get('serviceFunction').value).toBe('svc-fn');
        expect(comp.form.get('serviceRole').value).toBe('svc-role');
    });

    it('syncs edited Service-specific controls BACK to the component on save (C1)', async () => {
        const {comp, workspaceService} = createComp({component: makeServiceComp()});
        comp.ngOnInit();
        comp.form.get('serviceRole').setValue('edited-role');
        comp.form.get('namingPolicy').setValue('edited-policy');
        comp.form.get('serviceType').setValue('edited-type');
        await comp.save();
        expect(workspaceService.component.serviceRole).toBe('edited-role');
        expect(workspaceService.component.namingPolicy).toBe('edited-policy');
        expect(workspaceService.component.serviceType).toBe('edited-type');
    });

    it('onBaseTypeChange writes the selected base type to the component (C2)', () => {
        const {comp} = createComp({component: makeServiceComp({categories: [{name: 'cat1'}]})});
        comp.ngOnInit();
        comp.onBaseTypeChange('org.openecomp.resource.VFC');
        expect(comp.component.derivedFromGenericType).toBe('org.openecomp.resource.VFC');
    });

    it('onBaseTypeChange with empty value clears the version + hides the version select (C2)', () => {
        const {comp} = createComp({component: makeServiceComp({derivedFromGenericType: 'x'})});
        comp.ngOnInit();
        comp.onBaseTypeChange('');
        expect(comp.component.derivedFromGenericType).toBe('');
        expect(comp.component.derivedFromGenericVersion).toBeUndefined();
        expect(comp.showBaseTypeVersions).toBe(false);
    });

    it('onCategoryMetadataChange writes back into component.categorySpecificMetadata (C3)', () => {
        const {comp} = createComp({component: makeServiceComp()});
        comp.ngOnInit();
        comp.onCategoryMetadataChange('Service Role', 'role-value');
        expect(comp.component.categorySpecificMetadata['Service Role']).toBe('role-value');
    });
});
