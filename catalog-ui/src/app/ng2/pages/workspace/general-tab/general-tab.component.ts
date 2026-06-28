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
import {ChangeDetectionStrategy, ChangeDetectorRef, Component as NgComponent, Inject, OnDestroy, OnInit} from '@angular/core';
import {FormGroup, Validators} from '@angular/forms';
import {Subject} from 'rxjs/Subject';
import 'rxjs/add/operator/takeUntil';
import * as _ from 'lodash';

import {CacheService} from 'app/services-ng2';
import {EventListenerService} from 'app/services';
import {EVENTS, WorkspaceMode, ComponentState, ComponentType, Role, PREVIOUS_CSAR_COMPONENT, instantiationType, DEFAULT_MODEL_NAME, DEFAULT_ICON, CATEGORY_SERVICE_METADATA_KEYS} from 'app/utils/constants';
import {WorkspaceService} from '../workspace.service';
import {GeneralFormService, ValidationPatterns} from './general-form.service';
import {ComponentMetadataService} from './component-metadata.service';

@NgComponent({
    selector: 'general-tab',
    templateUrl: './general-tab.component.html',
    styleUrls: ['./general-tab.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class GeneralTabComponent implements OnInit, OnDestroy {

    form: FormGroup;
    component: any;
    originComponent: any;
    mode: WorkspaceMode;
    private role: string;
    private destroy$ = new Subject<void>();

    // Option-list fields populated by the init* methods (bound by the template)
    instantiationTypes: string[] = [];
    models: string[] = [];
    defaultModelOption: string = DEFAULT_MODEL_NAME;
    showDefaultModelOption: boolean = true;
    isModelRequired: boolean = false;
    baseTypes: string[] = [];
    baseTypeVersions: any[] = [];
    showBaseTypeVersions: boolean = false;
    isBaseTypeRequired: boolean = false;
    environmentContextObj: any = null;
    categories: any[] = [];
    isHiddenCategorySelected: boolean = false;

    // Service Role / Function dropdown state (ported from the old GeneralViewModel). The dropdown
    // lists getMetadataKeyValidValues('Service Role'|'Service Function') + an "Others" sentinel;
    // picking "Others" reveals a free-text <input> bound to component.serviceRole/serviceFunction.
    roleOption: string = null;
    functionOption: string = null;
    othersRoleFlag: boolean = false;
    othersFlag: boolean = false;

    // AngularJS services resolved lazily in ngOnInit
    private $state: any;
    private $stateParams: any;
    private $filter: any;
    private componentFactory: any;
    private notification: any;
    private importVSPService: any;
    private onBoardingService: any;
    private modelService: any;
    private elementService: any;
    private sdcMenu: any;
    private modalsHandler: any;

    constructor(
        private generalFormService: GeneralFormService,
        private metadataService: ComponentMetadataService,
        private workspaceService: WorkspaceService,
        private cacheService: CacheService,
        private eventListenerService: EventListenerService,
        private cdr: ChangeDetectorRef,
        @Inject('$injector') private $injector: any
    ) {}

    ngOnInit(): void {
        this.resolveNg1Services();
        this.component = this.workspaceService.component;
        if (!this.component) { return; }
        this.originComponent = this.componentFactory.createComponent(this.component);
        // Strip the component-name "special tag" from the editable tag list on load, mirroring the
        // old GeneralViewModel (general-view-model.ts:266 `tags = _.without(tags, name)`). The name is
        // persisted in component.tags by the BE (Component.handleTags pushes it on every save), but in
        // the UI it is shown as a separate NON-deletable special chip bound to the LIVE name — never as
        // an editable chip. Without this strip, the persisted name stays in the array and, after a
        // rename, the stale OLD name no longer matches the displayedTags name-filter and leaks as a
        // deletable chip (CI: Service.updateService tags assertion). save()/updateComponent re-adds the
        // current name via handleTags(), so the BE round-trip is unaffected.
        if (this.component.tags && this.component.name) {
            this.component.tags = _.without(this.component.tags, this.component.name);
        }
        const user = this.cacheService.get('user');
        this.role = user ? user.role : '';
        this.mode = this.initMode(user);
        // Pass isResource so vendorName/vendorRelease are required only for Resources — a Service
        // never renders those fields (template *ngIf), so requiring them would leave its form
        // permanently invalid and block the certify/lifecycle-save gate.
        const isResourceComponent = !!(this.component.isResource && this.component.isResource());
        const patterns = this.readPatterns();
        this.tagPattern = patterns.tag;
        this.form = this.generalFormService.buildForm(patterns, isResourceComponent);
        this.patchFormFromComponent();
        // Old GeneralViewModel.initScope auto-populated contactId from the logged-in user in CREATE mode
        // (general-view-model.ts:381-385). The VFC-import Selenium flow never types a contactId, so without
        // this the create POST fails 400 SVC4049 "Missing contact".
        if (this.isCreateMode()) {
            const createUser = this.cacheService.get('user');
            if (createUser && createUser.userId) {
                this.component.contactId = createUser.userId;
                if (this.originComponent) { this.originComponent.contactId = createUser.userId; }
                this.form.get('contactId').setValue(createUser.userId);
            }
        }
        this.registerLifecycleSaveHandler();
        this.wireFormToWorkspace();
        // Populate dropdown option-lists (ported from old AngularJS controller, Tasks 8b + 8c)
        // Categories must be loaded first — initBaseTypes reads component.categories
        this.initCategories();
        // Re-derive the structured component.categories for an existing component that already has
        // a selection — MUST run after initCategories() (which populates this.categories), so the
        // full category object can be found. (For new creates the user's change event fires
        // onCategoryChange; checkout-edits need the array rebuilt on load for the BE.)
        if (this.component.selectedCategory) {
            this.onCategoryChange();
        }
        this.detectChangesSafe();
        this.initEnvironmentContext();
        this.initInstantiationTypes();
        this.initModel();
        this.initBaseTypes();
        // Resolve the Service Role / Function dropdown selection from the loaded values — must run
        // after the category (and thus its metadataKeys/validValues) is resolved above. Mirrors the
        // old GeneralViewModel.initScope which called setFunctionRole() once, separately from the
        // user-driven onCategoryChange (so loaded values are never wiped on load — §P).
        this.setFunctionRole();
        // Old GeneralViewModel.initScope auto-saved when navigated with a new VSP version
        // (componentCsar) outside CREATE mode (general-view-model.ts:340-343): the checkout picked up
        // a new onboarding package and persisted it immediately.
        if (this.$stateParams.componentCsar && !this.isCreateMode()) {
            if (this.$state.current.data) { this.$state.current.data.unsavedChanges = true; }
            this.save();
        }
        this.detectChangesSafe();
    }

    ngOnDestroy(): void {
        this.eventListenerService.unRegisterObserver(EVENTS.ON_LIFECYCLE_CHANGE_WITH_SAVE);
        this.eventListenerService.unRegisterObserver(EVENTS.ON_LIFECYCLE_CHANGE);
        this.destroy$.next();
        this.destroy$.complete();
    }

    private resolveNg1Services(): void {
        this.$state = this.$injector.get('$state');
        this.$stateParams = this.$injector.get('$stateParams');
        this.$filter = this.$injector.get('$filter');
        this.componentFactory = this.$injector.get('ComponentFactory');
        this.notification = this.$injector.get('Notification');
        this.importVSPService = this.$injector.get('ImportVSPService');
        this.onBoardingService = this.$injector.get('OnboardingService');
        this.modelService = this.$injector.get('ModelService');
        this.elementService = this.$injector.get('ElementService');
        this.sdcMenu = this.$injector.get('sdcMenu');
        this.modalsHandler = this.$injector.get('ModalsHandler');
    }

    // ---- Data-init methods ported from the old AngularJS GeneralViewModel ----

    /** Ported from GeneralViewModel.initCategories (lines 494-511). Reads categories from CacheService.
     *  Categories are sorted by name to match the old template's `orderBy:['name']` — see sortByName(). */
    private initCategories(): void {
        if (this.component.componentType === ComponentType.RESOURCE) {
            this.categories = this.sortByName(this.cacheService.get('resourceCategories') || []);
        }
        if (this.component.componentType === ComponentType.SERVICE) {
            this.categories = this.sortByName(this.cacheService.get('serviceCategories') || []);
            // Remove categories not applicable for External API
            if (this.isCreateMode() || (ComponentState.NOT_CERTIFIED_CHECKOUT === this.component.lifecycleState)) {
                this.categories = this.filteredCategories();
                this.isHiddenCategorySelected = this.isHiddenCategory(this.component.selectedCategory);
            }
        }
    }

    /**
     * Return a NEW array of categories sorted by name, mirroring the old template's
     * `data-ng-repeat="mainCategory in categories | orderBy:['name']"` (general-view.html:111,119).
     * The migrated template renders categories in array order, and the Selenium category pickers use
     * Select.selectByVisibleText(subCategoryName) — which selects the FIRST <option> with that text.
     * Several main categories share a subcategory name (e.g. "Database" lives under Application L4+,
     * DCAE Component AND Generic); without the alphabetical sort the first match is environment-
     * dependent, so a VFC imported under the ETSI model exported main category "Generic" instead of
     * "Application L4+" (CI: ImportVfcUiTest.checkEtsiMetadata expected "Application L4+"). Sorting a
     * COPY avoids mutating the shared CacheService array.
     */
    private sortByName(categories: any[]): any[] {
        return (categories || []).slice().sort((a: any, b: any) =>
            (a && a.name ? a.name : '').localeCompare(b && b.name ? b.name : ''));
    }

    /** Ported from GeneralViewModel.isHiddenCategory (lines 460-469). */
    private isHiddenCategory(category: string): boolean {
        if (!this.sdcMenu || !this.sdcMenu.component_workspace_menu_option) { return false; }
        const items: any[] = this.sdcMenu.component_workspace_menu_option[this.component.getComponentSubType()];
        if (!items) { return false; }
        for (let i = 0; i < items.length; ++i) {
            if (items[i].hiddenCategories && items[i].hiddenCategories.indexOf(category) > -1) { return true; }
        }
        return false;
    }

    /** Ported from GeneralViewModel.filteredCategories (lines 471-490). Service External-API filtering. */
    private filteredCategories(): any[] {
        const temp: any[] = [];
        this.categories.forEach((category) => {
            if (!this.isHiddenCategory(category.name) && this.isCreateMode()) {
                temp.push(category);
            } else if ((ComponentState.NOT_CERTIFIED_CHECKOUT === this.component.lifecycleState)
                       && !this.isHiddenCategory(this.component.selectedCategory) && !this.isHiddenCategory(category.name)) {
                temp.push(category);
            } else if ((ComponentState.NOT_CERTIFIED_CHECKOUT === this.component.lifecycleState)
                       && this.isHiddenCategory(this.component.selectedCategory)) {
                temp.push(category);
            }
        });
        return temp;
    }

    /** Ported from GeneralViewModel.isCreateModeAvailable (line 249). */
    private isCreateModeAvailable(verifyObj: string): boolean {
        const isCheckout = ComponentState.NOT_CERTIFIED_CHECKOUT === this.component.lifecycleState;
        return this.isCreateMode() || (isCheckout && !verifyObj);
    }

    /** Ported from GeneralViewModel.initInstantiationTypes (lines 512-526). Service only. */
    private initInstantiationTypes(): void {
        if (this.component.componentType !== ComponentType.SERVICE) { return; }
        this.instantiationTypes = [];
        this.instantiationTypes.push(instantiationType.A_LA_CARTE);
        this.instantiationTypes.push(instantiationType.MACRO);
        const instType: string = this.component.instantiationType;
        if (instType === '') {
            this.instantiationTypes.push('');
        } else if (this.isCreateModeAvailable(instType)) {
            this.component.instantiationType = instantiationType.A_LA_CARTE;
            // Reflect the applied default into the form control. The old template bound
            // ng-model="component.instantiationType", so setting the component updated the
            // <select> for free; with formControlName the control stays at its '' default and
            // would otherwise clobber this back to '' on the next syncFormToComponent.
            this.patchControl('instantiationType', this.component.instantiationType);
        }
        this.detectChangesSafe();
    }

    /** Ported from GeneralViewModel.initModel (lines 552-598). Service + all component types. */
    private initModel(): void {
        this.isModelRequired = false;
        this.models = [];
        this.defaultModelOption = DEFAULT_MODEL_NAME;
        this.showDefaultModelOption = true;
        this.applyModelRequiredValidator();

        // filterCategoriesByModel — now that initCategories() populates this.categories,
        // apply model-based filtering here (ported from GeneralViewModel, Task 8c).
        this.filterCategoriesByModel(this.component.model || null);

        if (this.isCreateMode() && this.isVspImport()) {
            const modelOptions = this.component.componentMetadata && this.component.componentMetadata.models;
            if (modelOptions) {
                this.isModelRequired = true;
                this.applyModelRequiredValidator();
                if (modelOptions.length === 1) {
                    this.models = modelOptions;
                    this.component.model = modelOptions[0];
                    this.patchControl('model', this.component.model);
                    this.showDefaultModelOption = false;
                } else {
                    this.models = modelOptions.slice().sort();
                    this.defaultModelOption = 'Select';
                }
            }
            this.detectChangesSafe();
            return;
        }

        if (!this.isCreateMode() && this.isVspImport()) {
            if (this.modelService) {
                this.modelService.getModels().takeUntil(this.destroy$).subscribe((modelsFound: any[]) => {
                    (modelsFound || []).sort().forEach((model: any) => {
                        if (this.component.model !== undefined) {
                            if (model.modelType === 'NORMATIVE_EXTENSION') {
                                if (this.component.model === model.name) {
                                    this.component.model = model.derivedFrom;
                                }
                                this.models.push(model.derivedFrom);
                            } else {
                                this.models.push(model.name);
                            }
                        }
                    });
                    this.detectChangesSafe();
                });
            }
            return;
        }

        // Default (normative) path
        if (this.modelService) {
            this.modelService.getModelsOfType('normative').takeUntil(this.destroy$).subscribe((modelsFound: any[]) => {
                (modelsFound || []).sort().forEach((model: any) => {
                    this.models.push(model.name);
                });
                this.detectChangesSafe();
            });
        }
    }

    /** Ported from GeneralViewModel.initBaseTypes (lines 528-550). Service + categories. */
    private initBaseTypes(): void {
        if (this.component.componentType !== ComponentType.SERVICE) { return; }
        if (!this.component || !this.component.categories || !this.component.categories[0]) { return; }
        if (!this.component.derivedFromGenericType) {
            this.component.derivedFromGenericVersion = undefined;
            this.showBaseTypeVersions = false;
            return;
        }
        const modelName = this.component.model ? this.component.model : null;
        const categoryName = this.component.categories[0].name;
        if (this.elementService) {
            this.elementService.getCategoryBaseTypes(categoryName, modelName)
                .takeUntil(this.destroy$)
                .subscribe((data: any) => {
                    this.baseTypes = [];
                    this.baseTypeVersions = [];
                    this.isBaseTypeRequired = data.required;
                    (data.baseTypes || []).forEach((baseType: any) => {
                        this.baseTypes.push(baseType.toscaResourceName);
                        if (baseType.toscaResourceName === this.component.derivedFromGenericType) {
                            baseType.versions.slice().reverse().forEach((v: any) => this.baseTypeVersions.push(v));
                        }
                    });
                    this.showBaseTypeVersions = true;
                    this.detectChangesSafe();
                });
        }
    }

    /** Ported from GeneralViewModel.initEnvironmentContext (lines 609-618). Service only. */
    private initEnvironmentContext(): void {
        if (this.component.componentType !== ComponentType.SERVICE) { return; }
        const uiConfig = this.cacheService.get('UIConfiguration');
        if (!uiConfig) { return; }
        this.environmentContextObj = uiConfig.environmentContext;
        const envCtx: string = this.component.environmentContext;
        if (this.isCreateModeAvailable(envCtx)) {
            this.component.environmentContext = this.environmentContextObj.defaultValue;
            // Reflect the applied default into the form control. Without this the control stays
            // '' (its build default; patchFormFromComponent ran before this init while
            // component.environmentContext was still undefined), and the next
            // syncFormToComponent overwrites component.environmentContext back to '' — the create
            // POST then sends an empty environment context and the backend rejects it with
            // "Invalid Environment context" (CI: EtsiModelUiTests.createServiceWithModel).
            this.patchControl('environmentContext', this.component.environmentContext);
        }
        this.detectChangesSafe();
    }

    /** Filter already-loaded categories by model name (ported from GeneralViewModel, Task 8c). */
    private filterCategoriesByModel(modelName: string | null): void {
        if (!this.categories || this.categories.length === 0) { return; }
        this.categories = this.categories.filter((c: any) =>
            !modelName
                ? (!c.models || c.models.indexOf(DEFAULT_MODEL_NAME) !== -1)
                : (c.models !== null && c.models !== undefined && c.models.indexOf(modelName) !== -1)
        );
    }

    private readPatterns(): ValidationPatterns {
        return {
            name: this.$injector.get('ComponentNameValidationPattern'),
            contactId: this.$injector.get('ContactIdValidationPattern'),
            tag: this.$injector.get('TagValidationPattern'),
            vendorName: this.$injector.get('VendorNameValidationPattern'),
            vendorRelease: this.$injector.get('VendorReleaseValidationPattern'),
            vendorModelNumber: this.$injector.get('VendorModelNumberValidationPattern'),
            comment: this.$injector.get('CommentValidationPattern')
        };
    }

    private initMode(user: any): WorkspaceMode {
        if (!this.$stateParams.id) { return WorkspaceMode.CREATE; }
        if (this.component.lifecycleState === ComponentState.NOT_CERTIFIED_CHECKOUT &&
            this.component.lastUpdaterUserId === (user && user.userId) &&
            (this.component.isService() || this.component.isResource()) && this.role === Role.DESIGNER) {
            return WorkspaceMode.EDIT;
        }
        return WorkspaceMode.VIEW;
    }

    private patchFormFromComponent(): void {
        this.form.patchValue({
            name: this.component.name || '',
            description: this.component.description || '',
            vendorName: this.component.vendorName || '',
            vendorRelease: this.component.vendorRelease || '',
            resourceVendorModelNumber: this.component.resourceVendorModelNumber || '',
            contactId: this.component.contactId || '',
            tags: this.component.tags || [],
            category: this.component.selectedCategory || '',
            // Service-specific controls — must be patched on load so EDIT/VIEW shows the persisted
            // values (the old template bound these via ng-model="component.*", so load populated them
            // for free; with formControlName the value lives only in the FormGroup until patched).
            model: this.component.model || '',
            instantiationType: this.component.instantiationType || '',
            namingPolicy: this.component.namingPolicy || '',
            ecompGeneratedNaming: this.component.ecompGeneratedNaming !== undefined ? this.component.ecompGeneratedNaming : true,
            environmentContext: this.component.environmentContext || '',
            serviceType: this.component.serviceType || '',
            serviceFunction: this.component.serviceFunction || '',
            serviceRole: this.component.serviceRole || ''
        }, {emitEvent: false});
    }

    /**
     * Called whenever the category <select> changes (via (change) binding in the template).
     * Replicates the old GeneralViewModel.onCategoryChange (general-view-model.ts:802-863):
     * sets component.selectedCategory from the form, then builds the structured component.categories
     * array the backend requires — using the full category objects from the loaded option-list.
     */
    onCategoryChange(): void {
        this.component.selectedCategory = this.form.get('category').value;
        if (this.component.selectedCategory) {
            // Re-derive role/function on category change (the new category's validValues differ);
            // mirrors the old onCategoryChange resetting these before re-seeding (general-view-model.ts:810-816).
            this.roleOption = null;
            this.functionOption = null;
            this.othersFlag = false;
            this.othersRoleFlag = false;
            this.component.categories = this.buildCategoriesFromSelected();
            this.component.icon = DEFAULT_ICON;
            // Seed category-specific metadata from the selected category's (and subcategory's)
            // metadataKeys, ported from the old GeneralViewModel.onCategoryChange
            // (general-view-model.ts:820-845). This is what surfaces fields like "ETSI Version" for
            // the ETSI NFV category: each metadataKey becomes a categorySpecificMetadata entry (its
            // defaultValue, or '') that the template renders as a <select>/<input>. Without this the
            // ETSI Version <select data-tests-id="ETSI Version"> never appears and
            // EtsiNetworkServiceUiTests times out in createService waiting for it.
            this.initCategorySpecificMetadata();
            // Load the base types for the newly-selected category and (in CREATE mode) auto-select
            // the required/default base type — ported from the old onCategoryChange, which called
            // loadBaseTypes (general-view-model.ts:846-860, 1004-1035). Without this a Service create
            // on a base-type-required category never gets a base type assigned.
            this.loadBaseTypesForSelectedCategory();
        } else {
            this.component.categories = undefined;
            this.clearBaseTypes();
        }
        this.detectChangesSafe();
    }

    /**
     * Service-only: fetch the base types for the currently-selected category and, in CREATE mode,
     * auto-select the required/default base type (and its newest version), mirroring the old
     * GeneralViewModel.loadBaseTypes (general-view-model.ts:1004-1035). In EDIT mode it only refreshes
     * the available lists (the user's existing derivedFromGenericType is preserved).
     */
    private loadBaseTypesForSelectedCategory(): void {
        if (this.component.componentType !== ComponentType.SERVICE) { return; }
        if (!this.component.categories || !this.component.categories[0] || !this.elementService) { return; }
        const modelName = this.component.model ? this.component.model : null;
        const categoryName = this.component.categories[0].name;
        this.elementService.getCategoryBaseTypes(categoryName, modelName)
            .takeUntil(this.destroy$)
            .subscribe((data: any) => {
                if (this.isCreateMode()) {
                    this.loadBaseTypes(data);
                } else {
                    // EDIT: just refresh the dropdown lists; keep the existing selection.
                    this.isBaseTypeRequired = data.required;
                    this.baseTypes = [];
                    (data.baseTypes || []).forEach((bt: any) => this.baseTypes.push(bt.toscaResourceName));
                }
                this.detectChangesSafe();
            });
    }

    /**
     * Populate base types from a getCategoryBaseTypes response and auto-select the default/required
     * base type + its newest version. Ported verbatim in behavior from GeneralViewModel.loadBaseTypes
     * (general-view-model.ts:1004-1035).
     */
    private loadBaseTypes(data: any): void {
        this.isBaseTypeRequired = data.required;
        this.baseTypes = [];
        this.baseTypeVersions = [];
        const defaultBaseType = data.defaultBaseType;
        (data.baseTypes || []).forEach((bt: any) => this.baseTypes.push(bt.toscaResourceName));
        if (this.isBaseTypeRequired || defaultBaseType != null) {
            let baseType = data.baseTypes[0];
            if (defaultBaseType != null) {
                data.baseTypes.forEach((bt: any) => { if (bt.toscaResourceName === defaultBaseType) { baseType = bt; } });
            }
            if (this.component.derivedFromGenericType) {
                data.baseTypes.forEach((bt: any) => { if (bt.toscaResourceName === this.component.derivedFromGenericType) { baseType = bt; } });
            }
            if (baseType) {
                baseType.versions.slice().reverse().forEach((v: any) => this.baseTypeVersions.push(v));
                this.component.derivedFromGenericType = baseType.toscaResourceName;
                this.component.derivedFromGenericVersion = this.baseTypeVersions[0];
                this.showBaseTypeVersions = true;
            }
            return;
        }
        this.component.derivedFromGenericType = undefined;
        this.component.derivedFromGenericVersion = undefined;
        this.showBaseTypeVersions = false;
    }

    /** Reset all base-type state (ported from GeneralViewModel.clearBaseTypes, lines 1037-1044). */
    private clearBaseTypes(): void {
        this.isBaseTypeRequired = false;
        this.baseTypes = [];
        this.baseTypeVersions = [];
        this.component.derivedFromGenericType = undefined;
        this.component.derivedFromGenericVersion = undefined;
        this.showBaseTypeVersions = false;
    }

    /**
     * Populate component.categorySpecificMetadata from the selected category + subcategory
     * metadataKeys (ported from GeneralViewModel.onCategoryChange, general-view-model.ts:820-845).
     * Only fills keys that are absent, using the key's defaultValue (or '') — never overwrites a
     * value already loaded for an existing component.
     */
    private initCategorySpecificMetadata(): void {
        if (!this.component.categories || !this.component.categories[0]) { return; }
        if (!this.component.categorySpecificMetadata) { this.component.categorySpecificMetadata = {}; }
        const seed = (metadataKeys: any[]) => {
            (metadataKeys || []).forEach((metadataKey: any) => {
                if (!this.component.categorySpecificMetadata[metadataKey.name]) {
                    this.component.categorySpecificMetadata[metadataKey.name] =
                        metadataKey.defaultValue ? metadataKey.defaultValue : '';
                }
            });
        };
        seed(this.component.categories[0].metadataKeys);
        if (this.component.categories[0].subcategories && this.component.categories[0].subcategories[0]) {
            seed(this.component.categories[0].subcategories[0].metadataKeys);
        }
    }

    /** Resolve a metadata-key definition by name from the selected category/subcategory.
     *  Ported from GeneralViewModel.getMetadataKey/getSubcategoryMetadataKey/getCategoryMetadataKey
     *  (general-view-model.ts:1050-1073) — subcategory takes precedence over main category. */
    private getMetadataKey(key: string): any {
        const categories = this.component && this.component.categories;
        if (!categories || !categories[0]) { return null; }
        const sub = categories[0].subcategories && categories[0].subcategories[0];
        if (sub && sub.metadataKeys) {
            const found = sub.metadataKeys.find((mk: any) => mk.name === key);
            if (found) { return found; }
        }
        if (categories[0].metadataKeys) {
            const found = categories[0].metadataKeys.find((mk: any) => mk.name === key);
            if (found) { return found; }
        }
        return null;
    }

    /** Whether a metadata key belongs to the selected category at all (template *ngIf guard).
     *  Ported from GeneralViewModel.isMetadataKeyForComponentCategory (line 963). */
    isMetadataKeyForComponentCategory(key: string): boolean {
        return this.getMetadataKey(key) != null;
    }

    /** Whether a metadata key is one of the dedicated Service metadata fields (Naming Policy,
     *  Service Type, Service Function, Service Role) — those have their own form controls and must
     *  NOT be rendered again as a generic category-metadata field. Ported from
     *  GeneralViewModel.isCategoryServiceMetadataKey/isServiceMetadataKey (lines 967-968, 1075-1077). */
    isCategoryServiceMetadataKey(key: string): boolean {
        return CATEGORY_SERVICE_METADATA_KEYS.indexOf(key) > -1;
    }

    /** Valid values for a metadata-key <select>; empty array → render a free-text <input> instead.
     *  Ported from GeneralViewModel.getMetadataKeyValidValues (lines 943-953). For the two free-text
     *  -capable keys (Service Function / Service Role) the old code appended an "Others" sentinel that
     *  reveals a free-text <input> — restored here so those dropdowns are populated and offer Others. */
    getMetadataKeyValidValues(key: string): string[] {
        const metadataKey = this.getMetadataKey(key);
        if (!metadataKey || !metadataKey.validValues) { return []; }
        if (key === 'Service Function' || key === 'Service Role') {
            return metadataKey.validValues.concat('Others');
        }
        return metadataKey.validValues;
    }

    /** Whether the metadata key is mandatory (drives the required marker). Ported from
     *  GeneralViewModel.isMetadataKeyMandatory (lines 938-941). */
    isMetadataKeyMandatory(key: string): boolean {
        const metadataKey = this.getMetadataKey(key);
        return !!(metadataKey && metadataKey.mandatory);
    }

    /** Display label for a metadata key. Ported from GeneralViewModel.getMetadataDisplayName
     *  (lines 955-961). */
    getMetadataDisplayName(key: string): string {
        const metadataKey = this.getMetadataKey(key);
        if (metadataKey) { return metadataKey.displayName ? metadataKey.displayName : metadataKey.name; }
        return '';
    }

    /**
     * Replicates the old GeneralViewModel.convertCategoryStringToOneArray (general-view-model.ts:427-450).
     * Splits selectedCategory on '_#_', finds the FULL main-category object in this.categories
     * (preserving uniqueId, normalizedName, etc.), deep-clones it, and attaches a one-element
     * subcategory slice when a sub-category is present.
     */
    private buildCategoriesFromSelected(): any[] {
        const tmp = this.component.selectedCategory.split('_#_');
        const mainCategoryName = tmp[0];
        const subCategoryName = tmp[1];

        const selectedMainCategory = this.categories.find((item: any) => item.name === mainCategoryName);
        if (!selectedMainCategory) { return []; }

        const mainCategoryClone = _.cloneDeep(selectedMainCategory);
        if (subCategoryName && selectedMainCategory.subcategories) {
            const selectedSubcategory = selectedMainCategory.subcategories.find(
                (item: any) => item.name === subCategoryName
            );
            if (selectedSubcategory) {
                mainCategoryClone.subcategories = [_.cloneDeep(selectedSubcategory)];
            }
        }
        return [mainCategoryClone];
    }

    /**
     * Service base-type (Substitution Node Type) select changed. Writes the selection onto the
     * component and refreshes the version list for the chosen base type (ported from the old
     * GeneralViewModel.onBaseTypeChange, general-view-model.ts:875-895). Without this the select
     * was display-only and a designer's base-type choice was never persisted.
     */
    onBaseTypeChange(value: string): void {
        this.component.derivedFromGenericType = value;
        if (!value) {
            this.component.derivedFromGenericVersion = undefined;
            this.showBaseTypeVersions = false;
            this.detectChangesSafe();
            return;
        }
        if (!this.component.categories || !this.component.categories[0]) { return; }
        const modelName = this.component.model ? this.component.model : null;
        const categoryName = this.component.categories[0].name;
        if (this.elementService) {
            this.elementService.getCategoryBaseTypes(categoryName, modelName)
                .takeUntil(this.destroy$)
                .subscribe((data: any) => {
                    this.baseTypeVersions = [];
                    (data.baseTypes || []).forEach((baseType: any) => {
                        if (baseType.toscaResourceName === this.component.derivedFromGenericType) {
                            baseType.versions.slice().reverse().forEach((v: any) => this.baseTypeVersions.push(v));
                            this.component.derivedFromGenericVersion = baseType.versions[0];
                        }
                    });
                    this.showBaseTypeVersions = true;
                    this.detectChangesSafe();
                });
        }
    }

    /**
     * Service model select changed. Clears the now-invalid category selection and re-filters the
     * category + base-type lists by the chosen model (ported from GeneralViewModel.onModelChange,
     * general-view-model.ts:897-906). syncFormToComponent already copies the model value; this adds
     * the model-driven re-filtering the old handler performed.
     */
    onModelChange(): void {
        // Coerce ''->null: the blank "no model" option must not reach the BE as an empty string
        // (it would resolve an empty datatype scope — see the note in syncFormToComponent).
        const selected = this.form.get('model').value;
        this.component.model = selected ? selected : null;
        if (this.component.componentType === ComponentType.SERVICE) {
            const modelName = this.component.model ? this.component.model : null;
            this.component.categories = undefined;
            this.component.selectedCategory = undefined;
            this.form.get('category').setValue('', {emitEvent: false});
            // Re-load + re-filter categories for the new model, then refresh base types.
            this.initCategories();
            this.filterCategoriesByModel(modelName);
            this.initBaseTypes();
            this.detectChangesSafe();
        }
    }

    /** Category-specific metadata field edited — write back into component.categorySpecificMetadata. */
    onCategoryMetadataChange(key: string, value: string): void {
        if (!this.component.categorySpecificMetadata) { this.component.categorySpecificMetadata = {}; }
        this.component.categorySpecificMetadata[key] = value;
    }

    /**
     * Service Role dropdown changed. "Others" reveals the free-text input and clears the model value
     * (the user types a custom role); a real option writes straight onto the component. Ported from
     * GeneralViewModel.setServiceRole (general-view-model.ts:925-934).
     */
    setServiceRole(option: string): void {
        this.roleOption = option;
        if (option === 'Others') {
            this.othersRoleFlag = true;
            this.component.serviceRole = '';
        } else {
            this.othersRoleFlag = false;
            this.component.serviceRole = option;
        }
        // Keep the reactive control in sync — syncFormToComponent copies serviceRole FROM the form
        // control, so a dropdown selection that only wrote component.serviceRole would be clobbered
        // back on the next sync (§P). emitEvent:false avoids re-triggering the sync cycle.
        this.patchControl('serviceRole', this.component.serviceRole);
        this.detectChangesSafe();
    }

    /**
     * Service Function dropdown changed. Mirrors setServiceRole. Ported from
     * GeneralViewModel.setServiceFunction (general-view-model.ts:914-923).
     */
    setServiceFunction(option: string): void {
        this.functionOption = option;
        if (option === 'Others') {
            this.othersFlag = true;
            this.component.serviceFunction = '';
        } else {
            this.othersFlag = false;
            this.component.serviceFunction = option;
        }
        this.patchControl('serviceFunction', this.component.serviceFunction);
        this.detectChangesSafe();
    }

    /**
     * Resolve the initial roleOption/functionOption dropdown selection from the loaded service:
     * if the persisted serviceRole/serviceFunction is one of the category's validValues, pre-select
     * it; otherwise select "Others" and reveal the free-text input. Ported from
     * GeneralViewModel.setFunctionRole (general-view-model.ts:398-424).
     */
    private setFunctionRole(): void {
        if (this.component.componentType !== ComponentType.SERVICE) { return; }
        if (!this.component.serviceFunction && this.component.componentMetadata) {
            this.component.serviceFunction = this.component.componentMetadata.serviceFunction;
        }
        if (!this.component.serviceRole && this.component.componentMetadata) {
            this.component.serviceRole = this.component.componentMetadata.serviceRole;
        }
        if (this.component.serviceFunction) {
            const functionList = this.getMetadataKeyValidValues('Service Function');
            if (functionList.find((value) => value === this.component.serviceFunction) !== undefined) {
                this.functionOption = this.component.serviceFunction;
            } else {
                this.functionOption = 'Others';
                this.othersFlag = true;
            }
        }
        if (this.component.serviceRole) {
            const roleList = this.getMetadataKeyValidValues('Service Role');
            if (roleList.find((value) => value === this.component.serviceRole) !== undefined) {
                this.roleOption = this.component.serviceRole;
            } else {
                this.roleOption = 'Others';
                this.othersRoleFlag = true;
            }
        }
    }

    /** Generated-naming select changed: clear the naming policy when it is turned off, mirroring the
     *  old GeneralViewModel.onEcompGeneratedNamingChange (general-view-model.ts:866-870). */
    onEcompGeneratedNamingChange(): void {
        if (!this.form.get('ecompGeneratedNaming').value) {
            this.component.namingPolicy = '';
            this.patchControl('namingPolicy', '');
        }
        this.detectChangesSafe();
    }

    // ---- Icon ----

    /** Whether the icon may be changed — non-certified, with a category, and (for Resources) a vendor.
     *  Ported from GeneralViewModel.possibleToUpdateIcon (general-view-model.ts:657-663). */
    possibleToUpdateIcon(): boolean {
        return !!(this.component.selectedCategory
            && (!this.component.isResource() || this.component.vendorName)
            && !this.isAlreadyCertified());
    }

    /** Open the change-icon modal; in EDIT mode a confirmed change marks the workspace dirty.
     *  Ported from GeneralViewModel.updateIcon (general-view-model.ts:647-655). */
    updateIcon(): Promise<void> {
        return Promise.resolve(this.modalsHandler.openUpdateIconModal(this.component)).then((isDirty: boolean) => {
            if (isDirty && !this.isCreateMode()) {
                if (this.$state.current.data) { this.$state.current.data.unsavedChanges = true; }
            }
            this.detectChangesSafe();
        }, () => { /* modal dismissed */ });
    }

    /** Build the "Main_#_Sub" unique category string. Delegates to ComponentMetadataService so the
     *  '_#_' separator lives in one place. Ported from GeneralViewModel.calculateUnique (line 373). */
    calculateUnique(mainCategory: string, subCategory: string): string {
        return this.metadataService.calculateUnique(mainCategory, subCategory);
    }

    /**
     * Asynchronously validate the component name against the backend for uniqueness and reflect the
     * result onto the name control (nameExist error). Ported from GeneralViewModel.validateName
     * (general-view-model.ts:665-727), simplified to the on-change path the reactive form needs.
     */
    validateName(): Promise<void> {
        const nameCtrl = this.form.get('name');
        const name = nameCtrl.value;
        if (!name || name === '') {
            this.clearNameExistError(nameCtrl);
            return Promise.resolve();
        }
        // Don't re-validate when the name is unchanged from the loaded component.
        if (this.originComponent && this.originComponent.name
            && name.toUpperCase() === this.originComponent.name.toUpperCase()) {
            this.clearNameExistError(nameCtrl);
            return Promise.resolve();
        }
        // CSAR resources keep the imported name — skip the uniqueness check (old code guarded the same).
        if (this.component.componentType === ComponentType.RESOURCE && this.isCsarComponent()) {
            return Promise.resolve();
        }
        let subtype = this.component.componentType === ComponentType.RESOURCE ? this.component.getComponentSubType() : undefined;
        if (subtype === 'SRVC') { subtype = 'VF'; }
        return Promise.resolve(this.component.validateName(name, subtype)).then((validation: any) => {
            if (validation && validation.isValid) {
                this.clearNameExistError(nameCtrl);
                // Reflect the new (valid) name into the workspace chrome (breadcrumb/menu title), which
                // the old controller did via updateComponentNameInBreadcrumbs. The shell exposes this
                // through containerActions; guard because the shim may not be present in every flow.
                const actions = this.workspaceService.containerActions;
                if (actions) {
                    actions.menuComponentTitle = name;
                    if (actions.cdr) { actions.cdr.detectChanges(); }
                }
            } else {
                this.setNameExistError(nameCtrl);
            }
            this.detectChangesSafe();
        }, () => { /* validation request failed — leave the control as-is */ });
    }

    private setNameExistError(ctrl: any): void {
        const errors = Object.assign({}, ctrl.errors || {}, {nameExist: true});
        ctrl.setErrors(errors);
    }

    private clearNameExistError(ctrl: any): void {
        if (ctrl.errors && ctrl.errors.nameExist) {
            const errors = Object.assign({}, ctrl.errors);
            delete errors.nameExist;
            ctrl.setErrors(Object.keys(errors).length ? errors : null);
        }
    }

    // ---- Tags widget ----

    newTag: string = '';
    private readonly maxTags = 20;
    private tagPattern: RegExp;

    get displayedTags(): string[] {
        const tags = (this.component && this.component.tags) || [];
        // Don't show the component-name special tag as a removable chip (mirrors old sdc-tags directive)
        return tags.filter((t: string) => t !== (this.component && this.component.name));
    }

    addTag(): void {
        const t = (this.newTag || '').trim();
        const tags = this.component.tags || (this.component.tags = []);
        // Mirror the old sdc-tags directive's pattern="validation.tagValidationPattern" — reject tags
        // with invalid characters instead of silently accepting them.
        if (t && this.tagPattern && !this.tagPattern.test(t)) { return; }
        if (t && tags.length < this.maxTags && tags.indexOf(t) === -1 && t !== this.component.name) {
            tags.push(t);
            this.form.get('tags').setValue(tags);
            this.form.get('tags').markAsDirty();
            this.newTag = '';
            if (this.$state.current.data) { this.$state.current.data.unsavedChanges = !this.isCreateMode(); }
            this.detectChangesSafe();
        }
    }

    deleteTag(tag: string): void {
        const tags = this.component.tags || [];
        const i = tags.indexOf(tag);
        if (i > -1) {
            tags.splice(i, 1);
            this.form.get('tags').setValue(tags);
            this.form.get('tags').markAsDirty();
            this.detectChangesSafe();
        }
    }

    // ---- Mode helpers ----

    isViewMode(): boolean { return this.mode === WorkspaceMode.VIEW; }
    isCreateMode(): boolean { return this.mode === WorkspaceMode.CREATE; }
    isEditMode(): boolean { return this.mode === WorkspaceMode.EDIT; }

    private isCsarComponent(): boolean {
        return !!(this.component && this.component.isCsarComponent && this.component.isCsarComponent());
    }

    private isAlreadyCertified(): boolean {
        return !!(this.component && this.component.isAlreadyCertified && this.component.isAlreadyCertified());
    }

    // ---- Per-field disable predicates (ported from general-view.html data-ng-disabled) ----
    // The reactive template must NOT blanket-disable on isViewMode() — the old template encoded
    // field-level business rules (create-only model, CSAR-immutable category/vendor, certified locks).
    // Collapsing them to isViewMode() let a user mutate fields the BE assumed fixed (e.g. the model
    // scope key — §Q). Each predicate below mirrors the exact old expression.

    /** model: locked outside CREATE (general-view.html:84 ng-disabled="!isCreateMode()"). */
    isModelDisabled(): boolean {
        return !this.isCreateMode();
    }

    /** category (general-view.html:106): certified OR (CSAR && already-categorised) OR hidden-selected. */
    isCategoryDisabled(): boolean {
        return this.isViewMode()
            || this.isAlreadyCertified()
            || (this.isCsarComponent() && !!this.component.selectedCategory && this.component.selectedCategory !== '')
            || this.isHiddenCategorySelected;
    }

    /** vendorName (general-view.html:308): certified OR (CSAR && already has a vendorName). */
    isVendorNameDisabled(): boolean {
        return this.isViewMode()
            || this.isAlreadyCertified()
            || (this.isCsarComponent() && !!this.component.vendorName && this.component.vendorName !== '');
    }

    /** vendorRelease (general-view.html:334): CSAR && already has a vendorRelease. */
    isVendorReleaseDisabled(): boolean {
        return this.isViewMode()
            || (this.isCsarComponent() && !!this.component.vendorRelease && this.component.vendorRelease !== '');
    }

    /** instantiation type (general-view.html:607 ng-disabled="component.isCsarComponent()"). */
    isInstantiationTypeDisabled(): boolean {
        return this.isViewMode() || this.isCsarComponent();
    }

    /** base type (general-view.html:624): CSAR OR not in an editable mode. */
    isBaseTypeDisabled(): boolean {
        return this.isCsarComponent() || !(this.isEditMode() || this.isCreateMode());
    }

    /** Angular 5-compatible substitute for the Angular 6.1+ `keyvalue` pipe.
     *  Returns the entries of categorySpecificMetadata as [{key, value}] pairs. */
    get categorySpecificMetadataEntries(): Array<{key: string, value: any}> {
        const meta = this.component && this.component.categorySpecificMetadata;
        if (!meta) { return []; }
        return Object.keys(meta).map(k => ({key: k, value: meta[k]}));
    }

    // Flush the reactive form into the working component. Because the form is now Angular
    // (not debounced AngularJS ng-model), the component is current synchronously — this is
    // why the shell's commitPendingFormValues() hack is no longer needed (removed in Task 9).
    private syncFormToComponent(): void {
        const v = this.form.getRawValue();
        Object.assign(this.component, {
            name: v.name, description: v.description, vendorName: v.vendorName,
            vendorRelease: v.vendorRelease, resourceVendorModelNumber: v.resourceVendorModelNumber,
            contactId: v.contactId, tags: v.tags, selectedCategory: v.category,
            // Service-specific controls — must be synced back so EDIT-mode changes are persisted
            // by updateComponent() (the old ng-model bound straight to component.*; formControlName
            // does not, so without copying these the edits are silently dropped on save).
            //
            // model MUST be coerced ''->null. The empty <option value=""> (the "no model"/default
            // SDC AID choice) yields '' from the reactive control, but the BE keys its datatype cache
            // by model name (ApplicationDataTypeCache.getDataTypeDefinitionMapByModel): containsKey("")
            // is false, so an empty-string model resolves an EMPTY datatype scope and the BE then
            // rejects even basic string/boolean properties with "Unsupported datatype found for
            // property". The old GeneralViewModel never wrote ''; every read site was
            // `component.model ? component.model : null`, and AngularJS maps the blank option to null.
            // Sending '' here regressed VF/Service create -> later property-add 400s in CI.
            model: v.model ? v.model : null, instantiationType: v.instantiationType, namingPolicy: v.namingPolicy,
            ecompGeneratedNaming: v.ecompGeneratedNaming, environmentContext: v.environmentContext,
            serviceType: v.serviceType, serviceFunction: v.serviceFunction, serviceRole: v.serviceRole
        });
    }

    private wireFormToWorkspace(): void {
        this.form.statusChanges.takeUntil(this.destroy$).subscribe(() => {
            this.workspaceService.isValidForm = this.form.valid;
            // Only report unsaved changes outside CREATE mode — in CREATE there is nothing to lose,
            // and reporting dirty here disables the shell's Create button (it is [disabled] on unsavedChanges),
            // so the create flow / loader never fires. Mirrors the create-guard already on the valueChanges handler
            // and the old GeneralViewModel, which never drove the shell's unsavedChanges in create mode.
            const dirty = this.form.dirty && !this.isCreateMode();
            this.eventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, dirty, undefined);
        });
        this.form.valueChanges.takeUntil(this.destroy$).subscribe(() => {
            this.syncFormToComponent();
            // Mirror the AngularJS $watch('editForm.$dirty') → setUnsavedChanges(true) logic.
            // Only track dirty in EDIT mode — in CREATE mode there is nothing to lose.
            if (this.form.dirty && !this.isCreateMode()) {
                if (this.$state.current.data) { this.$state.current.data.unsavedChanges = true; }
            }
        });
    }

    isVspImport(): boolean {
        return this.component && this.component.isResource && this.component.isResource() && !!this.component.csarUUID;
    }

    openOnBoardingModal(): void {
        const csarUUID = this.component.csarUUID;
        const csarVersion = this.component.csarVersion;
        this.importVSPService.openOnboardingModal(csarUUID, csarVersion)
            .takeUntil(this.destroy$)
            .subscribe((result: any) => {
                this.componentFactory.getComponentWithMetadataFromServer(result.type.toUpperCase(), result.previousComponent.uniqueId)
                    .then((component: any) => {
                        if (result.componentCsar && component.isResource()) {
                            this.cacheService.set(PREVIOUS_CSAR_COMPONENT, component);
                            component = this.componentFactory.updateComponentFromCsar(result.componentCsar, component);
                        }
                        this.workspaceService.setComponent(component);
                        this.component = component;
                        this.save();
                        this.detectChangesSafe();
                    });
            });
    }

    onImportFileChange(): void {
        if (this.$state.current.data) { this.$state.current.data.unsavedChanges = true; }
    }

    revert(): void {
        const restored = this.componentFactory.createComponent(this.originComponent);
        this.workspaceService.setComponent(restored);
        this.component = restored;
        this.form.reset({
            name: restored.name || '',
            description: restored.description || '',
            vendorName: restored.vendorName || '',
            vendorRelease: restored.vendorRelease || '',
            resourceVendorModelNumber: restored.resourceVendorModelNumber || '',
            contactId: restored.contactId || '',
            tags: restored.tags || [],
            category: restored.selectedCategory || '',
            model: restored.model || '',
            instantiationType: restored.instantiationType || '',
            namingPolicy: restored.namingPolicy || '',
            ecompGeneratedNaming: restored.ecompGeneratedNaming !== undefined ? restored.ecompGeneratedNaming : true,
            environmentContext: restored.environmentContext || '',
            serviceType: restored.serviceType || '',
            serviceFunction: restored.serviceFunction || '',
            serviceRole: restored.serviceRole || ''
        });
        this.form.markAsPristine();
        if (this.$state.current.data) { this.$state.current.data.unsavedChanges = false; }
        this.workspaceService.isValidForm = this.form.valid;
        this.detectChangesSafe();
    }

    // The real save (replaces the shim's no-op $scope.save that silently dropped edits).
    save(): Promise<void> {
        const actions = this.workspaceService.containerActions;
        this.syncFormToComponent();
        this.component.tags = _.without(this.component.tags, this.component.name);
        if (actions && actions.startProgress) { actions.startProgress('Updating Asset...'); }

        return new Promise<void>((resolve, reject) => {
            const finish = () => {
                if (actions && actions.stopProgress) { actions.stopProgress(); }
            };
            this.component.updateComponent().then(
                (updated: any) => {
                    finish();
                    updated.tags = _.without(updated.tags, updated.name);
                    this.workspaceService.setComponent(updated);
                    this.component = updated;
                    this.originComponent = this.componentFactory.createComponent(updated);
                    if (this.$state.current.data) { this.$state.current.data.unsavedChanges = false; }
                    this.notification.success({
                        message: this.$filter('translate')('IMPORT_VF_MESSAGE_CREATE_FINISHED_DESCRIPTION'),
                        title: this.$filter('translate')('IMPORT_VF_MESSAGE_CREATE_FINISHED_TITLE')
                    });
                    this.detectChangesSafe();
                    resolve();
                },
                (err: any) => {
                    finish();
                    this.eventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_SAVE_BUTTON_ERROR);
                    this.detectChangesSafe();
                    reject(err || new Error('updateComponent failed'));
                }
            );
        });
    }

    private registerLifecycleSaveHandler(): void {
        this.eventListenerService.registerObserverCallback(EVENTS.ON_LIFECYCLE_CHANGE_WITH_SAVE, (nextState: string) => {
            const actions = this.workspaceService.containerActions;
            const dirty = this.$state.current.data && this.$state.current.data.unsavedChanges;
            if (dirty && this.form.valid) {
                return this.save().then(
                    () => { if (actions) { actions.handleChangeLifecycleState(nextState); } },
                    () => { console.error('Save failed, unable to change lifecycle state to ' + nextState); }
                );
            } else if (!this.form.valid) {
                console.error('Form is not valid');
            } else if (actions) {
                actions.handleChangeLifecycleState(nextState);
            }
        });
    }

    /**
     * Set a form control's value WITHOUT emitting valueChanges, used by the init* methods to
     * reflect a default they just applied to the component (instantiationType, environmentContext,
     * single VSP model) into the reactive control. The old AngularJS template bound these via
     * ng-model="component.x", so assigning the component updated the <select> automatically; with
     * formControlName the control is decoupled and keeps its '' build-default, which the next
     * syncFormToComponent would then copy back over the component default. emitEvent:false avoids
     * re-triggering the valueChanges/syncFormToComponent cycle for a value we set programmatically.
     */
    private patchControl(name: string, value: any): void {
        const ctrl = this.form && this.form.get(name);
        if (ctrl) { ctrl.setValue(value, {emitEvent: false}); }
    }

    /**
     * Toggle Validators.required on the model control to mirror the old template's
     * data-ng-required="isModelRequired" (general-view.html:87). A VSP import with a fixed set of
     * models must pick one; a normal component may leave model empty (→ null). The model control
     * carries no other validators, so we can set/clear cleanly.
     */
    private applyModelRequiredValidator(): void {
        const ctrl = this.form && this.form.get('model');
        if (!ctrl) { return; }
        ctrl.setValidators(this.isModelRequired ? [Validators.required] : []);
        ctrl.updateValueAndValidity({emitEvent: false});
    }

    private detectChangesSafe(): void {
        if (!(this.cdr as any).destroyed) {
            this.cdr.detectChanges();
        }
    }
}
