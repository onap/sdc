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
import {FormGroup} from '@angular/forms';
import {Subject} from 'rxjs/Subject';
import 'rxjs/add/operator/takeUntil';
import * as _ from 'lodash';

import {CacheService} from 'app/services-ng2';
import {EventListenerService} from 'app/services';
import {EVENTS, WorkspaceMode, ComponentState, ComponentType, Role, PREVIOUS_CSAR_COMPONENT, instantiationType, DEFAULT_MODEL_NAME, DEFAULT_ICON} from 'app/utils/constants';
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
        const user = this.cacheService.get('user');
        this.role = user ? user.role : '';
        this.mode = this.initMode(user);
        this.form = this.generalFormService.buildForm(this.readPatterns());
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
    }

    // ---- Data-init methods ported from the old AngularJS GeneralViewModel ----

    /** Ported from GeneralViewModel.initCategories (lines 494-511). Reads categories from CacheService. */
    private initCategories(): void {
        if (this.component.componentType === ComponentType.RESOURCE) {
            this.categories = this.cacheService.get('resourceCategories') || [];
        }
        if (this.component.componentType === ComponentType.SERVICE) {
            this.categories = this.cacheService.get('serviceCategories') || [];
            // Remove categories not applicable for External API
            if (this.isCreateMode() || (ComponentState.NOT_CERTIFIED_CHECKOUT === this.component.lifecycleState)) {
                this.categories = this.filteredCategories();
                this.isHiddenCategorySelected = this.isHiddenCategory(this.component.selectedCategory);
            }
        }
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
        }
        this.detectChangesSafe();
    }

    /** Ported from GeneralViewModel.initModel (lines 552-598). Service + all component types. */
    private initModel(): void {
        this.isModelRequired = false;
        this.models = [];
        this.defaultModelOption = DEFAULT_MODEL_NAME;
        this.showDefaultModelOption = true;

        // filterCategoriesByModel — now that initCategories() populates this.categories,
        // apply model-based filtering here (ported from GeneralViewModel, Task 8c).
        this.filterCategoriesByModel(this.component.model || null);

        if (this.isCreateMode() && this.isVspImport()) {
            const modelOptions = this.component.componentMetadata && this.component.componentMetadata.models;
            if (modelOptions) {
                this.isModelRequired = true;
                if (modelOptions.length === 1) {
                    this.models = modelOptions;
                    this.component.model = modelOptions[0];
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
            this.component.categories = this.buildCategoriesFromSelected();
            this.component.icon = DEFAULT_ICON;
        } else {
            this.component.categories = undefined;
        }
        this.detectChangesSafe();
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

    // ---- Tags widget ----

    newTag: string = '';
    private readonly maxTags = 20;

    get displayedTags(): string[] {
        const tags = (this.component && this.component.tags) || [];
        // Don't show the component-name special tag as a removable chip (mirrors old sdc-tags directive)
        return tags.filter((t: string) => t !== (this.component && this.component.name));
    }

    addTag(): void {
        const t = (this.newTag || '').trim();
        const tags = this.component.tags || (this.component.tags = []);
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

    private detectChangesSafe(): void {
        if (!(this.cdr as any).destroyed) {
            this.cdr.detectChanges();
        }
    }
}
