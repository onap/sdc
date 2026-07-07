/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2026 Deutsche Telekom AG
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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';
import * as _ from 'lodash';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import {PropertyModel, PropertyFEModel, Component as IComponent, InputFEModel} from 'app/models';
import {InstanceFeDetails} from 'app/models/instance-fe-details';
import {CustomToscaFunction} from 'app/models/default-custom-functions';
import {ToscaGetFunction} from 'app/models/tosca-get-function';
import {PROPERTY_DATA, PROPERTY_TYPES, PROPERTY_VALUE_CONSTRAINTS, ValidationUtils} from 'app/utils';
import {WorkspaceService} from 'app/ng2/pages/workspace/workspace.service';
import {CompositionService} from 'app/ng2/pages/composition/composition.service';
import {TopologyTemplateService} from 'app/ng2/services/component-services/topology-template.service';
import {ModalService} from 'app/ng2/services/modal.service';
import {TranslateService} from 'app/ng2/shared/translator/translate.service';
import {PropertiesUtils} from 'app/ng2/pages/properties-assignment/services/properties.utils';
import {ToscaFunctionValidationEvent} from 'app/ng2/pages/properties-assignment/tosca-function/tosca-function.component';
import {SdcUiCommon, SdcUiComponents, SdcUiServices} from 'onap-ui-angular';
import {PropertyFormModalService, PropertyFormModalSaveContext} from './property-form-modal.service';

// Validation patterns registered as AngularJS values in app.ts. They are static (no injected state),
// so they are mirrored here to keep the reactive form self-contained (see app.ts: PropertyNameValidationPattern
// DE210977, CommentValidationPattern). getValidationPattern/validateJson/validateIntRange still delegate to the
// injected ValidationUtils because those are instance methods that depend on constructor-injected regex.
const PROPERTY_NAME_VALIDATION_PATTERN = /^[a-zA-Z0-9._:\-@]{1,100}$/;
const COMMENT_VALIDATION_PATTERN = /^[\u0000-\u00BF]*$/;

@Component({
    selector: 'property-form-modal',
    templateUrl: './property-form-modal.component.html',
    styleUrls: ['./property-form-modal.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PropertyFormModalComponent implements OnInit {

    public input: {
        property: PropertyModel;
        component: IComponent;
        filteredProperties: PropertyModel[];
        isPropertyValueOwner: boolean;
        propertyOwnerType: string;
        propertyOwnerId: string;
        isViewOnly: boolean;
        inputProperty: InputFEModel;
    };

    public form: FormGroup;
    public property: PropertyModel;
    public componentMetadata: { isService: boolean, isVfc: boolean };
    public nameMaxLength: number = PROPERTY_VALUE_CONSTRAINTS.NAME_MAX_LENGTH;
    public maxLength: number = PROPERTY_VALUE_CONSTRAINTS.MAX_LENGTH;

    // Type option lists (populated in ngOnInit; nonPrimitiveTypes filled by a later task from the datatype cache).
    public types: string[] = [];
    public nonPrimitiveTypes: string[] = [];

    // The recursive value editor (<dynamic-property>) drives these for complex types. valueObj is the live
    // object model; serializedValue is its JSON string (flattened via PropertyFEModel.stringifyValueObj);
    // valueValid mirrors the FEModel's valueObjIsValid. For simple types they stay undefined and save()
    // sources the scalar value straight from the reactive form.
    public valueObj: any;
    public serializedValue: string;
    public valueValid: boolean = true;

    // The FEModel the reused <dynamic-property> edits (built for complex types only). undefined for simple types.
    public propertyFEModel: PropertyFEModel;

    // BUG 2: the ModalsHandler owns the outer $q deferred that the caller's .then(reloadProperties) awaits;
    // this component cannot resolve it directly. On launch the ModalsHandler sets this callback (mirroring the
    // Save button's deferred.resolve + closeCurrentModal) so the delete-success path can resolve the deferred
    // and close the modal, letting the caller reload the table. Falls back to closeModal() when unset (unit tests).
    public deleteCallback: () => void;

    // ---- Task 6: default-value TYPE radio (Value/Entries vs TOSCA Function) ----
    // hasGetFunctionValue is the radio state: false = Value/Entries (value editor shown), true = TOSCA Function
    // (<tosca-function> shown, value editor hidden). Mirrors editPropertyModel.hasGetFunctionValue (VM:44,316).
    public hasGetFunctionValue: boolean = false;
    // isGetFunctionValid mirrors editPropertyModel.isGetFunctionValid (VM:45): true when the chosen tosca function
    // validates, undefined while pending/invalid — folded into isValid() when hasGetFunctionValue is on.
    public isGetFunctionValid: boolean = true;
    // Inputs the <tosca-function> component needs: the instance map (uniqueId -> {name}) and the custom functions.
    public componentInstanceMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();
    public customToscaFunctions: Array<CustomToscaFunction> = [];

    // ---- Task 6: constraints / metadata Save-gating ----
    // The old VM tracked the base form validity in invalidMandatoryFields and let the constraints / metadata
    // change events toggle footerButtons[0].disabled off it (VM:534-562). Here we keep the last emitted validity
    // of each sub-widget and fold them into isValid(); constraintsValid/metadataValid default true (no widget edits yet).
    public constraintsValid: boolean = true;
    public metadataValid: boolean = true;

    // ---- Task 6: prev/next navigation ----
    // currentPropertyIndex is the position within filteredProperties (old VM:309); getPrev/getNext walk it.
    public currentPropertyIndexValue: number = 0;

    constructor(private workspaceService: WorkspaceService,
                private validationUtils: ValidationUtils,
                private propertyFormModalService: PropertyFormModalService,
                private propertiesUtils: PropertiesUtils,
                private cdr: ChangeDetectorRef,
                private compositionService: CompositionService,
                private topologyTemplateService: TopologyTemplateService,
                private translateService: TranslateService,
                private modalServiceSdcUI: SdcUiServices.ModalService,
                private modalServiceNg2: ModalService) {
    }

    ngOnInit(): void {
        // Match the old VM: componentMetadata comes from workspaceService.metadata (Component has no isVfc()).
        this.componentMetadata = {
            isService: this.workspaceService.metadata.isService(),
            isVfc: this.workspaceService.metadata.isVfc()
        };

        // Primitive type list, sorted (old: editPropertyModel.types.sort). Copy first — never mutate the shared const.
        this.types = PROPERTY_DATA.TYPES.slice().sort((a, b) => a.localeCompare(b));

        // Position within filteredProperties for prev/next (old VM currentPropertyIndex, property-form-view-model.ts:309).
        this.currentPropertyIndexValue = this.findCurrentIndex(this.input.property);

        // Build the working property + reactive form + complex-value editor from the launch property.
        this.initResource(this.input.property);

        // Task 6: the <tosca-function> inputs. componentInstanceMap and customToscaFunctions are launch-time
        // constants (independent of which filteredProperties row is shown), so they are built once here —
        // mirroring the old initComponentInstanceMap / initCustomToscaFunctions (VM:236-254).
        this.initComponentInstanceMap();
        this.initCustomToscaFunctions();
    }

    /**
     * (Re)builds the working property, reactive form, TOSCA-function radio state and the complex-value editor
     * from the given source property. Called at open and by getPrev/getNext, mirroring the old VM's
     * initResource + initToscaGetFunction + initForNotSimpleType (property-form-view-model.ts:165-234).
     */
    private initResource(source: PropertyModel): void {
        // Working copy of the property: a fresh PropertyModel, type kept as "" (not null) so the empty <select>
        // option round-trips exactly as AngularJS did, and value falls back to defaultValue when absent.
        this.property = new PropertyModel(source);
        this.property.type = source && source.type ? source.type : '';
        this.property.value = source && source.value ? source.value : (source ? source.defaultValue : undefined);

        // Task 6: the radio state is derived from whether the property currently holds a tosca function
        // (old initToscaGetFunction, VM:174-177). isGetFunctionValid starts true so an unedited get-function
        // property is savable; onValueTypeChange re-gates it when the user switches the radio.
        this.hasGetFunctionValue = this.property.isToscaFunction();
        this.isGetFunctionValid = true;

        this.buildForm();
        this.setMaxLength();

        // For complex types, build the FEModel the reused <dynamic-property> edits. Mirror the old
        // initForNotSimpleType (property-form-view-model.ts:205-220): when there is no existing value the
        // complex value is seeded empty first so the editor shows one empty row.
        this.initComplexValue();
    }

    /**
     * Builds the {@link #componentInstanceMap} (uniqueId -> {name}) the reused <tosca-function> consumes,
     * from CompositionService.componentInstances — verbatim from the old initComponentInstanceMap (VM:236-245).
     */
    private initComponentInstanceMap(): void {
        this.componentInstanceMap = new Map<string, InstanceFeDetails>();
        if (this.compositionService.componentInstances) {
            this.compositionService.componentInstances.forEach((value) => {
                this.componentInstanceMap.set(value.uniqueId, <InstanceFeDetails>{name: value.name});
            });
        }
    }

    /**
     * Loads the custom TOSCA functions the reused <tosca-function> offers, from
     * TopologyTemplateService.getDefaultCustomFunction() — verbatim from the old initCustomToscaFunctions (VM:247-254).
     * OnPush: mark for check after the async result lands so the tosca-function inputs repaint.
     */
    private initCustomToscaFunctions(): void {
        this.customToscaFunctions = [];
        this.topologyTemplateService.getDefaultCustomFunction().subscribe((data) => {
            if (data) {
                data.forEach((customFunction) => this.customToscaFunctions.push(new CustomToscaFunction(customFunction)));
            }
            this.cdr.markForCheck();
        });
    }

    /**
     * Builds (or rebuilds) {@link #propertyFEModel} from the working property for complex types, seeding an
     * empty complex value first when the property has none — mirroring the old initForNotSimpleType /
     * initEmptyComplexValue (property-form-view-model.ts:205-220, 256-267). No-op for simple types.
     */
    private initComplexValue(): void {
        if (!this.isComplexType) {
            this.propertyFEModel = undefined;
            return;
        }
        // BUG 1: at CREATE time the name lives only in the reactive form, not yet on this.property. The FEModel
        // build below calls updateExpandedChildPropertyId(name) → name.lastIndexOf('#'), which throws on undefined.
        // Sync the name from the form first ('' is safe: ''.lastIndexOf('#') === -1). See failure-catalog.
        this.syncNameFromForm();
        if (!this.property.value && !this.property.defaultValue) {
            this.property.value = JSON.stringify(this.emptyComplexValue(this.property.type));
        }
        this.propertyFEModel = this.propertiesUtils.convertAddPropertyBAToPropertyFE(this.property);
        // Reviewer Important: seed the save-seam fields from the freshly-built FEModel. dynamic-property
        // only emits (propertyChanged) on USER edits, never on init, so without this an unedited save of an
        // EXISTING complex property would find serializedValue/valueObj undefined and overwrite the stored
        // value with undefined (silent data loss). The old VM avoided this by pre-populating myValue at open
        // (initForNotSimpleType, property-form-view-model.ts:205-220). No detectChanges here: this runs from
        // ngOnInit before the view exists (calling detectChanges pre-init would throw); onValueChanged keeps
        // its own detectChanges for the post-edit OnPush repaint.
        this.captureValueFromFEModel();
    }

    /**
     * Copies the current {@link #propertyFEModel} value onto the save-seam fields (serializedValue / valueObj /
     * valueValid) that {@link #save} reads, using the same flatten (PropertyFEModel.stringifyValueObj) the
     * properties-assignment getJSONValue idiom does. Shared by {@link #initComplexValue} (open-time seed) and
     * {@link #onValueChanged} (post-edit) so the two never drift.
     */
    private captureValueFromFEModel(): void {
        this.serializedValue = PropertyFEModel.stringifyValueObj(
            this.propertyFEModel.valueObj,
            this.propertyFEModel.schema.property.type,
            this.propertyFEModel.derivedDataType
        );
        this.valueObj = this.propertyFEModel.valueObj;
        this.valueValid = this.propertyFEModel.valueObjIsValid;
    }

    /**
     * BUG 1: reflect the reactive-form name onto the working property before building the complex-value
     * FEModel. At CREATE time this.property.name is undefined (the name lives only in the form) and
     * PropertiesUtils.convertAddPropertyBAToPropertyFE → PropertyFEModel.updateExpandedChildPropertyId does
     * name.lastIndexOf('#'), which throws on undefined. Defaulting to '' is safe (''.lastIndexOf('#') === -1).
     */
    private syncNameFromForm(): void {
        if (this.form && this.form.get('name')) {
            this.property.name = this.form.get('name').value || '';
        } else if (this.property.name == null) {
            this.property.name = '';
        }
    }

    // Old initEmptyComplexValue (property-form-view-model.ts:256-267): map → {'': null}, list → [''], else {}.
    private emptyComplexValue(type: string): any {
        switch (type) {
            case PROPERTY_TYPES.MAP:
                return {'': null};
            case PROPERTY_TYPES.LIST:
                return [''];
            default:
                return {};
        }
    }

    // Complex-vs-simple boundary, exactly the old isComplexType (property-form-view-model.ts:269-274):
    // a populated type not in PROPERTY_DATA.SIMPLE_TYPES is complex → render <dynamic-property>; simple →
    // keep the scalar text input / boolean select. (Task 6 gates this further with !hasGetFunctionValue.)
    public get isComplexType(): boolean {
        const type: string = this.property && this.property.type;
        return !!type && PROPERTY_DATA.SIMPLE_TYPES.indexOf(type) === -1;
    }

    // The reused editor is read-only in view-only mode (mirrors the old isViewOnly gate on the value region).
    public get isValueReadonly(): boolean {
        return this.input.isViewOnly;
    }

    /**
     * Task-5 seam: the whole value region (scalar input, boolean select, <dynamic-property>) is shown only when
     * the "Value/Entries" radio is selected, mirroring the old template's data-ng-if="!hasGetFunctionValue"
     * wrapper (property-form-view.html:165). When "TOSCA Function" is selected the value editor hides and
     * <tosca-function> takes over.
     */
    public get showValueEditor(): boolean {
        return !this.hasGetFunctionValue;
    }

    /**
     * The default-value TYPE radio is offered only for non-VFC components (old ng-if="!componentMetadata.isVfc",
     * property-form-view.html:144). For a VFC the value editor is always shown (hasGetFunctionValue stays false).
     */
    public get showToscaFunctionOption(): boolean {
        return !this.componentMetadata.isVfc;
    }

    /**
     * Reused-editor change handler. Flattens the FEModel's valueObj back to a JSON string exactly like
     * properties-assignment's getJSONValue (PropertyFEModel.stringifyValueObj), stores it on the save seam
     * fields, then triggers OnPush change detection so the reused editor stays painted.
     */
    public onValueChanged(): void {
        this.captureValueFromFEModel();
        this.cdr.detectChanges();
    }

    // ---- Task 6: TOSCA-function radio + <tosca-function> event handlers ----

    /**
     * Radio-change handler for the Value/Entries ↔ TOSCA Function toggle (old onValueTypeChange, VM:524-532).
     * Clears the current value, then re-gates the tosca-function validity: selecting TOSCA Function makes the
     * form invalid until a valid function is chosen (isGetFunctionValid=undefined); selecting Value/Entries
     * drops any chosen function and restores validity.
     */
    public onValueTypeChange(): void {
        this.setEmptyValue();
        if (this.hasGetFunctionValue) {
            this.isGetFunctionValid = undefined;
        } else {
            this.property.toscaFunction = undefined;
            this.isGetFunctionValid = true;
        }
    }

    // Old setEmptyValue (property-form-view-model.ts:588-596): clear the scalar value, or (re)seed an empty
    // complex value + rebuild the FEModel so the editor shows an empty row when switching back to Value/Entries.
    private setEmptyValue(): void {
        this.property.value = undefined;
        if (this.isComplexType) {
            // BUG 1: ensure the working property carries a name before rebuilding the FEModel (see syncNameFromForm).
            this.syncNameFromForm();
            this.property.value = JSON.stringify(this.emptyComplexValue(this.property.type));
            this.propertyFEModel = this.propertiesUtils.convertAddPropertyBAToPropertyFE(this.property);
            this.captureValueFromFEModel();
        } else {
            this.form.get('value').setValue('', {emitEvent: false});
        }
    }

    // <tosca-function> (onValidFunction) — store the emitted function on the property (old, VM:564-566).
    public onGetFunctionValidFunction(toscaGetFunction: ToscaGetFunction): void {
        this.property.toscaFunction = toscaGetFunction;
    }

    // <tosca-function> (onValidityChange) — mirror the old onToscaFunctionValidityChange (VM:568-574):
    // valid → isGetFunctionValid=true, invalid → undefined (which fails isValid() while hasGetFunctionValue).
    public onToscaFunctionValidityChange(validationEvent: ToscaFunctionValidationEvent): void {
        this.isGetFunctionValid = validationEvent && validationEvent.isValid ? true : undefined;
    }

    // ---- Task 6: <app-constraints> / <app-property-metadata> event handlers ----

    /**
     * <app-constraints> (onConstraintChange) — serialize the emitted constraints onto the property and track
     * the widget's validity for Save-gating. Mirrors the old onConstraintChange (VM:534-549): an empty list
     * nulls both constraint fields; otherwise propertyConstraints holds the JSON-stringified array and
     * constraints holds the parsed objects.
     */
    public onConstraintChange(event: { constraints: any[], valid: boolean }): void {
        this.constraintsValid = event.valid;
        if (!event.constraints || event.constraints.length === 0) {
            this.property.propertyConstraints = null;
            this.property.constraints = null;
            return;
        }
        this.property.propertyConstraints = this.serializePropertyConstraints(event.constraints);
        this.property.constraints = event.constraints;
    }

    /**
     * <app-property-metadata> (onPropertyMetadataChange) — store the emitted metadata and track its validity.
     * Mirrors the old onPropertyMetadataChange (VM:551-562): an empty metadata object nulls property.metadata.
     * PropertyMetadataComponent emits {metadata: {}, valid} for an empty list, so treat an empty object as null.
     */
    public onPropertyMetadataChange(event: { metadata: any, valid: boolean }): void {
        this.metadataValid = event.valid;
        if (!event.metadata || Object.keys(event.metadata).length === 0) {
            this.property.metadata = null;
            return;
        }
        this.property.metadata = event.metadata;
    }

    // Old serializePropertyConstraints (property-form-view-model.ts:577-586).
    private serializePropertyConstraints(constraints: any[]): string[] {
        if (constraints) {
            return constraints.map((constraint) => JSON.stringify(constraint));
        }
        return null;
    }

    // ---- Task 6: prev/next navigation through filteredProperties ----

    private findCurrentIndex(property: PropertyModel): number {
        if (!property || !this.input.filteredProperties) {
            return 0;
        }
        const idx = this.input.filteredProperties.findIndex((p) => p.name === property.name);
        return idx === -1 ? 0 : idx;
    }

    // Old getPrev (property-form-view-model.ts:432-437): re-init the form + value region from the previous row.
    public getPrev(): void {
        if (this.isFirstProperty()) {
            return;
        }
        this.currentPropertyIndexValue--;
        this.initResource(this.input.filteredProperties[this.currentPropertyIndexValue]);
    }

    // Old getNext (property-form-view-model.ts:439-444): re-init the form + value region from the next row.
    public getNext(): void {
        if (this.isLastProperty()) {
            return;
        }
        this.currentPropertyIndexValue++;
        this.initResource(this.input.filteredProperties[this.currentPropertyIndexValue]);
    }

    // prev is disabled at index 0 (old ng-class="{'disabled': !currentPropertyIndex}", template:22).
    public isFirstProperty(): boolean {
        return this.currentPropertyIndexValue === 0;
    }

    // next is disabled at the last row (old isLastProperty, VM:310, template:23).
    public isLastProperty(): boolean {
        return !this.input.filteredProperties || this.currentPropertyIndexValue >= this.input.filteredProperties.length - 1;
    }

    // The top-bar (delete / prev / next) is shown only for an existing, non-input property
    // (old data-ng-if="!isNew && !editPropertyModel.property.propertyView", template:18).
    public get showTopBar(): boolean {
        return !this.isNew && !this.property.propertyView;
    }

    // isNew mirrors the old FormState.CREATE check: a property with no name is being created (VM:161,303).
    public get isNew(): boolean {
        return !this.input.property || !this.input.property.name;
    }

    // Delete is disabled for value-owners / readonly / group / policy owners
    // (old ng-class disabled predicate, template:20).
    public get isDeleteDisabled(): boolean {
        return this.input.isPropertyValueOwner || this.property.readonly
            || this.input.propertyOwnerType === 'group' || this.input.propertyOwnerType === 'policy';
    }

    // ---- Task 6: delete (confirm + splice) ----

    /**
     * Opens the SdcUi confirm dialog and, on OK, DELETEs the property on the BE and removes it from
     * filteredProperties before closing the modal. Mirrors the old delete + deleteProperty (VM:512-522, 638-647):
     * the old VM's deleteProperty did _.remove(filteredProperties, {uniqueId}); the ROUTING-ONLY
     * PropertyFormModalService.deleteProperty does not, so the splice is done HERE (Task-4 obligation).
     */
    public deleteCurrent(): void {
        const property: PropertyModel = this.property;
        const onOk: Function = (): void => {
            const metadata = this.workspaceService.metadata;
            this.propertyFormModalService
                .deleteProperty(metadata.componentType, metadata.uniqueId, property.uniqueId)
                .subscribe(() => {
                    // Task-4 obligation: mirror the old _.remove so the deleted row does not linger in the
                    // shared filteredProperties list the caller keeps rendering.
                    _.remove(this.input.filteredProperties, {uniqueId: property.uniqueId});
                    // BUG 2: resolve the ModalsHandler deferred (+ close) so the caller's .then(reloadProperties)
                    // runs and the table re-fetches; fall back to closeModal() when the callback is not wired.
                    if (this.deleteCallback) {
                        this.deleteCallback();
                    } else {
                        this.closeModal();
                    }
                });
        };
        const title: string = this.translateService.translate('PROPERTY_VIEW_DELETE_MODAL_TITLE');
        const message: string = this.translateService.translate('PROPERTY_VIEW_DELETE_MODAL_TEXT', {name: property.name});
        const okButton = {
            testId: 'OK',
            text: 'OK',
            type: SdcUiCommon.ButtonType.info,
            callback: onOk,
            closeModal: true
        } as SdcUiComponents.ModalButtonComponent;
        this.modalServiceSdcUI.openInfoModal(title, message, 'delete-modal', [okButton]);
    }

    // Close seam for the delete-success path — delegates to the same ng2 ModalService the ModalsHandler used to
    // open this modal (createCustomModal/closeCurrentModal). Overridable in tests.
    public closeModal(): void {
        this.modalServiceNg2.closeCurrentModal();
    }

    private buildForm(): void {
        const schemaType = this.property.schema && this.property.schema.property && this.property.schema.property.type
            ? this.property.schema.property.type : '';

        // §Q / reviewer Important #2: the form inits type/schemaType to '' (matching the old AngularJS
        // <option value="">), but the OLD VM held null (property-form-view-model.ts:167). PropertyModel.toJSON()
        // does angular.copy() and does NOT coerce type, so an empty '' would wire type:"" — which keys the BE
        // datatype cache differently from null/omitted. save() therefore coerces ''->null on the property before
        // delegating to the BE (see save() below); the wire payload never ships type:'' (asserted in the spec).
        this.form = new FormGroup({
            name: new FormControl(this.property.name || '', this.nameValidators()),
            type: new FormControl(this.property.type || '', Validators.required),
            schemaType: new FormControl(schemaType, this.schemaTypeValidators()),
            description: new FormControl(this.property.description || '', [
                Validators.maxLength(400),
                Validators.pattern(COMMENT_VALIDATION_PATTERN)
            ]),
            value: new FormControl(this.property.value != null ? this.property.value : '')
        });
        // BUG 3: gate the scalar value with the same client-side validation the old template had
        // (ng-pattern + per-type validateJson / validateIntRange). Applied here and re-applied on type change.
        this.applyValueValidator();
    }

    /**
     * BUG 3: (re)installs the scalar value validator for the current simple type, reproducing the old
     * AngularJS wiring (property-form-view.html): ng-pattern="getValidationPattern(simpleType||type)" plus
     * ng-change that set $setValidity('pattern', validateJson(value)) for json and validateIntRange(value)
     * for integer. An empty value stays valid; a bad value fails with the {value: true} error so the
     * value control becomes invalid, Save is disabled (isValid() folds in form.valid), and the value never POSTs.
     * Only the scalar (non-complex) value control is validated — complex values are validated by the FEModel.
     */
    private applyValueValidator(): void {
        if (!this.form || !this.form.get('value')) {
            return;
        }
        this.form.get('value').setValidators(this.valueValidator());
        this.form.get('value').updateValueAndValidity({emitEvent: false});
    }

    private valueValidator(): ValidatorFn {
        return (control: FormControl): { [key: string]: any } | null => {
            // Complex types are edited by <dynamic-property> (validated via valueValid), not the scalar control.
            if (this.isComplexType) {
                return null;
            }
            const raw = control.value;
            // Empty value is valid (nothing entered yet) — matches the old ng-pattern (empty passes).
            if (raw == null || raw === '') {
                return null;
            }
            const type: string = (this.property && this.property.simpleType) || this.property.type;
            const strValue: string = String(raw);
            const pattern: RegExp = this.getValidationPattern(type);
            if (pattern && !pattern.test(strValue)) {
                return {value: true};
            }
            // Per-type extra checks the old template applied via $setValidity('pattern', ...).
            if (type === PROPERTY_TYPES.JSON && !this.validateJson(strValue)) {
                return {value: true};
            }
            if (type === PROPERTY_TYPES.INTEGER && !this.validateIntRange(strValue)) {
                return {value: true};
            }
            return null;
        };
    }

    // Name: required only when NOT a service (old: label ng-class="{'required': !componentMetadata.isService}");
    // pattern is the PropertyNameValidationPattern, maxlength 100.
    private nameValidators(): ValidatorFn[] {
        const validators: ValidatorFn[] = [
            Validators.maxLength(this.nameMaxLength),
            Validators.pattern(PROPERTY_NAME_VALIDATION_PATTERN)
        ];
        if (!this.componentMetadata.isService) {
            validators.push(Validators.required);
        }
        return validators;
    }

    // Entry schema: required only while it is rendered (old: field lives inside data-ng-if="showSchema()",
    // so in AngularJS it is neither shown nor validated for scalar types). See failure-catalog S.
    private schemaTypeValidators(): ValidatorFn[] {
        return this.showSchema() ? [Validators.required] : [];
    }

    public showSchema(): boolean {
        return [PROPERTY_TYPES.LIST, PROPERTY_TYPES.MAP].indexOf(this.property.type) > -1;
    }

    public isSimpleType(typeName: string): boolean {
        return typeName && PROPERTY_DATA.SIMPLE_TYPES.indexOf(typeName) !== -1;
    }

    // Value-widget gate: is the current property's value a boolean (render the true/false <select>)
    // or a scalar (render the text <input>). Mirrors the old template's (simpleType || type) === 'boolean'
    // (property-form-view.html:221,236) — simpleType wins when populated, else the current form type.
    public isBooleanValue(): boolean {
        const type = (this.property && this.property.simpleType) || this.form.get('type').value;
        return type === PROPERTY_TYPES.BOOLEAN;
    }

    public onTypeChange(): void {
        // Reflect the new type onto the working model, reset the value, then recompute constraints.
        this.property.type = this.form.get('type').value;
        this.property.value = '';
        this.property.defaultValue = '';
        this.form.get('value').setValue('', {emitEvent: false});
        // The entry-schema field is now shown/hidden by the new type — re-gate its required validator.
        this.form.get('schemaType').setValidators(this.schemaTypeValidators());
        this.form.get('schemaType').updateValueAndValidity({emitEvent: false});
        this.setMaxLength();
        // BUG 3: the value type changed, so the scalar value validator must be recomputed for the new type
        // (integer/json/string pattern etc.). Re-apply before initComplexValue so a complex type disables it.
        this.applyValueValidator();
        // Old onTypeChange re-ran initForNotSimpleType (property-form-view-model.ts:496-501): if the new
        // type is complex, seed an empty complex value and (re)build the FEModel so the editor shows an
        // empty row; if it is simple, clear the FEModel so the scalar widget takes over.
        this.initComplexValue();
    }

    public onSchemaTypeChange(): void {
        this.property.schema.property.type = this.form.get('schemaType').value;
        this.setMaxLength();
        // Old onSchemaTypeChange reset myValue to {} (map) / [] (list) (property-form-view-model.ts:503-510):
        // reproduce on the rebuilt FEModel so switching entry-schema clears the editor's rows.
        if (this.isComplexType) {
            // BUG 1: sync the name before rebuilding the FEModel (create-of-new-map path, see syncNameFromForm).
            this.syncNameFromForm();
            if (this.property.type === PROPERTY_TYPES.MAP) {
                this.property.value = JSON.stringify({});
            } else if (this.property.type === PROPERTY_TYPES.LIST) {
                this.property.value = JSON.stringify([]);
            }
            this.property.defaultValue = '';
            this.propertyFEModel = this.propertiesUtils.convertAddPropertyBAToPropertyFE(this.property);
            // Re-seed the save seam from the rebuilt model (same reason as initComplexValue): the reset
            // is silent to the editor, so an unedited save after a schema-type change must ship the reset
            // value, not the stale pre-change seam.
            this.captureValueFromFEModel();
        }
    }

    public setMaxLength(): void {
        switch (this.property.type) {
            case PROPERTY_TYPES.MAP:
            case PROPERTY_TYPES.LIST:
                this.maxLength = this.property.schema.property.type === PROPERTY_TYPES.JSON ?
                    PROPERTY_VALUE_CONSTRAINTS.JSON_MAX_LENGTH :
                    PROPERTY_VALUE_CONSTRAINTS.MAX_LENGTH;
                break;
            case PROPERTY_TYPES.JSON:
                this.maxLength = PROPERTY_VALUE_CONSTRAINTS.JSON_MAX_LENGTH;
                break;
            default:
                this.maxLength = PROPERTY_VALUE_CONSTRAINTS.MAX_LENGTH;
        }
    }

    public getValidationPattern(type: string): RegExp {
        return this.validationUtils.getValidationPattern(type);
    }

    public validateJson(json: string): boolean {
        if (!json) {
            return true;
        }
        return this.validationUtils.validateJson(json);
    }

    public validateIntRange(value: string): boolean {
        return !value || this.validationUtils.validateIntRange(value);
    }

    isValid(): boolean {
        // Completed Save-gating (Task 6), reproducing the old $watch on forms.editForm.$valid/$invalid
        // (property-form-view-model.ts:472-490): Save is enabled iff
        //   (1) the reactive form is valid,
        //   (2) NOT view-only,
        //   (3) the value region is valid, which splits on the default-value radio:
        //         - hasGetFunctionValue: a tosca function is chosen (property.toscaFunction set) AND it validates
        //           (isGetFunctionValid === true) — the value editor is hidden, so its valueValid is irrelevant;
        //         - else: the complex value editor is valid (valueValid; true for simple types), and
        //     AND the constraints / metadata sub-widgets last reported valid (old onConstraintChange/onPropertyMetadataChange
        //         toggled footerButtons[0].disabled off !constraints.valid / !metadata.valid).
        if (!this.form || !this.form.valid || this.input.isViewOnly) {
            return false;
        }
        if (!this.constraintsValid || !this.metadataValid) {
            return false;
        }
        if (this.hasGetFunctionValue) {
            return !!this.property.toscaFunction && this.isGetFunctionValid === true;
        }
        const valueRegionValid: boolean = !this.isComplexType || this.valueValid;
        return valueRegionValid;
    }

    /**
     * Assembles the property from the reactive form (+ the launch inputs), applies the same
     * description strip/sanitize and complex-value serialization the old view-model did, then delegates
     * to {@link PropertyFormModalService#save}. Returns the service Observable so the ModalsHandler Save
     * callback can resolve its deferred with the saved property and close the modal (the modal is NOT
     * closed here). Mirrors property-form-view-model.ts:354-430 (minus the modal-close / early-returns).
     */
    save(): Observable<PropertyModel | void> {
        // Reflect the reactive-form scalar fields onto the working property.
        const property: PropertyModel = this.property;
        property.name = this.form.get('name').value;
        property.type = this.form.get('type').value;
        property.value = this.form.get('value').value;

        // §Q / reviewer Important #2: coerce empty '' type/schemaType back to null so PropertyModel.toJSON()
        // (which does angular.copy() and does NOT touch type) never wires type:"" — matching the old VM's
        // null (property-form-view-model.ts:167) and keeping the BE datatype-cache key parity.
        if (property.type === '') {
            property.type = null;
        }
        if (property.schema && property.schema.property && property.schema.property.type === '') {
            property.schema.property.type = null;
        }

        // Description strip/sanitize is a component concern (keeps the service BE-call-only) — old VM line 373.
        property.description = this.validationUtils.stripAndSanitize(this.form.get('description').value);

        // Complex-value serialization (old VM lines 405-428). For a not-simple type the value comes from the
        // Task-5 value region (this.valueObj / this.serializedValue); for simple types the scalar form value is
        // already on property.value. isPropertyValueOwner uses value only; the plain-property branch also seeds
        // defaultValue. simpleType wins when populated, mirroring the old (!simpleType && !isSimpleType(type)).
        const complex: boolean = !property.simpleType && !this.isSimpleType(property.type);
        if (this.input.isPropertyValueOwner) {
            if (complex) {
                property.value = this.getSerializedComplexValue();
            }
        } else if (this.input.propertyOwnerType === 'policy') {
            if (complex && this.valueObj != null) {
                property.value = this.getSerializedComplexValue();
            }
        } else if (this.input.propertyOwnerType !== 'group' && !property.propertyView) {
            if (complex) {
                property.defaultValue = this.getSerializedComplexValue();
                property.value = property.defaultValue;
            } else {
                property.defaultValue = property.value;
            }
        }

        const ctx: PropertyFormModalSaveContext = {
            property,
            component: this.input.component,
            filteredProperties: this.input.filteredProperties,
            currentPropertyIndex: this.currentPropertyIndex(),
            isPropertyValueOwner: this.input.isPropertyValueOwner,
            propertyOwnerType: this.input.propertyOwnerType,
            propertyOwnerId: this.input.propertyOwnerId,
            inputProperty: this.input.inputProperty,
            myValueJson: this.serializedValue
        };
        return this.propertyFormModalService.save(ctx);
    }

    // Task 5 seam: the JSON string of the complex value. Until the value region is wired, serializedValue is
    // fed by Task 5 (falls back to stringifying valueObj so the plumbing is exercisable now).
    private getSerializedComplexValue(): string {
        if (this.serializedValue != null) {
            return this.serializedValue;
        }
        return JSON.stringify(this.valueObj);
    }

    private currentPropertyIndex(): number {
        return this.input.filteredProperties.findIndex((p) => p.name === this.property.name);
    }
}
