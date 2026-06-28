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
 * Tests for the code-review follow-up fixes (CR 145593 review):
 *   #1 Service Role/Function dropdown options (getMetadataKeyValidValues + Others/free-text)
 *   #2 per-field disable predicates (model/category/vendor/instantiation/base-type)
 *   #3 base types loaded + auto-selected on category change
 *   #4 updateIcon / possibleToUpdateIcon / async name validation / componentCsar auto-save
 *   #5 isModelRequired validator + showDefaultModelOption gating
 *   #6 ComponentMetadataService used (calculateUnique)
 *   #7 tag pattern validation in addTag
 *   #9 onEcompGeneratedNamingChange clears namingPolicy
 *
 * Each behavior is ported from the old GeneralViewModel (general-view-model.ts) / general-view.html
 * which is the behavioral spec.
 */
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
        isAlreadyCertified: () => false,
        updateComponent: jest.fn(function() { return Promise.resolve(this); }),
        componentMetadata: {}, categorySpecificMetadata: {}
    }, overrides);
}

function makeService(overrides: any = {}) {
    return makeComponent(Object.assign({
        componentType: 'SERVICE', isService: () => true, isResource: () => false,
        getComponentSubType: () => 'SERVICE',
        instantiationType: '', environmentContext: undefined, model: undefined,
        categories: [{name: 'Network Service', subcategories: []}],
        tags: [], name: 'MySvc'
    }, overrides));
}

function makeInjector(opts: any = {}) {
    const modelService = Object.assign({
        getModels: jest.fn(() => of([])),
        getModelsOfType: jest.fn(() => of([]))
    }, opts.modelService || {});
    const elementService = Object.assign({
        getCategoryBaseTypes: jest.fn(() => of({required: false, baseTypes: []}))
    }, opts.elementService || {});
    const services: any = {
        '$state': {current: {name: 'workspace.general', data: {unsavedChanges: false}}, go: jest.fn()},
        '$stateParams': opts.stateParams || {id: 'id-1'},
        '$filter': () => (k: string) => k,
        'ComponentFactory': {createComponent: (c: any) => Object.assign({}, c)},
        'ImportVSPService': {},
        'OnboardingService': {},
        'Notification': {success: jest.fn(), error: jest.fn()},
        'ModelService': modelService,
        'ElementService': elementService,
        'ModalsHandler': opts.modalsHandler || {openUpdateIconModal: jest.fn(() => Promise.resolve(true))},
        'sdcMenu': opts.sdcMenu || {component_workspace_menu_option: {VF: [{hiddenCategories: []}], SERVICE: [{hiddenCategories: []}]}},
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
    const cacheService: any = {get: jest.fn((k: string) => {
        if (k === 'user') { return {userId: 'cs0008', role: 'DESIGNER'}; }
        if (opts.cache && opts.cache[k] !== undefined) { return opts.cache[k]; }
        return null;
    }), set: jest.fn(), remove: jest.fn(), contains: jest.fn(() => false)};
    const eventListener: any = {registerObserverCallback: jest.fn(), unRegisterObserver: jest.fn(), notifyObservers: jest.fn()};
    const cdr: any = {detectChanges: jest.fn()};
    const comp = new GeneralTabComponent(
        new GeneralFormService(), new ComponentMetadataService(),
        workspaceService, cacheService, eventListener, cdr,
        opts.injector || makeInjector(opts)
    );
    return {comp, workspaceService, cacheService, eventListener, cdr};
}

// ─── #1 Service Role / Function dropdowns ─────────────────────────────────────
describe('GeneralTabComponent fixes - #1 Service Role/Function dropdowns', () => {
    const ROLE_CATEGORY = {
        name: 'Network Service',
        metadataKeys: [
            {name: 'Service Role', validValues: ['R1', 'R2']},
            {name: 'Service Function', validValues: ['F1', 'F2']}
        ],
        subcategories: []
    };

    function serviceWithRoleKeys(overrides: any = {}) {
        return makeService(Object.assign({
            selectedCategory: 'Network Service',
            categories: [JSON.parse(JSON.stringify(ROLE_CATEGORY))]
        }, overrides));
    }

    // The category list must be in cache too: an existing service with selectedCategory triggers
    // onCategoryChange() in ngOnInit, which rebuilds component.categories from the loaded option-list.
    function createRoleComp(overrides: any = {}) {
        return createComp({
            component: serviceWithRoleKeys(overrides),
            cache: {serviceCategories: [JSON.parse(JSON.stringify(ROLE_CATEGORY))]}
        });
    }

    it('getMetadataKeyValidValues appends "Others" for Service Role', () => {
        const {comp} = createRoleComp();
        comp.ngOnInit();
        expect(comp.getMetadataKeyValidValues('Service Role')).toEqual(['R1', 'R2', 'Others']);
    });

    it('getMetadataKeyValidValues appends "Others" for Service Function', () => {
        const {comp} = createRoleComp();
        comp.ngOnInit();
        expect(comp.getMetadataKeyValidValues('Service Function')).toEqual(['F1', 'F2', 'Others']);
    });

    it('getMetadataKeyValidValues does NOT append "Others" for a non-role key (e.g. ETSI Version)', () => {
        const etsiCat = {name: 'C', metadataKeys: [{name: 'ETSI Version', validValues: ['2.5.1']}], subcategories: []};
        const svc = makeService({selectedCategory: 'C', categories: [etsiCat]});
        const {comp} = createComp({component: svc, cache: {serviceCategories: [etsiCat]}});
        comp.ngOnInit();
        expect(comp.getMetadataKeyValidValues('ETSI Version')).toEqual(['2.5.1']);
    });

    it('setServiceRole("Others") shows the free-text input and clears component.serviceRole', () => {
        const {comp} = createRoleComp();
        comp.ngOnInit();
        comp.setServiceRole('Others');
        expect(comp.othersRoleFlag).toBe(true);
        expect(comp.component.serviceRole).toBe('');
    });

    it('setServiceRole("R1") hides the free-text input and writes the value', () => {
        const {comp} = createRoleComp();
        comp.ngOnInit();
        comp.setServiceRole('R1');
        expect(comp.othersRoleFlag).toBe(false);
        expect(comp.component.serviceRole).toBe('R1');
    });

    it('setServiceFunction("Others") shows the free-text input and clears component.serviceFunction', () => {
        const {comp} = createRoleComp();
        comp.ngOnInit();
        comp.setServiceFunction('Others');
        expect(comp.othersFlag).toBe(true);
        expect(comp.component.serviceFunction).toBe('');
    });

    it('on init, an existing serviceRole that is in validValues selects the option (no Others)', () => {
        const {comp} = createRoleComp({serviceRole: 'R2'});
        comp.ngOnInit();
        expect(comp.roleOption).toBe('R2');
        expect(comp.othersRoleFlag).toBe(false);
    });

    it('on init, an existing serviceRole NOT in validValues selects "Others" + shows free-text', () => {
        const {comp} = createRoleComp({serviceRole: 'customRole'});
        comp.ngOnInit();
        expect(comp.roleOption).toBe('Others');
        expect(comp.othersRoleFlag).toBe(true);
    });
});

// ─── #2 per-field disable predicates ──────────────────────────────────────────
describe('GeneralTabComponent fixes - #2 disable predicates', () => {
    it('model select is disabled outside CREATE mode (old: ng-disabled="!isCreateMode()")', () => {
        const {comp} = createComp(); // EDIT mode (has id)
        comp.ngOnInit();
        expect(comp.isEditMode()).toBe(true);
        expect(comp.isModelDisabled()).toBe(true);
    });

    it('model select is enabled in CREATE mode', () => {
        const {comp} = createComp({stateParams: {}, component: makeComponent()});
        comp.ngOnInit();
        expect(comp.isCreateMode()).toBe(true);
        expect(comp.isModelDisabled()).toBe(false);
    });

    it('category is disabled for a certified component', () => {
        const certified = makeComponent({isAlreadyCertified: () => true});
        const {comp} = createComp({component: certified});
        comp.ngOnInit();
        expect(comp.isCategoryDisabled()).toBe(true);
    });

    it('category is disabled for a CSAR component that already has a selected category', () => {
        const csar = makeComponent({isCsarComponent: () => true, selectedCategory: 'Network L2-3_#_Gateway'});
        const {comp} = createComp({component: csar});
        comp.ngOnInit();
        expect(comp.isCategoryDisabled()).toBe(true);
    });

    it('category is disabled when a hidden category is selected', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        (comp as any).isHiddenCategorySelected = true;
        expect(comp.isCategoryDisabled()).toBe(true);
    });

    it('vendorName is disabled for a certified component', () => {
        const certified = makeComponent({isAlreadyCertified: () => true});
        const {comp} = createComp({component: certified});
        comp.ngOnInit();
        expect(comp.isVendorNameDisabled()).toBe(true);
    });

    it('vendorName is disabled for a CSAR component that already has a vendorName', () => {
        const csar = makeComponent({isCsarComponent: () => true, vendorName: 'acme'});
        const {comp} = createComp({component: csar});
        comp.ngOnInit();
        expect(comp.isVendorNameDisabled()).toBe(true);
    });

    it('instantiation type is disabled for a CSAR component', () => {
        const svc = makeService({isCsarComponent: () => true});
        const {comp} = createComp({component: svc});
        comp.ngOnInit();
        expect(comp.isInstantiationTypeDisabled()).toBe(true);
    });

    it('base type is disabled for a CSAR component', () => {
        const svc = makeService({isCsarComponent: () => true});
        const {comp} = createComp({component: svc});
        comp.ngOnInit();
        expect(comp.isBaseTypeDisabled()).toBe(true);
    });

    it('base type is disabled in VIEW mode', () => {
        // VIEW: has id, but not checked-out-by-me -> mode VIEW
        const viewComp = makeComponent({lifecycleState: 'CERTIFIED'});
        const {comp} = createComp({component: viewComp});
        comp.ngOnInit();
        expect(comp.isViewMode()).toBe(true);
        expect(comp.isBaseTypeDisabled()).toBe(true);
    });
});

// ─── #3 base types on category change ─────────────────────────────────────────
describe('GeneralTabComponent fixes - #3 base types loaded on category change', () => {
    it('onCategoryChange (Service, CREATE) loads base types and auto-selects the required base type', () => {
        const elementService = {
            getCategoryBaseTypes: jest.fn(() => of({
                required: true,
                baseTypes: [{toscaResourceName: 'org.openecomp.resource.abstract.nodes.service', versions: ['1.0', '2.0']}]
            }))
        };
        const svc = makeService({selectedCategory: undefined, categories: undefined, derivedFromGenericType: undefined});
        const {comp} = createComp({component: svc, injector: makeInjector({stateParams: {}, elementService})});
        comp.ngOnInit();
        (comp as any).categories = [{name: 'Network Service', subcategories: []}];
        comp.form.get('category').setValue('Network Service');
        comp.onCategoryChange();
        expect(elementService.getCategoryBaseTypes).toHaveBeenCalled();
        expect(comp.baseTypes).toContain('org.openecomp.resource.abstract.nodes.service');
        expect(comp.component.derivedFromGenericType).toBe('org.openecomp.resource.abstract.nodes.service');
        // versions are reversed (newest first), so the auto-selected version is the last in the list
        expect(comp.component.derivedFromGenericVersion).toBe('2.0');
    });

    it('onCategoryChange does NOT load base types for a Resource', () => {
        const elementService = {getCategoryBaseTypes: jest.fn(() => of({required: false, baseTypes: []}))};
        const res = makeComponent({selectedCategory: undefined});
        const {comp} = createComp({component: res, injector: makeInjector({stateParams: {}, elementService})});
        comp.ngOnInit();
        (comp as any).categories = [{name: 'Generic', uniqueId: 'g1'}];
        comp.form.get('category').setValue('Generic');
        comp.onCategoryChange();
        expect(elementService.getCategoryBaseTypes).not.toHaveBeenCalled();
    });
});

// ─── #4 dropped behaviors ─────────────────────────────────────────────────────
describe('GeneralTabComponent fixes - #4 icon / name validation / csar auto-save', () => {
    it('possibleToUpdateIcon is true for a non-certified component with a selected category', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        comp.component.selectedCategory = 'cat';
        expect(comp.possibleToUpdateIcon()).toBe(true);
    });

    it('possibleToUpdateIcon is false for a Resource with no vendorName', () => {
        const res = makeComponent({vendorName: '', selectedCategory: 'cat'});
        const {comp} = createComp({component: res});
        comp.ngOnInit();
        expect(comp.possibleToUpdateIcon()).toBe(false);
    });

    it('possibleToUpdateIcon is false for a certified component', () => {
        const certified = makeComponent({isAlreadyCertified: () => true, selectedCategory: 'cat'});
        const {comp} = createComp({component: certified});
        comp.ngOnInit();
        expect(comp.possibleToUpdateIcon()).toBe(false);
    });

    it('updateIcon opens the icon modal and, when dirty in EDIT mode, sets unsavedChanges', async () => {
        const modalsHandler = {openUpdateIconModal: jest.fn(() => Promise.resolve(true))};
        const {comp} = createComp({injector: makeInjector({modalsHandler})}); // EDIT mode
        comp.ngOnInit();
        await comp.updateIcon();
        expect(modalsHandler.openUpdateIconModal).toHaveBeenCalledWith(comp.component);
        expect((comp as any).$state.current.data.unsavedChanges).toBe(true);
    });

    it('validateName flags a duplicate name with a nameExist error', async () => {
        const dup = makeComponent({validateName: jest.fn(() => Promise.resolve({isValid: false}))});
        const {comp} = createComp({component: dup});
        comp.ngOnInit();
        comp.form.get('name').setValue('TakenName');
        await comp.validateName();
        expect(comp.form.get('name').errors && comp.form.get('name').errors.nameExist).toBe(true);
    });

    it('validateName clears a previous nameExist error when the name becomes valid', async () => {
        const ok = makeComponent({validateName: jest.fn(() => Promise.resolve({isValid: true}))});
        const {comp} = createComp({component: ok});
        comp.ngOnInit();
        comp.form.get('name').setValue('FreshName');
        comp.form.get('name').setErrors({nameExist: true});
        await comp.validateName();
        expect(comp.form.get('name').errors && comp.form.get('name').errors.nameExist).toBeFalsy();
    });

    it('auto-saves on init when navigated with a componentCsar outside CREATE mode', () => {
        const csarComp = makeComponent();
        const {comp} = createComp({component: csarComp, injector: makeInjector({stateParams: {id: 'id-1', componentCsar: {csarVersion: '2.0'}}})});
        comp.ngOnInit();
        expect(csarComp.updateComponent).toHaveBeenCalled();
    });

    it('does NOT auto-save on init for a normal (no componentCsar) navigation', () => {
        const normal = makeComponent();
        const {comp} = createComp({component: normal});
        comp.ngOnInit();
        expect(normal.updateComponent).not.toHaveBeenCalled();
    });
});

// ─── #5 isModelRequired validator + showDefaultModelOption gating ──────────────
describe('GeneralTabComponent fixes - #5 model required validator', () => {
    it('applies a required validator to the model control for a multi-model VSP import', () => {
        const vsp = makeComponent({
            csarUUID: 'csar-1', model: undefined,
            componentMetadata: {models: ['mA', 'mB']}
        });
        const {comp} = createComp({component: vsp, injector: makeInjector({stateParams: {}})}); // CREATE + VSP import
        comp.ngOnInit();
        expect((comp as any).isModelRequired).toBe(true);
        // model is empty -> required makes it invalid
        expect(comp.form.get('model').valid).toBe(false);
    });

    it('a single-model VSP import hides the default option and pre-selects the model', () => {
        const vsp = makeComponent({
            csarUUID: 'csar-1', model: undefined,
            componentMetadata: {models: ['onlyModel']}
        });
        const {comp} = createComp({component: vsp, injector: makeInjector({stateParams: {}})});
        comp.ngOnInit();
        expect((comp as any).showDefaultModelOption).toBe(false);
        expect(comp.component.model).toBe('onlyModel');
    });

    it('does NOT require the model for a normal (non-VSP) component', () => {
        const {comp} = createComp({stateParams: {}, component: makeComponent()});
        comp.ngOnInit();
        expect((comp as any).isModelRequired).toBe(false);
        expect(comp.form.get('model').valid).toBe(true);
    });
});

// ─── #6 ComponentMetadataService used ─────────────────────────────────────────
describe('GeneralTabComponent fixes - #6 calculateUnique uses ComponentMetadataService', () => {
    it('calculateUnique joins main and sub with the shared separator', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.calculateUnique('Network L2-3', 'Gateway')).toBe('Network L2-3_#_Gateway');
    });

    it('calculateUnique returns the main category alone when there is no subcategory', () => {
        const {comp} = createComp();
        comp.ngOnInit();
        expect(comp.calculateUnique('Generic', '')).toBe('Generic');
    });
});

// ─── #7 tag pattern validation ────────────────────────────────────────────────
describe('GeneralTabComponent fixes - #7 tag pattern validation', () => {
    it('addTag rejects a tag with invalid characters (pattern)', () => {
        const res = makeComponent({tags: [], name: 'MyVF'});
        const {comp} = createComp({component: res, stateParams: {}});
        comp.ngOnInit();
        comp.newTag = 'bad/tag$';
        comp.addTag();
        expect(comp.component.tags).not.toContain('bad/tag$');
    });

    it('addTag accepts a valid tag', () => {
        const res = makeComponent({tags: [], name: 'MyVF'});
        const {comp} = createComp({component: res, stateParams: {}});
        comp.ngOnInit();
        comp.newTag = 'valid_tag';
        comp.addTag();
        expect(comp.component.tags).toContain('valid_tag');
    });
});

// ─── #9 onEcompGeneratedNamingChange ──────────────────────────────────────────
describe('GeneralTabComponent fixes - #9 onEcompGeneratedNamingChange', () => {
    it('clears namingPolicy when generated naming is turned off', () => {
        const svc = makeService({ecompGeneratedNaming: false, namingPolicy: 'some-policy'});
        const {comp} = createComp({component: svc});
        comp.ngOnInit();
        comp.form.get('ecompGeneratedNaming').setValue(false);
        comp.onEcompGeneratedNamingChange();
        expect(comp.component.namingPolicy).toBe('');
        expect(comp.form.get('namingPolicy').value).toBe('');
    });

    it('keeps namingPolicy when generated naming is on', () => {
        const svc = makeService({ecompGeneratedNaming: true, namingPolicy: 'keep-me'});
        const {comp} = createComp({component: svc});
        comp.ngOnInit();
        comp.form.get('ecompGeneratedNaming').setValue(true);
        comp.onEcompGeneratedNamingChange();
        expect(comp.component.namingPolicy).toBe('keep-me');
    });
});
